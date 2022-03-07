package utils

import contract.MultiCall
import contract.MultiCallData
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

object ETHUtils {

    fun batchBalance(multiCall: MultiCall, addresses: List<String>): List<BigInteger> {
        val calls = addresses.map {
            val ethBalanceFunction = Function(
                "getEthBalance",
                listOf(Address(it)),
                listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
            )
            MultiCallData(multiCall.contract, FunctionEncoder.encode(ethBalanceFunction))
        }
        val balances = multiCall.aggregate(calls)
        return balances.map {
            BigInteger(it.value)
        }
    }

}