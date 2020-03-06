package com.tt.esayweb3j.impl

import com.tt.esayweb3j.EasyWeb3JGlobalConfig
import com.tt.esayweb3j.EthTokenBalanceInfo
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object TokenBalanceCache {

    fun init() {
        EasyWeb3JGlobalConfig.cacheDirDir.mkdirs()
    }

    val tokenBalance = ConcurrentHashMap<String, EthTokenBalanceInfo>()

    private fun getCacheFile(ethAddr: String, subscribeToken: String) =
        File(EasyWeb3JGlobalConfig.cacheDirDir, "$ethAddr$subscribeToken-balance")

    fun getBalanceInfo(ethAddr: String, subscribeToken: String): EthTokenBalanceInfo? {
        return tokenBalance["$ethAddr$subscribeToken"] ?: kotlin.runCatching {
            synchronized(TokenBalanceCache::class.java) {
                val cacheStr = getCacheFile(
                    ethAddr,
                    subscribeToken
                ).readText()
                gson.fromJson<EthTokenBalanceInfo>(cacheStr, EthTokenBalanceInfo::class.java)
            }

        }.getOrNull()?.also {
            tokenBalance["$ethAddr$subscribeToken"] = it
        }
    }

    fun addBalanceInfo(ethAddr: String, balanceInfo: EthTokenBalanceInfo) {
        tokenBalance["$ethAddr${balanceInfo.contractAddr}"] = balanceInfo
        kotlin.runCatching {
            synchronized(TokenBalanceCache::class.java) {
                getCacheFile(
                    ethAddr,
                    balanceInfo.contractAddr
                ).writeText(
                    gson.toJson(balanceInfo)
                )
            }
        }
    }

}