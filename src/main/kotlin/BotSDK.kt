import contract.Disperse
import contract.MultiCall
import utils.ERC20Utils
import utils.ERC721Utils
import utils.ETHUtils
import java.math.BigInteger

open class BotSDK(rpc: String, mnemonic: String, walletSize: Int) {

    val MultiCallContract = "0x1Ee38d535d541c55C9dae27B12edf090C608E6Fb"
    val DisperseContract = "0xD152f549545093347A162Dce210e7293f1452150"

    val botWeb3 = BotWeb3(rpc)
    val walletManager = WalletManager(mnemonic, walletSize)

    val multiCall = MultiCall(botWeb3, MultiCallContract)
    val disperse = Disperse(botWeb3, DisperseContract)

    fun setDefaultGasPrice(gasPrice: BigInteger) {
        botWeb3.defaultGasPrice = gasPrice
    }

    fun setMultiCallContract(address: String) {
        multiCall.contract = address
    }

    fun setDisperseContract(address: String) {
        disperse.contract = address
    }

    @kotlin.jvm.Throws
    fun batchEthBalance(): List<BigInteger> {
        return ETHUtils.batchBalance(multiCall, walletManager.getAllWalletAddresses())
    }

    @kotlin.jvm.Throws
    fun batchEthBalance(addresses: List<String>): List<BigInteger> {
        return ETHUtils.batchBalance(multiCall, addresses)
    }

    @kotlin.jvm.Throws
    fun batchErc20Balance(token: String): List<BigInteger> {
        return ERC20Utils.batchBalance(multiCall, walletManager.getAllWalletAddresses(), token)
    }

    @kotlin.jvm.Throws
    fun batchErc20Balance(addresses: List<String>, token: String): List<BigInteger> {
        return ERC20Utils.batchBalance(multiCall, addresses, token)
    }

    @kotlin.jvm.Throws
    fun distributeEth(
        addresses: List<String>,
        value: BigInteger,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger = botWeb3.defaultGasPrice
    ): String {
        return ETHUtils.distributeETH(addresses, value, disperse, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeEthTarget(
        addresses: List<String>,
        value: BigInteger,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger = botWeb3.defaultGasPrice
    ): String {
        return ETHUtils.distributeETHTarget(addresses, value, disperse, multiCall, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeErc20(
        token: String,
        addresses: List<String>,
        value: BigInteger,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger = botWeb3.defaultGasPrice
    ): String {
        return ERC20Utils.distributeToken(token, addresses, value, disperse, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun distributeErc20Target(
        token: String,
        addresses: List<String>,
        value: BigInteger,
        wallet: WalletManager.WalletIndexed,
        gasPrice: BigInteger = botWeb3.defaultGasPrice
    ): String {
        return ERC20Utils.distributeTokenTarget(token, addresses, value, disperse, multiCall, wallet, gasPrice)
    }

    @kotlin.jvm.Throws
    fun batchERC721Balance(token: String): List<BigInteger> {
        return ERC721Utils.batchBalance(token, walletManager.getAllWalletAddresses(), multiCall)
    }

    @kotlin.jvm.Throws
    fun batchERC721Balance(token: String, addresses: List<String>): List<BigInteger> {
        return ERC721Utils.batchBalance(token, addresses, multiCall)
    }

    @kotlin.jvm.Throws
    fun collectAllErc721(token: String, targetAddress: String) {
        ERC721Utils.collectAll(token, walletManager.getAllWalletWithoutFirst(), multiCall, targetAddress)
    }


    @kotlin.jvm.Throws
    fun collectAllErc721(token: String, wallets: List<WalletManager.WalletIndexed>, targetAddress: String) {
        ERC721Utils.collectAll(token, wallets, multiCall, targetAddress)
    }

}