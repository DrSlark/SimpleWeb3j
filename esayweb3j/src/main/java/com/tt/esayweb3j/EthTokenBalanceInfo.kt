package com.tt.esayweb3j

import java.math.BigDecimal

data class EthTokenBalanceInfo(
    val contractAddr: String,
    val decimals: Int,
    val balanceInSmallestUnit: String
) {

    fun isEth() = contractAddr == ETH_CONTRACT_ADDR

    companion object {
        // ETH本身不是合约，只是为了方便用同一的缓存结构去描述
        const val ETH_CONTRACT_ADDR = "ETH"

        fun balanceBaseUnitToSmallestUnit(amount: BigDecimal, decimals: Int) =
            amount.multiply(BigDecimal.TEN.pow(decimals)).toBigInteger()

        fun balanceBaseUnitToSmallestUnit(amount: String, decimals: Int) =
            amount.toBigDecimal().multiply(BigDecimal.TEN.pow(decimals)).toBigInteger()
    }

    fun balanceSmallestUnitToBaseUnit() =
        balanceInSmallestUnit.toBigDecimal().divide(BigDecimal.TEN.pow(decimals))
}