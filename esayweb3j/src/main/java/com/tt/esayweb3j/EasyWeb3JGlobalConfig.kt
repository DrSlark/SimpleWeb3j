package com.tt.esayweb3j

import android.content.Context
import android.content.SharedPreferences
import com.tt.esayweb3j.impl.EasyWalletCenter
import com.tt.esayweb3j.impl.EthNet
import com.tt.esayweb3j.impl.SafeBox
import com.tt.esayweb3j.impl.TokenBalanceCache
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File

object EasyWeb3JGlobalConfig {

    lateinit var walletBaseDir: File
        private set
    lateinit var cacheDirDir: File
        private set

    lateinit var web3JUrl: String
        private set

    lateinit var context: Context
        private set

    lateinit var env: String
        private set

    lateinit var safeBox: SafeBox
        private set

    lateinit var kvStore: SharedPreferences
        private set

    fun config(
        context: Context,
        env: String,
        walletBaseDirPath: String,
        cacheDirPath: String,
        web3JUrl: String
    ) {
        this.context = context
        this.env = env
        this.web3JUrl = web3JUrl
        this.walletBaseDir = File(walletBaseDirPath, env)
        this.cacheDirDir = File(cacheDirPath, env)
        this.safeBox = SafeBox(File(walletBaseDirPath, "safeBox$env"), "xxx$env")
        this.kvStore = context.getSharedPreferences("kv$env", Context.MODE_PRIVATE)
    }

    fun initLocal() {
        TokenBalanceCache.init()
        EasyWalletCenter.init()
    }

    fun initWeb3jService() = Observable.fromCallable {
        EthNet.init()
    }.subscribeOn(Schedulers.io())

}