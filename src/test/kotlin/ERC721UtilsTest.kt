import org.junit.jupiter.api.Test
import utils.ERC721Utils
import java.math.BigInteger

class ERC721UtilsTest {

    private val botSDK = BotSDK(ConstantsTest.RPC, Mnemonic, 1)

    @Test
    fun testBatchTokenURI() {
        ERC721Utils.batchTokenURI(
            "0x4B5C4b2bE2fe1656F8eFEdd27393c61A7357b6E7", listOf(BigInteger.ZERO, 7648.toBigInteger(), 9448.toBigInteger()), botSDK
                .multiCall
        ).forEach {
            it?.let {
                println(it)
            } ?: println()
        }

    }

    @Test
    fun testTokenURI() {
        val uri = ERC721Utils.tokenURI(botSDK.botWeb3, "0x4B5C4b2bE2fe1656F8eFEdd27393c61A7357b6E7", 7648.toBigInteger())
        println(uri)
    }

}