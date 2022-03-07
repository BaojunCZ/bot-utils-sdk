package contract

import BotWeb3
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import java.math.BigInteger

data class MultiCallData(val address: String, val data: String)

class MultiCall(private val botWeb3: BotWeb3, var contract: String) {

    @kotlin.jvm.Throws
    fun aggregate(calls: List<MultiCallData>): List<DynamicBytes> {
        try {
            val dataList = calls.map {
                DynamicStruct(Address(it.address), DynamicBytes(Hex.decode(it.data.substring(2).toByteArray())))
            }
            val aggregateFunction = Function(
                "aggregate",
                listOf(DynamicArray(DynamicArray::class.java, dataList)),
                listOf(object : TypeReference<Uint256?>() {}, object : TypeReference<DynamicArray<DynamicBytes?>?>() {})
            )
            val encodeFunctionDataOfMultiCall = FunctionEncoder.encode(aggregateFunction)
            val ethCallTransaction: Transaction =
                Transaction.createEthCallTransaction(calls[0].address, contract, encodeFunctionDataOfMultiCall)
            val response = botWeb3.web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send()
            val result = FunctionReturnDecoder.decode(response.value, aggregateFunction.outputParameters)
            return result[1].value as List<DynamicBytes>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    @kotlin.jvm.Throws
    fun getEthBalance(address: String): BigInteger {
        val function = Function(
            "getEthBalance",
            listOf(Address(address)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        val encoding = FunctionEncoder.encode(function)
        val ethCallTransaction: Transaction =
            Transaction.createEthCallTransaction(address, contract, encoding)
        val response = botWeb3.web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send()
        val result = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        return (result[0] as Uint256).value
    }

}