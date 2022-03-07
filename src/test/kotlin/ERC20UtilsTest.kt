import org.junit.jupiter.api.Test
import utils.ERC20Utils
import utils.Utils

class ERC20UtilsTest {

    @Test
    fun testDecimals() {
        val decimal = ERC20Utils.decimals(ConstantsTest.MboxTokenContract, BotWeb3(ConstantsTest.RPC))
        Utils.log(decimal.toString())
    }

}