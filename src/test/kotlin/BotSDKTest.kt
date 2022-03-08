import ConstantsTest.MboxTokenContract
import org.junit.jupiter.api.Test
import org.web3j.utils.Convert
import utils.Utils
import java.math.BigDecimal
import java.math.BigInteger

class BotSDKTest {

    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 520)

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

    @Test
    fun testDistributeEth() {
        val wallets = botSDK.walletManager.getPartWallets(520, 522)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeEth(wallets.map { it.credentials.address }, value, botSDK.walletManager.getFirstWallet())
        Utils.log(hash)
    }

    @Test
    fun testDistributeEthTarget() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeEthTarget(wallets.map { it.credentials.address }, value, botSDK.walletManager.getFirstWallet())
        Utils.log(hash)
    }

    @Test
    fun testDistributeErc20() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeErc20(
            ConstantsTest.MboxTokenContract,
            wallets.map { it.credentials.address },
            value,
            botSDK.walletManager.getFirstWallet()
        )
        Utils.log(hash)
    }

    @Test
    fun testDistributeErc20Target() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.7"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeErc20Target(
            ConstantsTest.MboxTokenContract,
            wallets.map { it.credentials.address },
            value,
            botSDK.walletManager.getFirstWallet()
        )
        Utils.log(hash)
    }

    @Test
    fun testBatchERC721Balance() {
        val addresses = botSDK.walletManager.getAllWalletAddresses()
        val balances = botSDK.batchERC721Balance(ConstantsTest.ElfinNFTContract, addresses)
        balances.forEachIndexed { index, balance ->
            Utils.log("${addresses[index]} $balance")
        }
    }

    @Test
    fun testCollectAllErc721() {
        botSDK.collectAllErc721(ConstantsTest.ElfinNFTContract, botSDK.walletManager.getFirstWallet().credentials.address)
    }

    @Test
    fun testErc20Approve() {
        val hash = botSDK.approveErc20(MboxTokenContract, BigInteger("10000000000000"), botSDK.walletManager.getFirstWallet())
        Utils.log(hash)
    }

    @Test
    fun testErc20ApproveMax() {
        val hash = botSDK.approveMaxErc20(MboxTokenContract, botSDK.walletManager.getFirstWallet())
        Utils.log(hash)
    }
}