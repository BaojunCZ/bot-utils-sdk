import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun formatTime(data: Date): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return df.format(data)
    }

    fun logWallet(wallet: WalletManager.WalletIndexed, log: String) {
        println(formatTime(Date(System.currentTimeMillis())) + " ${wallet.index} ${wallet.credentials.address} $log")
    }

    fun log(log: String) {
        println(formatTime(Date(System.currentTimeMillis())) + " $log")
    }

    fun Double.keep2Decimals(): Double {
        return this.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun zeroPad(data: String, length: Int = 64): String {
        var result = data.removePrefix("0x")
        while (result.length < length) {
            result = "0$result"
        }
        return result
    }
}
