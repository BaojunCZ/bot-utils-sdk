package contract

import BotWeb3
import WalletManager
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.utils.Convert
import utils.Utils
import java.math.BigInteger

data class DisperseData(val address: String, val value: BigInteger)

class Disperse(private val botWeb3: BotWeb3, var contract: String) {

    fun disperseEther(data: List<DisperseData>, wallet: WalletManager.WalletIndexed, gasPrice: BigInteger): String {
        val addresses = data.map {
            Address(it.address)
        }
        var totalValue = BigInteger.ZERO
        val values = data.map {
            totalValue += it.value
            Uint256(it.value)
        }
        val function = Function(
            "disperseEther",
            listOf(DynamicArray(Address::class.java, addresses), DynamicArray(Uint256::class.java, values)),
            listOf()
        )
        return botWeb3.sendTransaction(wallet.credentials, contract, FunctionEncoder.encode(function), totalValue)
    }

    fun disperseToken(
        token: String,
        data: List<DisperseData>,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger
    ): String {
        val addresses = data.map {
            Address(it.address)
        }
        val values = data.map {
            Uint256(it.value)
        }
        data.forEachIndexed { index, value ->
            Utils.log("${value.address} ${Convert.fromWei(value.value.toBigDecimal(), Convert.Unit.ETHER)}")
        }
        val function = Function(
            "disperseToken",
            listOf(Address(token), DynamicArray(Address::class.java, addresses), DynamicArray(Uint256::class.java, values)),
            listOf()
        )
        return botWeb3.sendTransaction(wallet.credentials, contract, FunctionEncoder.encode(function))
    }

}