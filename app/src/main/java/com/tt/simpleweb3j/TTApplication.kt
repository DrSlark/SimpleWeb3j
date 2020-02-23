package com.tt.simpleweb3j

import android.annotation.SuppressLint
import android.app.Application
import com.tt.esayweb3j.EasyWeb3JGlobalConfig
import io.reactivex.android.schedulers.AndroidSchedulers

class TTApplication : Application() {

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        EasyWeb3JGlobalConfig.config(
            walletBaseDirPath = filesDir.absolutePath + if (BuildConfig.DEBUG) {
                "/debug"
            } else {
                "/release"
            },
            cacheDirPath = cacheDir.absolutePath + if (BuildConfig.DEBUG) {
                "/debug"
            } else {
                "/release"
            },
            web3JUrl = "Add your web3j url"
        )
        EasyWeb3JGlobalConfig.initLocal()
        EasyWeb3JGlobalConfig.initWeb3jService().observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showToast("init web3j success")
            }, {
                showToast("init web3j error ${it.message}")
            })
    }
}