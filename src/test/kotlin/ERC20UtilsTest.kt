import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Test
import utils.ERC20Utils
import utils.Utils

class ERC20UtilsTest {
    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 520)

    @Test
    fun testDecimals() {
        val decimal = ERC20Utils.decimals(ConstantsTest.MboxTokenContract, BotWeb3(ConstantsTest.RPC))
        Utils.log(decimal.toString())
    }

    @Test
    fun testBalance() {
        val balance = ERC20Utils.balanceOf(botSDK.walletManager.getWallet(3).credentials.address, ConstantsTest.MboxTokenContract, botSDK.botWeb3)
        Utils.log(balance.toString())
    }

    @Test
    fun testTransferAll() {
        ERC20Utils.transferAll(
            botSDK.walletManager.getWallet(1),
            ConstantsTest.MboxTokenContract,
            botSDK.walletManager.collectAddress,
            botSDK.botWeb3
        )?.let {
            botSDK.botWeb3.observeTx(it).thenAccept {
                Utils.log(it.toString())
            }
        } ?: kotlin.run {
            Utils.log("没有余额")
        }
    }

    @Test
    fun testObserve() {
        botSDK.botWeb3.observeTx("0x924d9869dD13cD4d0B1B0734EE2Bc045994AFDc6").thenAccept {
            Utils.log(it.toString())
        }
    }

}