import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.MnemonicUtils

class WalletManager(mnemonic: String, private val walletSize: Int, var collectIndex: Int = 0) {

    private val seed: ByteArray = MnemonicUtils.generateSeed(mnemonic, "")
    private val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)

    val collectWallet: WalletIndexed
        get() = getWallet(collectIndex)

    val collectAddress: String
        get() = collectWallet.credentials.address

    val firstWallet: WalletIndexed
        get() = getWallet(0)

    val allWalletWithoutFirst: List<WalletIndexed>
        get() {
            val wallets = mutableListOf<WalletIndexed>()
            for (i in 1 until walletSize) {
                wallets.add(getWallet(i))
            }
            return wallets
        }

    val allWallet: List<WalletIndexed>
        get() {
            val wallets = mutableListOf<WalletIndexed>()
            for (i in 0 until walletSize) {
                wallets.add(getWallet(i))
            }
            return wallets
        }

    val allWalletAddresses: List<String>
        get() {
            return allWallet.map { it.credentials.address }
        }

    fun getWallet(index: Int): WalletIndexed {
        val path = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0,
            index
        )
        val privateKey = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path).privateKey
        return WalletIndexed(Credentials.create(ECKeyPair.create(privateKey)), index)
    }

    fun getPartWallets(start: Int): List<WalletIndexed> {
        val wallets = mutableListOf<WalletIndexed>()
        for (i in start until walletSize) {
            wallets.add(getWallet(i))
        }
        return wallets
    }

    fun getPartWallets(from: Int, to: Int): List<WalletIndexed> {
        val wallets = mutableListOf<WalletIndexed>()
        for (i in from until to) {
            wallets.add(getWallet(i))
        }
        return wallets
    }

    data class WalletIndexed(val credentials: Credentials, val index: Int)

}