package com.tt.esayweb3j

enum class EasyWalletErrCode {
    WALLET_NOT_EXIST,
    WALLET_NAME_DUPLICATED,
    WALLET_MNEMONIC_DUPLICATED,
    ADDRESS_NOT_FOUND,
    PASSWORD_ERROR,
    LOCKED,
    OTHER
}

open class EasyWalletException(
    val code: EasyWalletErrCode,
    message: String? = "",
    cause: Throwable? = null
) : Exception(message, cause)

class WalletMnemonicException(
    alreadyExistName: String,
    cause: Throwable? = null
) : EasyWalletException(EasyWalletErrCode.WALLET_MNEMONIC_DUPLICATED, alreadyExistName, cause)


class Bip44PathInvalidException(cause: Throwable?=null) : Exception(cause)
class MnemonicInvalidException(cause: Throwable?=null) : Exception(cause)