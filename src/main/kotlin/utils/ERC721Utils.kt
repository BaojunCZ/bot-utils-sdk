package utils

import BotWeb3
import WalletManager
import contract.MultiCall
import contract.MultiCallData
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

object ERC721Utils {

    @kotlin.jvm.Throws
    fun batchBalance(token: String, addresses: List<String>, multiCall: MultiCall): List<BigInteger> {
        val calls = addresses.map {
            val function = Function(
                "balanceOf",
                listOf<Type<*>>(Address(it)),
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
    fun batchTokenURI(token: String, ids: List<BigInteger>, multiCall: MultiCall): List<String?> {
        val calls = ids.map {
            val function = Function(
                "tokenURI",
                listOf<Type<*>>(Uint256(it)),
                listOf<TypeReference<*>>(object : TypeReference<Utf8String?>() {})
            )
            MultiCallData(token, FunctionEncoder.encode(function))
        }
        return multiCall.aggregate(calls).map {
            if (it != null) {
                String(it.value)
            } else {
                ""
            }
        }
    }

    fun tokenURI(botWeb3: BotWeb3, token: String, id: BigInteger): String {
        val function = Function(
            "tokenURI",
            listOf<Type<*>>(
                Uint256(id)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String?>() {})
        )
        val result = botWeb3.ethCall(function, "0x924d9869dd13cd4d0b1b0734ee2bc045994afdc6", token)
        return (result[0] as Utf8String).value
    }

    @kotlin.jvm.Throws
    fun collectAll(token: String, wallets: List<WalletManager.WalletIndexed>, multiCall: MultiCall, target: String) {
        batchBalance(token, wallets.map { it.credentials.address }, multiCall)
            .map { it.toInt() }
            .forEachIndexed { index, balance ->
                val wallet = wallets[index]
                for (i in 0 until balance) {
                    val id = tokenOfOwnerByIndex(multiCall.botWeb3, token, wallet.credentials.address, i.toBigInteger())
                    val function = Function(
                        "safeTransferFrom",
                        listOf<Type<*>>(
                            Address(wallet.credentials.address),
                            Address(target),
                            Uint256(id)
                        ), emptyList()
                    )
                    val payload = FunctionEncoder.encode(function)
                    val hash = multiCall.botWeb3.sendTransaction(wallet.credentials, token, payload)
                    Utils.logWallet(wallet, hash)
                }
            }
    }

    @kotlin.jvm.Throws
    fun tokenOfOwnerByIndex(botWeb3: BotWeb3, token: String, owner: String, index: BigInteger): BigInteger {
        val function = Function(
            "tokenOfOwnerByIndex",
            listOf<Type<*>>(
                Address(owner),
                Uint256(index)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        val result = botWeb3.ethCall(function, owner, token)
        return (result[0] as Uint256).value
    }

}