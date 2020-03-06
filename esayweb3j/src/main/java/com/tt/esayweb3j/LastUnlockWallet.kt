package com.tt.esayweb3j

data class LastUnlockWallet(
    var name: String,
    val password: String,
    var timestamp: Long = System.currentTimeMillis()
)