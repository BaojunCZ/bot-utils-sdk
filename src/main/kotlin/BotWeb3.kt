import BotSdkConstants.defaultGasPrice
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.*
import java.util.concurrent.CompletableFuture

class BotWeb3(rpc: String) {
    val web3j: Web3j = Web3j.build(HttpService(rpc))

    private fun getNonce(address: String): BigInteger? {
        return try {
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().transactionCount
        } catch (e: Exception) {
            Utils.log(e.message.toString())
            Utils.log("nonce 获取失败")
            null
        }
    }

    fun sendTransactionAsync(credentials: Credentials, to: String, data: String)
            : CompletableFuture<String> {
        return getNonce(credentials.address)?.let { nonce ->
            val transaction = Transaction.createFunctionCallTransaction(credentials.address, nonce, defaultGasPrice, BigInteger.ZERO, to, data)
            web3j.ethEstimateGas(transaction).sendAsync()
                .thenCompose {
                    if (it.result == null) {
                        Utils.log(it.error.message)
                        CompletableFuture<String>().exceptionally { "估算gasLimit失败" }
                    } else {
                        val gasLimit = BigInteger(it.result.removePrefix("0x"), 16) * BigInteger.valueOf(2)
                        sendTransactionAsync(credentials, nonce, defaultGasPrice, gasLimit, to, data)
                    }
                }
        } ?: kotlin.run {
            CompletableFuture<String>().exceptionally { "获取nonce失败" }
        }
    }

    fun sendTransactionAsync(credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String)
            : CompletableFuture<String> {
        return getNonce(credentials.address)?.let {
            sendTransactionAsync(credentials, it, gasPrice, gasLimit, to, data)
        } ?: kotlin.run {
            CompletableFuture<String>().exceptionally { "获取nonce失败" }
        }
    }

    fun sendTransactionAsync(credentials: Credentials, nonce: BigInteger, gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String)
            : CompletableFuture<String> {
        val future = CompletableFuture<String>()
        val rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data)
        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        web3j.ethSendRawTransaction(hexValue).sendAsync().thenAccept { ethSendTransaction ->
            if (ethSendTransaction.hasError()) {
                future.exceptionally {
                    ethSendTransaction.error.message
                }
            } else {
                future.complete(ethSendTransaction.transactionHash)
            }
        }
        return future
    }

    fun sendTransaction(credentials: Credentials, to: String, data: String): String {
        return getNonce(credentials.address)?.let { nonce ->
            val transaction = Transaction.createFunctionCallTransaction(credentials.address, nonce, defaultGasPrice, BigInteger.ZERO, to, data)
            val result = web3j.ethEstimateGas(transaction).send()
            if (result.result == null) {
                throw  Exception("估算gasLimit失败 ${result.error.message}")
            } else {
                val gasLimit = BigInteger(result.result.removePrefix("0x"), 16) * BigInteger.valueOf(2)
                sendTransaction(credentials, nonce, defaultGasPrice, gasLimit, to, data)
            }
        } ?: kotlin.run {
            throw  Exception("获取nonce失败")
        }
    }

    fun sendTransaction(credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String)
            : String {
        return getNonce(credentials.address)?.let {
            sendTransaction(credentials, it, gasPrice, gasLimit, to, data)
        } ?: kotlin.run {
            throw  Exception("获取nonce失败")
        }
    }

    fun sendTransaction(credentials: Credentials, nonce: BigInteger, gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String)
            : String {
        val rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data)
        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
        if (ethSendTransaction.hasError()) {
            throw  Exception("交易失败")
        } else {
            return ethSendTransaction.transactionHash
        }
    }

    fun observeTx(hash: String): CompletableFuture<Boolean> {

        fun queryTx(hash: String, future: CompletableFuture<Boolean>) {
            Timer().schedule(
                object : TimerTask() {
                    override fun run() {
                        val status = queryTransaction(hash)
                        if (status == null) {
                            queryTx(hash, future)
                        } else {
                            future.complete(status)
                        }
                    }
                }, 4 * 1000
            )
        }

        val future = CompletableFuture<Boolean>()
        queryTx(hash, future)
        return future
    }

    fun queryTransaction(hash: String): Boolean? {
        val receipt = web3j.ethGetTransactionReceipt(hash).send()
        return if (receipt.transactionReceipt.isPresent) {
            val status = receipt.transactionReceipt.get().status
            if (!status.isNullOrBlank()) {
                if (status == "0x1") {
                    Utils.log(("交易成功 $hash"))
                    true
                } else {
                    Utils.log(("交易失败 $hash"))
                    false
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    fun queryTransactionAsync(hash: String): CompletableFuture<Boolean?> {
        return web3j.ethGetTransactionReceipt(hash).sendAsync().thenCompose {
            CompletableFuture.supplyAsync {
                if (it.transactionReceipt.isPresent) {
                    val status = it.transactionReceipt.get().status
                    if (!status.isNullOrBlank()) {
                        if (status == "0x1") {
                            Utils.log(("交易成功 $hash"))
                            true
                        } else {
                            Utils.log(("交易失败 $hash"))
                            false
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }.exceptionally {
            it.printStackTrace()
            null
        }
    }
}