import ConstantsTest.ElfinNFTContract
import ConstantsTest.MboxTokenContract
import ConstantsTest.MomoBoxContract
import org.junit.jupiter.api.Test
import org.web3j.utils.Convert
import utils.Utils
import java.math.BigDecimal
import java.math.BigInteger

class BotSDKTest {

    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 520)

    @Test
    fun testBatchEthBalance() {
        val wallets = botSDK.walletManager.allWallet
        botSDK.batchEthBalance(wallets.map { it.credentials.address })
            .forEachIndexed { index, balance ->
                Utils.logWallet(wallets[index], "${Convert.fromWei(balance.toBigDecimal(), Convert.Unit.ETHER)}")
            }
    }

    @Test
    fun testBatchErc20Balance() {
        val wallets = botSDK.walletManager.allWallet
        botSDK.batchErc20Balance(wallets.map { it.credentials.address }, MboxTokenContract)
            .forEachIndexed { index, balance ->
                Utils.logWallet(wallets[index], "${Convert.fromWei(balance.toBigDecimal(), Convert.Unit.ETHER)}")
            }
    }

    @Test
    fun testDistributeEth() {
        val wallets = botSDK.walletManager.getPartWallets(520, 522)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeEth(wallets.map { it.credentials.address }, value, botSDK.walletManager.firstWallet)
        Utils.log(hash)
    }

    @Test
    fun testDistributeEthTarget() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeEthTarget(wallets.map { it.credentials.address }, value, botSDK.walletManager.firstWallet)
        Utils.log(hash)
    }

    @Test
    fun testDistributeErc20() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.01"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeErc20(
            MboxTokenContract,
            wallets.map { it.credentials.address },
            value,
            botSDK.walletManager.firstWallet
        )
        Utils.log(hash)
    }

    @Test
    fun testDistributeErc20Target() {
        val wallets = botSDK.walletManager.getPartWallets(1, 3)
        val value = Convert.toWei(BigDecimal("0.7"), Convert.Unit.ETHER).toBigInteger()
        val hash = botSDK.distributeErc20Target(
            MboxTokenContract,
            wallets.map { it.credentials.address },
            value,
            botSDK.walletManager.firstWallet
        )
        Utils.log(hash)
    }

    @Test
    fun testBatchERC721Balance() {
        val addresses = botSDK.walletManager.allWalletAddresses
        val balances = botSDK.batchERC721Balance(ConstantsTest.ElfinNFTContract, addresses)
        balances.forEachIndexed { index, balance ->
            Utils.log("${addresses[index]} $balance")
        }
    }

    @Test
    fun testCollectAllErc721() {
        botSDK.collectAllErc721(ConstantsTest.ElfinNFTContract, botSDK.walletManager.firstWallet.credentials.address)
    }

    @Test
    fun testErc20Approve() {
        val hash = botSDK.approveErc20(MboxTokenContract, ElfinNFTContract, BigInteger("10000000000000"), botSDK.walletManager.firstWallet)
        Utils.log(hash)
    }

    @Test
    fun testErc20ApproveMax() {
        val hash = botSDK.approveMaxErc20(MboxTokenContract, ElfinNFTContract, botSDK.walletManager.firstWallet)
        Utils.log(hash)
    }

    @Test
    fun testBatchErc1155Balance() {
        val wallets = listOf(botSDK.walletManager.getWallet(46))
        botSDK.batchErc1155Balance(MomoBoxContract, "1", wallets.map { it.credentials.address }).forEachIndexed { index, balance ->
            Utils.logWallet(botSDK.walletManager.getWallet(index), balance.toString())
        }
    }

    @Test
    fun testCollectErc1155() {
        val wallets = botSDK.walletManager.getPartWallets(274, 275)
        botSDK.collectErc1155(MomoBoxContract, "1", wallets, botSDK.walletManager.firstWallet.credentials.address)
    }

    @Test
    fun testCollectAddress() {
        Utils.log(botSDK.walletManager.collectAddress)
        botSDK.walletManager.collectIndex = 1
        Utils.log(botSDK.walletManager.collectAddress)
    }

    @Test
    fun testTransferERC20All() {
        botSDK.transferErc20All(
            botSDK.walletManager.getWallet(2),
            MboxTokenContract,
            botSDK.walletManager.collectAddress,
            botSDK.botWeb3
        )?.let {
            Utils.log(it)
            botSDK.botWeb3.observeTx(it).thenAccept {
                Utils.log(it.toString())
            }
        } ?: kotlin.run {
            Utils.log("没有余额")
        }
    }

    @Test
    fun testTransferETHAll() {
        botSDK.transferETHAll(botSDK.walletManager.getWallet(2), botSDK.walletManager.collectAddress, botSDK.botWeb3)?.let {
            Utils.log(it)
        }
    }
}