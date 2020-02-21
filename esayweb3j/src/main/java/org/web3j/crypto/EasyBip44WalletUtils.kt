package org.web3j.crypto

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

    private fun generateBip44KeyPair(master: Bip32ECKeyPair): Bip32ECKeyPair {
        // Although Eth official bip44 path is "m/44'/60'/0'/0/"
        // but in most of apps they use "m/44'/60'/0'/0/0"
        val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
        return Bip32ECKeyPair.deriveKeyPair(master, path)
    }


}