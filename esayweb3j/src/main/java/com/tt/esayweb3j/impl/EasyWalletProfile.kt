package com.tt.esayweb3j.impl

import com.google.gson.annotations.Expose
import com.tt.esayweb3j.EasyWalletErrCode
import com.tt.esayweb3j.EasyWalletException
import org.web3j.crypto.*
import org.web3j.utils.Numeric

data class Bip44PathAddr(
    val path: String,
    val address: String,
    // 也许你们要给每个地址取名字呢？
    val nickName: String = ""
) {
    fun getCredentials(mnemonic: String) =
        Credentials.create(
            EasyBip44WalletUtils.generateBip44KeyPair(
                mnemonic,
                path
            )
        )
}


data class EasyWalletProfile(
    var name: String,
    val walletFileName: String,
    val createTime: Long,
    val bip44Paths: ArrayList<Bip44PathAddr>,
    @Expose(serialize = false, deserialize = false)
    val easyBip44Wallet: EasyBip44Wallet? = null
) {

    companion object {
        // Although Eth official bip44 path is "m/44'/60'/0'/0/" but in most of apps they use "m/44'/60'/0'/0/0"
        const val EthBip44CommunityDefault = "m/44'/60'/0'/0/0"
        const val EthBip44CommunityPrefix = "m/44'/60'/0'/0/%d"

        fun create(name: String, easyBip44Wallet: EasyBip44Wallet) = EasyWalletProfile(
            name = name,
            walletFileName = easyBip44Wallet.filename,
            createTime = System.currentTimeMillis(),
            bip44Paths = arrayListOf(
                Bip44PathAddr(
                    EthBip44CommunityDefault,
                    easyBip44Wallet.defaultEthCredentials.address
                )
            ),
            easyBip44Wallet = easyBip44Wallet
        )
    }

    fun accessToken(): String? {
        val mnemonic =
            easyBip44Wallet?.mnemonic ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)
        return Numeric.toHexStringNoPrefix(
            Hash.sha256(mnemonic.toByteArray())
        )
    }

    fun defaultEthAddress() = bip44Paths[0].address

    fun addBip44Path(path: String) {
        easyBip44Wallet ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)
        bip44Paths.find { it.path == path }?.let {
            // already exist do nothing
            return
        }
        bip44Paths.add(Bip44PathAddr(path, easyBip44Wallet.getCredentials(path).address))
    }
}
