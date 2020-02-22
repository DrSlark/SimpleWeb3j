package com.tt.simpleweb3j

import android.app.Application
import com.tt.esayweb3j.EasyWalletCenter

class TTApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        EasyWalletCenter.config(
            filesDir.absolutePath + if (BuildConfig.DEBUG) {
                "/debug"
            } else {
                "/release"
            }
        )
    }
}