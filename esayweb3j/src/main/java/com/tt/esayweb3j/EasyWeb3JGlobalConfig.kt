package com.tt.esayweb3j

import com.tt.esayweb3j.impl.EasyWalletCenter
import com.tt.esayweb3j.impl.EthNet
import com.tt.esayweb3j.impl.TokenBalanceCache
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

object EasyWeb3JGlobalConfig {

    lateinit var walletBaseDirPath: String
        private set
    lateinit var cacheDirPath: String
        private set

    lateinit var web3JUrl: String
        private set

    fun config(walletBaseDirPath: String, cacheDirPath: String, web3JUrl: String) {
        this.web3JUrl = web3JUrl
        this.walletBaseDirPath = walletBaseDirPath
        this.cacheDirPath = cacheDirPath
    }

    fun initLocal() {
        TokenBalanceCache.init()
        EasyWalletCenter.init()
    }

    fun initWeb3jService() = Observable.fromCallable {
        EthNet.init()
    }.subscribeOn(Schedulers.io())

}