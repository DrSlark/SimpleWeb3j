package com.tt.esayweb3j.impl

import com.tt.esayweb3j.EthTokenBalanceInfo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

typealias OnBalanceGetFunc = (info: EthTokenBalanceInfo, isFromNet: Boolean) -> Unit

class TokenBalancePoll(
    val ethAddr: String,
    val subscribeToken: String,
    val onBalanceGetFunc: OnBalanceGetFunc = { _, _ -> },
    val pollIntervalSeconds: Long = 5L
) {

    private var pillDispose: Disposable? = null

    fun start() {
        pillDispose = Observable.fromCallable {
            val tokenBalance = TokenBalanceCache.getBalanceInfo(ethAddr, subscribeToken)?.also {
                onBalanceGetFunc(it, true)
            }
            kotlin.runCatching {
                val ethTokenBalanceInfo =
                    EthNet.getTokenBalance(ethAddr, subscribeToken, tokenBalance?.decimals)

                TokenBalanceCache.addBalanceInfo(ethAddr, ethTokenBalanceInfo)

                onBalanceGetFunc(ethTokenBalanceInfo, true)

            }
        }.subscribeOn(Schedulers.io())
            .repeatWhen { it.delay(pollIntervalSeconds, TimeUnit.SECONDS) }
            .retryWhen { it.delay(pollIntervalSeconds, TimeUnit.SECONDS) }
            .subscribe({}, {})

    }

    fun end() {
        pillDispose?.dispose()
    }

}