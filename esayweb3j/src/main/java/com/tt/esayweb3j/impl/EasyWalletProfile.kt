package com.tt.esayweb3j.impl

import com.google.gson.annotations.Expose
import com.tt.esayweb3j.EasyWalletErrCode
import com.tt.esayweb3j.EasyWalletException
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.lang.Exception

data class Bip44DeriveProfile(
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

    fun getIndex(): Int {
        return path.substringAfterLast("/").toInt()
    }
}

class IllegalEthBip44CommunityPathException : Exception()

data class EasyWalletProfile(
    var name: String,
    val walletFileName: String,
    val createTime: Long,
    val ethBip44CommunityPaths: ArrayList<Bip44DeriveProfile>,
    // 暂时没用
    val ethNotBip44CommunityPaths: ArrayList<Bip44DeriveProfile> = ArrayList(),
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
            ethBip44CommunityPaths = arrayListOf(
                Bip44DeriveProfile(
                    EthBip44CommunityDefault,
                    easyBip44Wallet.defaultEthCredentials.address
                )
            ),
            easyBip44Wallet = easyBip44Wallet
        )
    }

    fun getCredentialsByAddress(address: String): Credentials {
        val mnemonic =
            easyBip44Wallet?.mnemonic ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)
        return (ethBip44CommunityPaths.find { it.address == address }
            ?: ethNotBip44CommunityPaths.find { it.address == address })?.getCredentials(mnemonic)
            ?: throw EasyWalletException(EasyWalletErrCode.ADDRESS_NOT_FOUND)
    }

    fun accessToken(): String? {
        val mnemonic =
            easyBip44Wallet?.mnemonic ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)
        return Numeric.toHexStringNoPrefix(
            Hash.sha256(mnemonic.toByteArray())
        )
    }

    fun defaultEthAddress() = ethBip44CommunityPaths[0].address

    fun addEthBip44CommunityPath(path: String): Bip44DeriveProfile {
        if (path.substringBeforeLast("/") != EthBip44CommunityPrefix.substringBeforeLast("/")) {
            throw IllegalEthBip44CommunityPathException()
        }
        easyBip44Wallet ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)
        ethBip44CommunityPaths.find { it.path == path }?.let {
            // already exist do nothing
            return it
        }
        return Bip44DeriveProfile(path, easyBip44Wallet.getCredentials(path).address).also {
            ethBip44CommunityPaths.add(it)
            ethBip44CommunityPaths.sortBy {
                it.getIndex()
            }
        }
    }

    fun easyAddCommunityAddress(index: Int? = null): Bip44DeriveProfile {
        val nowIndex =
            index ?: ethBip44CommunityPaths.last().getIndex() + 1
        return addEthBip44CommunityPath(String.format(EthBip44CommunityPrefix, nowIndex))
    }

    fun deleteCommunityBip44ByIndex(index: Int) {
        ethBip44CommunityPaths.removeIf { it.getIndex() == index }
    }

    fun deleteCommunityBip44ByAddress(address: String) {
        ethBip44CommunityPaths.removeIf { it.address == address }
    }

}
