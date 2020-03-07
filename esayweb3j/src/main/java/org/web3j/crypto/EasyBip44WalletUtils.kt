package org.web3j.crypto

import com.tt.esayweb3j.Bip44PathInvalidException
import com.tt.esayweb3j.MnemonicInvalidException
import com.tt.esayweb3j.impl.EasyWalletProfile
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import java.io.File
import java.io.IOException

data class EasyBip44Wallet(
    val filename: String,
    val mnemonic: String
) {
    val defaultEthCredentials = Credentials.create(
        EasyBip44WalletUtils.generateBip44KeyPair(
            mnemonic
        )
    )

    fun getCredentials(bip44Path: String) =
        Credentials.create(
            EasyBip44WalletUtils.generateBip44KeyPair(
                mnemonic,
                bip44Path
            )
        )

}


object EasyBip44WalletUtils : WalletUtils() {

    @Throws(CipherException::class, IOException::class)
    fun generateBip44Wallet(password: String, destinationDirectory: File): EasyBip44Wallet {
        val initialEntropy = ByteArray(16)
        SecureRandomUtils.secureRandom().nextBytes(initialEntropy)

        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)

        val bip44Keypair = generateBip44KeyPair(mnemonic)

        val walletFile =
            generateWalletFile(password, initialEntropy, bip44Keypair, destinationDirectory)

        return EasyBip44Wallet(
            filename = walletFile,
            mnemonic = mnemonic
        )
    }

    fun mnemonicToDefaultEthAddr(mnemonic: String): String {
        if (!MnemonicUtils.validateMnemonic(mnemonic)) {
            throw MnemonicInvalidException()
        }
        val bip44Keypair = generateBip44KeyPair(mnemonic)
        return Credentials.create(bip44Keypair).address
    }

    fun recoverBip44Wallet(
        mnemonic: String,
        password: String,
        destinationDirectory: File
    ): EasyBip44Wallet {
        val entropy = kotlin.runCatching { MnemonicUtils.generateEntropy(mnemonic) }
            .getOrElse { throw  MnemonicInvalidException() }

        val bip44Keypair = generateBip44KeyPair(mnemonic)

        val walletFile =
            generateWalletFile(password, entropy, bip44Keypair, destinationDirectory)

        return EasyBip44Wallet(
            filename = walletFile,
            mnemonic = mnemonic
        )
    }

    fun loadEasyBip44Wallet(password: String, walletFile: File): EasyBip44Wallet {
        val walletFileObj = objectMapper.readValue<WalletFile>(walletFile, WalletFile::class.java)
        val initialEntropy = Wallet.decryptToPlainBytes(password, walletFileObj)
        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        return EasyBip44Wallet(
            filename = walletFile.name,
            mnemonic = mnemonic
        )
    }

    @Throws(CipherException::class, IOException::class)
    private fun generateWalletFile(
        password: String, entropy: ByteArray, ecKeyPair: ECKeyPair, destinationDirectory: File
    ): String {
        val walletFile = Wallet.createMnemonicEntropyLight(password, entropy, ecKeyPair)
        val fileName = getWalletFileName(walletFile)
        val destination = File(destinationDirectory, fileName)
        objectMapper.writeValue(destination, walletFile)
        return fileName
    }

    fun generateBip44KeyPair(
        validMnemonic: String,
        path: String = EasyWalletProfile.EthBip44CommunityDefault
    ): Bip32ECKeyPair {
        val seed = MnemonicUtils.generateSeed(validMnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val pathArray = bip44PathToIntArrayPath(path)
        return Bip32ECKeyPair.deriveKeyPair(masterKeypair, pathArray)
    }

    fun bip44PathToIntArrayPath(path: String): IntArray {
        return kotlin.runCatching {
            val result = ArrayList<Int>()
            val pathSeg = path.split('/')
            for (i in 1 until pathSeg.size) {
                result.add(
                    if (pathSeg[i].endsWith("'")) {
                        pathSeg[i].substring(0, pathSeg[i].length - 1).toInt() or HARDENED_BIT
                    } else {
                        pathSeg[i].toInt()
                    }
                )
            }
            result.toIntArray()
        }.getOrElse { throw  Bip44PathInvalidException(it) }
    }
}