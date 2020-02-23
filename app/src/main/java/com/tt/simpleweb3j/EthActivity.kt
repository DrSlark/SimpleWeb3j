package com.tt.simpleweb3j

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tt.esayweb3j.EthTokenBalanceInfo
import com.tt.esayweb3j.SingleEasyWallet
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_eth.*
import org.web3j.utils.Convert
import java.util.*

class EthActivity : AppCompatActivity() {

    fun appendText(str: String) {
        walletInfo.append("$str \n")
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eth)

        appendText("Name: ${SingleEasyWallet.unlockedWallet?.name}\n")
        appendText("EthAddr: ${SingleEasyWallet.unlockedWallet?.defaultEthAddress}\n")
        appendText("Mnemonic: ${SingleEasyWallet.unlockedWallet?.easyBip44Wallet?.mnemonic}")


        Observable.fromCallable {
            SingleEasyWallet.getGasPrice()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            appendText(
                "Gas $it WEI == ${Convert.fromWei(it.toBigDecimal(), Convert.Unit.ETHER)} ETH"
            )
        }, {
            appendText("Gas Error ${it.message}")
            it.printStackTrace()
        })

        SingleEasyWallet
            .getTokenBalance(EthTokenBalanceInfo.ETH_CONTRACT_ADDR)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                balanceInfo.text = tokenBalanceInfoStr(it)
            }, {
                balanceInfo.text = "getBalanceError ${it.message}"
                it.printStackTrace()
            })


    }

    override fun onStart() {
        super.onStart()
        SingleEasyWallet.startPollToken(EthTokenBalanceInfo.ETH_CONTRACT_ADDR, ::onBalanceGet)
        // 这个 是正式环境的usdt 只是作为演示使用
        SingleEasyWallet.startPollToken(
            "0xdac17f958d2ee523a2206206994597c13d831ec7",
            ::onBalanceGet
        )
    }

    override fun onStop() {
        super.onStop()
        SingleEasyWallet.endPollToken(EthTokenBalanceInfo.ETH_CONTRACT_ADDR)
        SingleEasyWallet.endPollToken("0xdac17f958d2ee523a2206206994597c13d831ec7")
    }

    @SuppressLint("SetTextI18n")
    fun onBalanceGet(balance: EthTokenBalanceInfo, isFromNet: Boolean) {
        runOnUiThread {
            pollBalanceInfo.text = "isFromNet: $isFromNet\n" + tokenBalanceInfoStr(balance)
        }

    }

    private fun tokenBalanceInfoStr(balance: EthTokenBalanceInfo): String {
        return "\ncontractAddr: ${balance.contractAddr}\n" +
                "decimals: ${balance.decimals}\n" +
                "balanceInSmallestUnit: ${balance.balanceInSmallestUnit}\n" +
                "BaseUnit:${balance.balanceSmallestUnitToBaseUnit().toPlainString()}\n"
    }


}
