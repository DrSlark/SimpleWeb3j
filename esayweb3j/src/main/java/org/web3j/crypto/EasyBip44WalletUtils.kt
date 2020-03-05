package org.web3j.crypto

import com.tt.esayweb3j.Bip44PathInvalidException
import com.tt.esayweb3j.MnemonicInvalidException
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import java.io.File
import java.io.IOException

data class EasyBip44Wallet(
    val filename: String,
    val mnemonic: String,
    val ethCredentials: Credentials
)



object EasyBip44WalletUtils : WalletUtils() {

    @Throws(CipherException::class, IOException::class)
    fun generateBip44Wallet(password: String, destinationDirectory: File): EasyBip44Wallet {
        val initialEntropy = ByteArray(16)
        SecureRandomUtils.secureRandom().nextBytes(initialEntropy)

        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = generateBip44KeyPair(masterKeypair)

        val walletFile =
            generateWalletFile(password, initialEntropy, bip44Keypair, destinationDirectory)

        return EasyBip44Wallet(
            filename = walletFile,
            mnemonic = mnemonic,
            ethCredentials = Credentials.create(bip44Keypair)
        )
    }

    fun mnemonicToDefaultEthAddr(mnemonic: String): String {
        if (!MnemonicUtils.validateMnemonic(mnemonic)) {
            throw MnemonicInvalidException()
        }
        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = generateBip44KeyPair(masterKeypair)
        return Credentials.create(bip44Keypair).address
    }

    fun recoverBip44Wallet(
        mnemonic: String,
        password: String,
        destinationDirectory: File
    ): EasyBip44Wallet {
        val entropy = kotlin.runCatching { MnemonicUtils.generateEntropy(mnemonic) }
            .getOrElse { throw  MnemonicInvalidException() }

        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = generateBip44KeyPair(masterKeypair)

        val walletFile =
            generateWalletFile(password, entropy, bip44Keypair, destinationDirectory)

        return EasyBip44Wallet(
            filename = walletFile,
            mnemonic = mnemonic,
            ethCredentials = Credentials.create(bip44Keypair)
        )
    }


    fun loadEasyBip44Wallet(password: String, walletFile: File): EasyBip44Wallet {
        val walletFileObj = objectMapper.readValue<WalletFile>(walletFile, WalletFile::class.java)
        val initialEntropy = Wallet.decryptToPlainBytes(password, walletFileObj)

        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = generateBip44KeyPair(masterKeypair)

        return EasyBip44Wallet(
            filename = walletFile.name,
            mnemonic = mnemonic,
            ethCredentials = Credentials.create(bip44Keypair)
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
        master: Bip32ECKeyPair,
        path: String = "m/44'/60'/0'/0/0"
    ): Bip32ECKeyPair {
        // Although Eth official bip44 path is "m/44'/60'/0'/0/"
        // but in most of apps they use "m/44'/60'/0'/0/0"
//        val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
        val pathArray = bip44PathToIntArrayPath(path)
        return Bip32ECKeyPair.deriveKeyPair(master, pathArray)
    }

    fun bip44PathToIntArrayPath(path: String): IntArray {
        return kotlin.runCatching {
            val result = ArrayList<Int>()
            val pathSeg = path.split('/')
            for (i in 1 until path.length) {
                result.add(
                    if (pathSeg[i].endsWith("'")) {
                        pathSeg[i].substring(0, pathSeg.size - 1).toInt() or HARDENED_BIT
                    } else {
                        pathSeg[i].toInt()
                    }
                )
            }
            result.toIntArray()
        }.getOrElse { throw  Bip44PathInvalidException() }
    }
}