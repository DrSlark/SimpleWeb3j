package com.tt.esayweb3j

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tt.esayweb3j.utils.EthNet
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

typealias OnBalanceGetFunc = (info: EthTokenBalanceInfo, isFromNet: Boolean) -> Unit

class BalancePollCenter(
    val myEthAddr: String,
    val cacheDir: String,
    val onBalanceGetFunc: OnBalanceGetFunc = { _, _ -> },
    val pollIntervalSeconds: Long = 5L
) {
    private val gson = Gson()

    private val balanceMap = mutableMapOf<String, EthTokenBalanceInfo>()
    private var pillDispose: Disposable? = null
    private val subscribeErcContracts = ArrayList<String>()

    fun getBalance(contractAddr: String) = balanceMap[contractAddr]

    fun subscribeErcContract(contractAddr: String) {
        subscribeErcContracts.add(contractAddr)
    }


    fun start() {
        pillDispose = Observable.fromCallable {
            kotlin.runCatching {
                val cacheStr = File(cacheDir, "$myEthAddr-balance").readText()
                val type = object : TypeToken<Map<String, EthTokenBalanceInfo>>() {}.type
                val cache =
                    gson.fromJson<Map<String, EthTokenBalanceInfo>>(cacheStr, type::class.java)
                cache.entries.forEach {
                    balanceMap[it.key] = it.value
                    onBalanceGetFunc(it.value, false)
                }
            }

            subscribeErcContracts.forEach { contractAddr ->
                kotlin.runCatching {
                    val ethTokenBalanceInfo =
                        if (contractAddr == EthTokenBalanceInfo.ETH_CONTRACT_ADDR) {
                            val balanceInSmallest =
                                EthNet.getEthBalance(myEthAddr).toString()
                            EthTokenBalanceInfo(
                                contractAddr,
                                18,
                                balanceInSmallest
                            )
                        } else {
                            val decimals =
                                balanceMap[contractAddr]?.decimals ?: EthNet.getErc20TokenDecimals(
                                    contractAddr
                                )

                            val balanceInSmallest =
                                EthNet.getErc20TokenBalance(myEthAddr, contractAddr).toString()

                            EthTokenBalanceInfo(
                                contractAddr,
                                decimals,
                                balanceInSmallest
                            )
                        }

                    onBalanceGetFunc(ethTokenBalanceInfo, true)

                    balanceMap[contractAddr] = ethTokenBalanceInfo
                    val jsonStr = gson.toJson(balanceMap)
                    val cacheDir = File(cacheDir).also { it.mkdirs() }
                    File(cacheDir, "$myEthAddr-balance").writeText(jsonStr)
                }
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