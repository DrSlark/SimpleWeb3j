package com.tt.simpleweb3j

import android.app.Application
import com.tt.esayweb3j.EasyWeb3JGlobalConfig
import com.tt.esayweb3j.impl.EasyWalletCenter

class TTApplication : Application() {

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
            web3JUrl = "12"
        )
        EasyWeb3JGlobalConfig.init()
    }
}