package com.tt.esayweb3j.impl

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class SafeBox(val dir: File, val alias: String) {

    private data class EncryptParams(
        val name: String,
        val cdata: String,
        val iv: String,
        val time: Long = System.currentTimeMillis()
    )

    private fun filename(name: String): String {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(
            dir.absolutePath, Numeric.toHexStringNoPrefix(
                Hash.sha256(name.toByteArray())
            )
        ).absolutePath
    }



    fun storeBelow23(key: String, value: ByteArray) = kotlin.runCatching {
        File(filename(key)).writeText(String(value))
    }.isSuccess

    @TargetApi(23)
    fun store23(key: String, value: ByteArray): Boolean {
        return kotlin.runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, aesKey())
            val cipherBytes = cipher.doFinal(value)

            val json = Gson().toJson(
                EncryptParams(
                    "AES/GCM/NoPadding",
                    Numeric.toHexString(cipherBytes), Numeric.toHexString(cipher.iv)
                )
            )
            File(filename(key)).writeText(json)
        }.isSuccess
    }

    fun store(key: String, value: ByteArray): Boolean {
        return if (Build.VERSION.SDK_INT < 23) {
            storeBelow23(key, value)
        } else {
            store23(key, value)
        }
    }

    @TargetApi(23)
    private fun aesKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return if (!ks.containsAlias(alias)) {
            with(KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")) {
                init(
                    KeyGenParameterSpec
                        .Builder(
                            alias,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                        .apply {
                            setKeySize(256)
                            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        }.build()
                )
                generateKey()
            }
        } else {
            val entry = ks.getEntry(alias, null) as KeyStore.SecretKeyEntry
            entry.secretKey
        }
    }


    fun getBelow23(key: String) = kotlin.runCatching {
        File(filename(key)).readText().toByteArray()
    }.getOrNull()

    @TargetApi(23)
    fun get23(key: String): ByteArray? {
        return kotlin.runCatching {
            val cipherJson = File(filename(key)).readText()
            val encryptParams = Gson().fromJson(cipherJson, EncryptParams::class.java)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                aesKey(),
                GCMParameterSpec(128, Numeric.hexStringToByteArray(encryptParams.iv))
            )

            val plainBytes = cipher.doFinal(Numeric.hexStringToByteArray(encryptParams.cdata))
            plainBytes
        }.getOrNull()
    }


    fun get(key: String): ByteArray? {
        return if (Build.VERSION.SDK_INT < 23) {
            getBelow23(key)
        } else {
            get23(key)
        }
    }

    fun delete(key: String) {
        kotlin.runCatching {
            File(filename(key)).delete()
        }
    }
}
