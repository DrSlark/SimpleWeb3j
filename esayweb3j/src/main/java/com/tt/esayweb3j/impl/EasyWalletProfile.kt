package com.tt.esayweb3j.impl

import com.google.gson.annotations.Expose
import org.web3j.crypto.EasyBip44Wallet
import org.web3j.crypto.Hash

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
