package utils

import BotWeb3
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
}