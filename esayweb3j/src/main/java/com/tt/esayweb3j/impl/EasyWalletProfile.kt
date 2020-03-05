package com.tt.esayweb3j.impl

import com.google.gson.annotations.Expose
import org.web3j.crypto.EasyBip44Wallet
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric

data class EasyWalletProfile(
    var name: String,
    val walletFileName: String,
    val defaultEthAddress: String,
    val createTime: Long,
    @Expose(serialize = false, deserialize = false)
    val easyBip44Wallet: EasyBip44Wallet? = null
) {
    companion object {
        fun getFileName(name: String) = Hash.sha3String(name)
    }

    fun accessToken(): String? {
        val mnemonic = easyBip44Wallet?.mnemonic ?: return null
        return Numeric.toHexStringNoPrefix(
            Hash.sha256(mnemonic.toByteArray())
        )
    }

}
