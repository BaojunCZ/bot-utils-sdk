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
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Convert
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
            BigInteger(it.value)
        }
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
}