package com.tt.esayweb3j.impl

import com.tt.esayweb3j.EasyWalletErrCode
import com.tt.esayweb3j.EasyWalletException
import com.tt.esayweb3j.EasyWeb3JGlobalConfig
import org.web3j.crypto.EasyBip44WalletUtils
import java.io.File


object EasyWalletCenter {
    private data class WalletsMeta(
        val walletNames: List<String>
    )

    // 这里有所有解锁的钱包 具体用哪一个可以在外面找个变量存一下
    private val unlockedWallets = mutableMapOf<String, EasyWalletProfile>()
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

    fun listAllWalletNames(): List<String> {
        return kotlin.runCatching {
            val walletsMetaFile = File(walletBaseDirPath, "meta")
            val walletsMete =
                gson.fromJson<WalletsMeta>(walletsMetaFile.readText(), WalletsMeta::class.java)
            walletsMete.walletNames
        }.getOrDefault(emptyList())
    }

    fun deleteWallet(name: String) {
        val walletsMetaFile = File(walletBaseDirPath, "meta")
        val walletsMete =
            gson.fromJson<WalletsMeta>(walletsMetaFile.readText(), WalletsMeta::class.java)

        val newWalletsMeta =
            walletsMete.copy(walletNames = walletsMete.walletNames.filter { it != name })
        walletsMetaFile.writeText(gson.toJson(newWalletsMeta))


        val expectWalletFile = File(
            walletBaseDirPath,
            EasyWalletProfile.getFileName(name)
        )
        val profile =
            kotlin.runCatching {
                gson.fromJson(expectWalletFile.reader(), EasyWalletProfile::class.java)
            }.getOrElse {
                throw EasyWalletException(
                    EasyWalletErrCode.OTHER,
                    "deserialize EasyWalletProfile error",
                    it
                )
            }

        val walletFile = File(walletBaseDirPath, profile.walletFileName)
        walletFile.delete()
        expectWalletFile.delete()

    }

    fun generate(name: String, password: String): EasyWalletProfile {
        val expectWalletFile = File(
            walletBaseDirPath,
            EasyWalletProfile.getFileName(name)
        )
        if (expectWalletFile.exists()) {
            throw EasyWalletException(EasyWalletErrCode.WALLET_NOT_EXIST)
        }
        expectWalletFile.parentFile?.mkdirs()
        val generateBip44Wallet =
            EasyBip44WalletUtils.generateBip44Wallet(password, File(walletBaseDirPath))

        val walletsMetaFile = File(walletBaseDirPath, "meta")
        val walletsMete = runCatching {
            gson.fromJson<WalletsMeta>(walletsMetaFile.readText(), WalletsMeta::class.java)
        }.getOrElse { WalletsMeta(emptyList()) }
        walletsMetaFile.writeText(
            gson.toJson(
                WalletsMeta(
                    walletNames = mutableListOf(name).apply {
                        addAll(
                            walletsMete.walletNames
                        )
                    })
            )
        )

        return EasyWalletProfile(
            name = name,
            walletFileName = generateBip44Wallet.filename,
            defaultEthAddress = generateBip44Wallet.ethCredentials.address,
            easyBip44Wallet = generateBip44Wallet
        ).also { expectWalletFile.writeText(gson.toJson(it)) }
    }

    fun unlock(name: String, password: String): EasyWalletProfile {
        val expectWalletFile = File(
            walletBaseDirPath,
            EasyWalletProfile.getFileName(name)
        )
        if (!expectWalletFile.exists()) {
            throw EasyWalletException(EasyWalletErrCode.WALLET_NOT_EXIST)
        }

        val profile =
            kotlin.runCatching {
                gson.fromJson(expectWalletFile.reader(), EasyWalletProfile::class.java)
            }.getOrElse {
                throw EasyWalletException(
                    EasyWalletErrCode.OTHER,
                    "deserialize EasyWalletProfile error",
                    it
                )
            }

        val walletFile = File(walletBaseDirPath, profile.walletFileName)
        val easyBip44Wallet = kotlin.runCatching {
            EasyBip44WalletUtils.loadEasyBip44Wallet(password, walletFile)
        }.getOrElse {
            throw EasyWalletException(
                EasyWalletErrCode.PASSWORD_ERROR,
                "loadEasyBip44Wallet error",
                it
            )
        }

        val newProfile = profile.copy(easyBip44Wallet = easyBip44Wallet)
        unlockedWallets[name] = newProfile
        return newProfile
    }

    fun lock(name: String) {
        unlockedWallets.remove(
            EasyWalletProfile.getFileName(
                name
            )
        )
    }


}