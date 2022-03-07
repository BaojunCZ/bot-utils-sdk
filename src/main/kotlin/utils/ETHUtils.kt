package utils

import WalletManager
import contract.Disperse
import contract.DisperseData
import contract.MultiCall
import contract.MultiCallData
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

object ETHUtils {

    @kotlin.jvm.Throws
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

    @kotlin.jvm.Throws
    fun distributeETH(
        addresses: List<String>, value: BigInteger, disperse: Disperse, wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ):
            String {
        val data = addresses.map {
            DisperseData(it, value)
        }
        return disperse.disperseEther(data, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeETHTarget(
        addresses: List<String>,
        value: BigInteger,
        disperse: Disperse,
        multiCall: MultiCall,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ): String {
        val disperseData = batchBalance(multiCall, addresses).mapIndexedNotNull { index, balance ->
            if (balance < value) {
                DisperseData(addresses[index], value - balance)
            } else {
                null
            }
        }
        return if (disperseData.isNotEmpty()) {
            disperse.disperseEther(disperseData, wallet, gasPrice)
        } else {
            ""
        }
    }

}