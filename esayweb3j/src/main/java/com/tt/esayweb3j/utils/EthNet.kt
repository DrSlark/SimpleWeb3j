package com.tt.esayweb3j.utils

import io.reactivex.Flowable
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.Transfer
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.io.IOException
import java.math.BigInteger


object EthNet {
    private var currentUrl: String? = null

    private lateinit var web3j: Web3j

    fun config(baseUrl: String) {
        currentUrl = baseUrl
        web3j = Web3j.build(HttpService(currentUrl, EthOkHttpClient.create()))
    }

    fun getEthBalance(ethAddr: String): BigInteger {
        return web3j.ethGetBalance(ethAddr, DefaultBlockParameterName.PENDING).send().balance
    }

    fun getErc20TokenDecimals(contractAddr: String): Int {
        return ERC20.load(
            contractAddr,
            web3j,
            ReadonlyTransactionManager(web3j, contractAddr),
            DefaultGasProvider()
        ).decimals().send().toInt()
    }

    fun getErc20TokenBalance(ethAddr: String, contractAddr: String): BigInteger {
        return ERC20.load(
            contractAddr,
            web3j,
            ReadonlyTransactionManager(web3j, ethAddr),
            DefaultGasProvider()
        ).balanceOf(ethAddr).send()
    }

    fun getGasPrice() = web3j.ethGasPrice().send().gasPrice

    @Throws(IOException::class)
    fun getNonce(
        address: String
    ): BigInteger {
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            address, DefaultBlockParameterName.PENDING
        ).send()

        return ethGetTransactionCount.transactionCount
    }

    fun sendErc20Tx(
        params: EthSendParams
    ): Flowable<TransactionReceipt> {
        return ERC20.load(
            params.erc20ContractAddr,
            web3j,
            RawTransactionManager(web3j, params.credentials),
            StaticGasProvider(params.gasPrice, params.gasLimit)
        ).transfer(params.toAddr, params.amount).flowable()
    }

    fun sendEth(
        params: EthSendParams
    ): Flowable<TransactionReceipt> {
        return Transfer.sendFunds(
            web3j,
            params.credentials,
            params.toAddr,
            params.amount.toBigDecimal(),
            Convert.Unit.WEI,
            params.gasPrice,
            params.gasLimit
        ).flowable()
    }

    data class EthSendParams(
        val toAddr: String,
        val amount: BigInteger,
        val gasPrice: BigInteger,
        val gasLimit: BigInteger,
        val credentials: Credentials,
        val erc20ContractAddr: String?
    )


}