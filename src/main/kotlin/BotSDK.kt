import contract.MultiCall
import utils.ERC20Utils
import utils.ETHUtils
import java.math.BigInteger

class BotSDK(rpc: String, mnemonic: String, walletSize: Int) {

    val MultiCallContract = "0x1Ee38d535d541c55C9dae27B12edf090C608E6Fb"
    val DisperseContract = "0xD152f549545093347A162Dce210e7293f1452150"

    val botWeb3 = BotWeb3(rpc)
    val walletManager = WalletManager(mnemonic, walletSize)

    val multiCall = MultiCall(botWeb3, MultiCallContract)

    fun setMultiCallContract(address: String) {
        multiCall.contract = address
    }

    @kotlin.jvm.Throws
    fun batchEthBalance(addresses: List<String>): List<BigInteger> {
        return ETHUtils.batchBalance(multiCall, addresses)
    }

    @kotlin.jvm.Throws
    fun batchErc20Balance(addresses: List<String>, token: String): List<BigInteger> {
        return ERC20Utils.batchBalance(multiCall, addresses, token)
    }

}