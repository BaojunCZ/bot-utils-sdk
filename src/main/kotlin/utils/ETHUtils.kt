package utils

import BotWeb3
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
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import java.math.BigDecimal
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
            it?.let {
                BigInteger(it.value)
            } ?: BigInteger.ZERO
        }
    }

    @kotlin.jvm.Throws
    fun balance(address: String, web3: BotWeb3): BigInteger {
        return web3.web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
    }

    @kotlin.jvm.Throws
    fun transferETH(wallet: WalletManager.WalletIndexed, to: String, amount: BigInteger, web3: BotWeb3): String {
        return web3.sendTransaction(wallet.credentials, web3.defaultGasPrice, to, "", amount)
    }

    @kotlin.jvm.Throws
    fun transferAll(wallet: WalletManager.WalletIndexed, to: String, web3: BotWeb3): String? {
        val balance = balance(wallet.credentials.address, web3)
        val fee = Convert.toWei(BigDecimal("0.000105"), Convert.Unit.ETHER).toBigInteger()
        return if (balance - fee > BigInteger.ZERO) {
            transferETH(wallet, to, balance - fee, web3)
        } else {
            null
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