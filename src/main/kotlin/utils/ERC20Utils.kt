package utils

import BotWeb3
import WalletManager
import contract.Disperse
import contract.DisperseData
import contract.MultiCall
import contract.MultiCallData
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Convert
import utils.Utils.zeroPad
import java.io.IOException
import java.math.BigInteger

object ERC20Utils {
    fun batchBalance(multiCall: MultiCall, addresses: List<String>, token: String): List<BigInteger> {
        val calls = addresses.map {
            val ethBalanceFunction = Function(
                "balanceOf",
                listOf(Address(it)),
                listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
            )
            MultiCallData(token, FunctionEncoder.encode(ethBalanceFunction))
        }
        val balances = multiCall.aggregate(calls)
        return balances.map {
            it?.let {
                BigInteger(it.value)
            } ?: BigInteger.ZERO
        }
    }


    @Throws(IOException::class)
    fun balanceOf(address: String, token: String, botWeb3: BotWeb3): BigInteger {
        val function = Function("balanceOf", listOf(Address(address)), listOf(object : TypeReference<Int256?>() {}))
        val result = botWeb3.ethCall(function, address, token)
        return (result[0] as Int256).value
    }

    @kotlin.jvm.Throws
    fun decimals(token: String, botWeb3: BotWeb3): BigInteger {
        val decimalsFunction = Function(
            "decimals",
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        val response = botWeb3.web3j.ethCall(
            Transaction.createEthCallTransaction(token, token, FunctionEncoder.encode(decimalsFunction)),
            DefaultBlockParameterName.LATEST
        ).send()
        Utils.log(response.value)
        val decode = FunctionReturnDecoder.decode(response.value, decimalsFunction.outputParameters)
        return (decode[0] as Uint256).value
    }

    @kotlin.jvm.Throws
    fun transfer(wallet: WalletManager.WalletIndexed, token: String, amount: BigInteger, toAddress: String, botWeb3: BotWeb3): String {
        val function = Function("transfer", listOf(Address(toAddress), Uint256(amount)), emptyList())
        return botWeb3.sendTransaction(wallet.credentials, token, FunctionEncoder.encode(function))
    }

    @kotlin.jvm.Throws
    fun transferAll(wallet: WalletManager.WalletIndexed, token: String, toAddress: String, botWeb3: BotWeb3): String? {
        val balance = balanceOf(wallet.credentials.address, token, botWeb3)
        return if (balance > BigInteger.ZERO) {
            transfer(wallet, token, balance, toAddress, botWeb3)
        } else {
            null
        }
    }

    @kotlin.jvm.Throws
    fun distributeToken(
        token: String,
        addresses: List<String>,
        values: List<BigInteger>,
        disperse: Disperse,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ): String {
        if (addresses.size != values.size) {
            throw Exception("Size not match")
        }
        val data = addresses.mapIndexed { index, value ->
            DisperseData(value, values[index])
        }
        return disperse.disperseToken(token, data, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeToken(
        token: String,
        addresses: List<String>,
        value: BigInteger,
        disperse: Disperse,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ): String {
        val data = addresses.map {
            DisperseData(it, value)
        }
        return disperse.disperseToken(token, data, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeTokenTarget(
        token: String, addresses: List<String>,
        value: BigInteger,
        disperse: Disperse,
        multiCall: MultiCall,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ): String {
        val disperseData = batchBalance(multiCall, addresses, token).mapIndexedNotNull { index, balance ->
            if (balance < value) {
                DisperseData(addresses[index], value - balance)
            } else {
                null
            }
        }
        disperseData.forEach {
            Utils.log("${it.address} ${Convert.fromWei(it.value.toBigDecimal(), Convert.Unit.ETHER)}")
        }
        return if (disperseData.isNotEmpty()) {
            disperse.disperseToken(token, disperseData, wallet, gasPrice)
        } else {
            ""
        }
    }

    @kotlin.jvm.Throws
    fun approve(token: String, approvedAddress: String, amount: BigInteger, wallet: WalletManager.WalletIndexed, botWeb3: BotWeb3): String {
        val payload = "0x095ea7b3" + zeroPad(approvedAddress.removePrefix("0x")) + zeroPad(amount.toString(16))
        return botWeb3.sendTransaction(wallet.credentials, token, payload)
    }

    @kotlin.jvm.Throws
    fun approveMax(token: String, approvedAddress: String, wallet: WalletManager.WalletIndexed, botWeb3: BotWeb3): String {
        val payload =
            "0x095ea7b3" + zeroPad(approvedAddress.removePrefix("0x")) + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        return botWeb3.sendTransaction(wallet.credentials, token, payload)
    }
}