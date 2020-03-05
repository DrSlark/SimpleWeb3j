package com.tt.esayweb3j.impl

import com.tt.esayweb3j.*
import org.web3j.crypto.EasyBip44WalletUtils
import org.web3j.crypto.MnemonicUtils
import java.io.File


object EasyWalletCenter {

    // 这里有所有解锁的钱包 具体用哪一个可以在外面找个变量存一下
    private val unlockedWallets = mutableMapOf<String, EasyWalletProfile>()
    private val nameToWalletMap = mutableMapOf<String, EasyWalletProfile>()
    private var walletBaseDirPath: String = ""

    fun getUnlockedWallet(walletName: String) = unlockedWallets[walletName]

    fun init() {
        walletBaseDirPath =
            EasyWeb3JGlobalConfig.walletBaseDirPath
        val f = File(walletBaseDirPath)
        if (f.isFile) {
            f.delete()
        }
        f.mkdirs()
    }

    fun listAllWalletProfile(): List<EasyWalletProfile> {
        return nameToWalletMap.values.toMutableList().apply {
            sortByDescending { it.createTime }
        }
    }

    fun listAllWalletNames(): List<String> {
        return listAllWalletProfile().map { it.name }
    }

    fun loadAllWallet() {
        kotlin.runCatching {
            val dir = File(walletBaseDirPath)
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    kotlin.runCatching {
                        val readText = file.readText()
                        val ap = gson.fromJson(readText, EasyWalletProfile::class.java)
                        nameToWalletMap[ap.name] = ap
                    }
                }
            } else {
                dir.delete()
            }
        }
    }

    fun deleteWallet(name: String) {
        val walletProfile =
            nameToWalletMap[name] ?: throw EasyWalletException(EasyWalletErrCode.WALLET_NOT_EXIST)
        File(walletBaseDirPath, walletProfile.walletFileName).delete()
        File(walletBaseDirPath, walletProfile.defaultEthAddress()).delete()
        nameToWalletMap.remove(name)
        unlockedWallets.remove(name)
    }

    fun generate(name: String, password: String): EasyWalletProfile {
        if (nameToWalletMap.containsKey(name)) {
            throw EasyWalletException(EasyWalletErrCode.WALLET_NAME_DUPLICATED)
        }
        val generateBip44Wallet =
            EasyBip44WalletUtils.generateBip44Wallet(password, File(walletBaseDirPath))

        return EasyWalletProfile.create(name, generateBip44Wallet).also {
            saveEasyWalletProfile(it)
            unlockedWallets[name] = it
        }
    }

    fun unlock(name: String, password: String): EasyWalletProfile {
        val walletProfile =
            nameToWalletMap[name] ?: throw EasyWalletException(EasyWalletErrCode.WALLET_NOT_EXIST)

        val walletFile = File(walletBaseDirPath, walletProfile.walletFileName)
        val easyBip44Wallet = kotlin.runCatching {
            EasyBip44WalletUtils.loadEasyBip44Wallet(password, walletFile)
        }.getOrElse {
            throw EasyWalletException(
                EasyWalletErrCode.PASSWORD_ERROR,
                "loadEasyBip44Wallet error",
                it
            )
        }

        val newProfile = walletProfile.copy(easyBip44Wallet = easyBip44Wallet)
        unlockedWallets[name] = newProfile
        return newProfile
    }

    fun lock(name: String) {
        unlockedWallets.remove(name)
    }

    fun changeName(oldName: String, newName: String) {
        val walletProfile =
            nameToWalletMap[oldName]
                ?: throw EasyWalletException(EasyWalletErrCode.WALLET_NOT_EXIST)
        val newProfile = walletProfile.copy(name = newName)
        saveEasyWalletProfile(newProfile)
    }

    fun importMnemonic(mnemonic: String, name: String, password: String): EasyWalletProfile {
        if (!MnemonicUtils.validateMnemonic(mnemonic)) {
            throw MnemonicInvalidException()
        }

        if (nameToWalletMap.containsKey(name)) {
            throw EasyWalletException(EasyWalletErrCode.WALLET_NAME_DUPLICATED)
        }

        val newDefaultEthAddr = EasyBip44WalletUtils.mnemonicToDefaultEthAddr(mnemonic)

        nameToWalletMap.values.find { it.defaultEthAddress() == newDefaultEthAddr }?.let {
            throw WalletMnemonicException(it.name)
        }

        val generateBip44Wallet =
            EasyBip44WalletUtils.recoverBip44Wallet(mnemonic, password, File(walletBaseDirPath))

        return EasyWalletProfile.create(name, generateBip44Wallet).also {
            saveEasyWalletProfile(it)
            unlockedWallets[name] = it
        }
    }

    private fun saveEasyWalletProfile(profile: EasyWalletProfile) {
        val expectWalletFile = File(walletBaseDirPath, profile.defaultEthAddress())
        expectWalletFile.parentFile?.mkdirs()
        expectWalletFile.writeText(gson.toJson(profile))
        nameToWalletMap[profile.name] = profile
    }


}