package com.tt.esayweb3j

import com.tt.esayweb3j.impl.EasyWalletCenter
import com.tt.esayweb3j.impl.EthNet
import com.tt.esayweb3j.impl.TokenBalanceCache

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

    fun init() {
        TokenBalanceCache.init()
        EthNet.init()
        EasyWalletCenter.init()
    }

}