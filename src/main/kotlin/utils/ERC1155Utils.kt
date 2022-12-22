package utils

import BotWeb3
import WalletManager
import contract.MultiCall
import contract.MultiCallData
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

object ERC1155Utils {
    @kotlin.jvm.Throws
    fun balanceOf(token: String, address: String, id: String, botWeb3: BotWeb3): BigInteger? {
        val function = Function(
            "balanceOf",
            listOf<Type<*>>(Address(address), Uint256(id.toBigInteger())),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        val result = botWeb3.ethCall(function, address, token)
        return (result[0] as Uint256).value
    }

    @kotlin.jvm.Throws
    fun batchBalance(token: String, id: String, addresses: List<String>, multiCall: MultiCall): List<BigInteger> {
        val calls = addresses.map {
            val function = Function(
                "balanceOf",
                listOf<Type<*>>(Address(it), Uint256(id.toBigInteger())),
                listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
            )
            MultiCallData(token, FunctionEncoder.encode(function))
        }
        return multiCall.aggregate(calls).map {
            it?.let {
                BigInteger(it.value)
            } ?: BigInteger.ZERO
        }
    }

    @kotlin.jvm.Throws
    fun safeTransferFrom(
        token: String,
        from: String,
        to: String,
        id: String,
        amount: BigInteger,
        botWeb3: BotWeb3,
        wallet: WalletManager.WalletIndexed
    )
            : String {
        val function = Function(
            "safeTransferFrom",
            listOf<Type<*>>(Address(from), Address(to), Uint256(id.toBigInteger()), Uint256(amount), DynamicBytes("0x".toByteArray())),
            listOf()
        )
        return botWeb3.sendTransaction(wallet.credentials, token, FunctionEncoder.encode(function))
    }

    @kotlin.jvm.Throws
    fun safeTransferFromAll(
        token: String,
        to: String,
        id: String,
        botWeb3: BotWeb3,
        wallet: WalletManager.WalletIndexed
    ): String? {
        return balanceOf(token, wallet.credentials.address, id, botWeb3)?.let {
            if (it > BigInteger.ZERO) {
                safeTransferFrom(token, wallet.credentials.address, to, id, it, botWeb3, wallet)
            } else {
                null
            }
        }
    }

    @kotlin.jvm.Throws
    fun collectAll(token: String, to: String, id: String, wallets: List<WalletManager.WalletIndexed>, botWeb3: BotWeb3) {
        wallets.forEach {
            safeTransferFromAll(token, to, id, botWeb3, it)?.let { hash ->
                Utils.logWallet(it, hash)
            }
        }
    }
}