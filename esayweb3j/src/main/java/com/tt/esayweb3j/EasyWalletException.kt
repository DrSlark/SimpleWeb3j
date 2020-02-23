package com.tt.esayweb3j

enum class EasyWalletErrCode {
    WALLET_NOT_EXIST,
    WALLET_NAME_DUPLICATED,
    PASSWORD_ERROR,
    LOCKED,
    OTHER
}

class EasyWalletException(
    val code: EasyWalletErrCode,
    message: String? = "",
    cause: Throwable? = null
) :
    Exception(message, cause)