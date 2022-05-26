import org.junit.jupiter.api.Test
import utils.ETHUtils
import utils.Utils

class ETHUtilsTest {
    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 520)

    @Test
    fun balance() {
        val balance = ETHUtils.balance(botSDK.walletManager.getWallet(2).credentials.address, botSDK.botWeb3)
        Utils.log(balance.toString())
    }

    @Test
    fun transferAll() {
        ETHUtils.transferAll(botSDK.walletManager.getWallet(1), botSDK.walletManager.collectAddress, botSDK.botWeb3)?.let {
            Utils.log(it)
        }
    }
}