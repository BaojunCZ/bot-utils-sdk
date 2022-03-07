import contract.MultiCall
import org.junit.jupiter.api.Test;

class MultiCallTest {
    val walletManager = WalletManager(Mnemonic, 520)
    val botWeb3 = BotWeb3(ConstantsTest.RPC)
    val multiCall = MultiCall(botWeb3, ConstantsTest.MultiCallContract)

    @Test
    fun getEthBalance() {
        val balance = multiCall.getEthBalance(walletManager.getFirstWallet())
        Utils.log(balance.toString())
    }

}