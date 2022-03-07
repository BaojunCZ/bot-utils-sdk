import org.junit.jupiter.api.Test
import org.web3j.utils.Convert
import utils.Utils

class BotSDKTest {

    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 100)

    @Test
    fun testBatchEthBalance() {
        val wallets = botSDK.walletManager.getAllWallet()
        botSDK.batchEthBalance(wallets.map { it.credentials.address })
            .forEachIndexed { index, balance ->
                Utils.logWallet(wallets[index], "${Convert.fromWei(balance.toBigDecimal(), Convert.Unit.ETHER)}")
            }
    }

    @Test
    fun testBatchErc20Balance() {
        val wallets = botSDK.walletManager.getAllWallet()
        botSDK.batchErc20Balance(wallets.map { it.credentials.address }, ConstantsTest.MboxTokenContract)
            .forEachIndexed { index, balance ->
                Utils.logWallet(wallets[index], "${Convert.fromWei(balance.toBigDecimal(), Convert.Unit.ETHER)}")
            }
    }

}