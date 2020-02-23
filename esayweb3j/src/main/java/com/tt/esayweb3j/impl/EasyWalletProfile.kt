package com.tt.esayweb3j.impl

import com.google.gson.annotations.Expose
import org.web3j.crypto.EasyBip44Wallet
import org.web3j.crypto.Hash

/**
 * 1 生成助记词 `
2 生成 1 个地址`
3 发erc20
4 发eth
5 获取交易列表
6 轮询余额
7 状态轮询
8 动态获取当前gas
 */
data class EasyWalletProfile(
    var name: String,
    val walletFileName: String,
    val defaultEthAddress: String,
    @Expose(serialize = false, deserialize = false)
    val easyBip44Wallet: EasyBip44Wallet? = null
) {
    companion object {
        fun getFileName(name: String) = Hash.sha3String(name)
    }
}
