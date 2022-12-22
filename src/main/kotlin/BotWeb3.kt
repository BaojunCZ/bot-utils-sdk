import BotSdkConstants.Gwei
import kotlinx.coroutines.delay
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import utils.Utils
import java.math.BigInteger
import java.util.*
import java.util.concurrent.CompletableFuture

class BotWeb3(rpc: String, var defaultGasPrice: BigInteger = BigInteger.valueOf(5) * Gwei) {
    val web3j: Web3j = Web3j.build(HttpService(rpc))

    fun getNonce(address: String): BigInteger? {
        return try {
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().transactionCount
        } catch (e: Exception) {
            Utils.log(e.message.toString())
            Utils.log("nonce 获取失败")
            null
        }
    }

    @kotlin.jvm.Throws
    fun sendTransaction(credentials: Credentials, to: String, data: String, gasPrice: BigInteger = defaultGasPrice): String {
        return sendTransaction(credentials, gasPrice, to, data, BigInteger.ZERO)
    }

    @kotlin.jvm.Throws
    fun sendTransaction(credentials: Credentials, gasPrice: BigInteger, to: String, data: String, value: BigInteger): String {
        return getNonce(credentials.address)?.let { nonce ->
            val transaction = Transaction.createFunctionCallTransaction(credentials.address, nonce, gasPrice, BigInteger.ZERO, to, value, data)
            val result = web3j.ethEstimateGas(transaction).send()
            if (result.result == null) {
                throw  Exception("估算gasLimit失败 ${result.error.message}")
            } else {
                var gasLimit = BigInteger(result.result.removePrefix("0x"), 16)
                if (data != "" && data != "0x") {
                    gasLimit *= BigInteger.valueOf(2)
                }
                sendTransaction(credentials, nonce, defaultGasPrice, gasLimit, to, data, value)
            }
        } ?: kotlin.run {
            throw  Exception("获取nonce失败")
        }
    }

    @kotlin.jvm.Throws
    fun sendTransaction(credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String, value: BigInteger)
            : String {
        return getNonce(credentials.address)?.let {
            sendTransaction(credentials, it, gasPrice, gasLimit, to, data, value)
        } ?: kotlin.run {
            throw  Exception("获取nonce失败")
        }
    }

    @kotlin.jvm.Throws
    fun sendTransaction(
        credentials: Credentials,
        nonce: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        to: String,
        data: String,
        value: BigInteger
    )
            : String {
        val rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data)
        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)
        val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
        if (ethSendTransaction.hasError()) {
            throw  Exception("交易失败")
        } else {
            return ethSendTransaction.transactionHash
        }
    }

    suspend fun waitTx(hash: String): Boolean? {
        return try {
            delay(3000)
            var result = queryTransaction(hash)
            while (result == null) {
                result = queryTransaction(hash)
                delay(2000)
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    fun observeTx(hash: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val timer = Timer()
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    val status = queryTransaction(hash)
                    if (status != null) {
                        future.complete(status)
                        timer.cancel()
                    }
                }
            }, 4 * 1000, 4 * 1000
        )
        return future
    }

    @kotlin.jvm.Throws
    fun queryTransaction(hash: String, log: Boolean = false): Boolean? {
        val receipt = web3j.ethGetTransactionReceipt(hash).send()
        return if (receipt.transactionReceipt.isPresent) {
            val status = receipt.transactionReceipt.get().status
            if (!status.isNullOrBlank()) {
                if (status == "0x1") {
                    if (log)
                        Utils.log(("交易成功 $hash"))
                    true
                } else {
                    if (log)
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

    @kotlin.jvm.Throws
    fun ethCall(function: org.web3j.abi.datatypes.Function, from: String, to: String): MutableList<Type<Any>> {
        val encoding = FunctionEncoder.encode(function)
        val ethCallTransaction: Transaction =
            Transaction.createEthCallTransaction(from, to, encoding)
        val response = web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send()
        return FunctionReturnDecoder.decode(response.value, function.outputParameters)
    }
}