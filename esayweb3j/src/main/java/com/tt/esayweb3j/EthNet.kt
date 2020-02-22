package com.tt.esayweb3j

import com.tt.esayweb3j.utils.EthOkHttpClient
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
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
    ): EthSendTransaction? {
        val f = Function(
            "transfer",
            arrayListOf(Address(params.toAddr), Uint256(params.amount)) as List<Type<Any>>,
            mutableListOf(TypeReference.create(Bool::class.java)) as List<TypeReference<*>>?
        )

        val rawTransactionManager =
            RawTransactionManager(web3j, Credentials.create(params.privateKey))
        return rawTransactionManager.sendTransaction(
            params.gasPrice,
            params.gasLimit,
            params.erc20ContractAddr,
            FunctionEncoder.encode(f),
            BigInteger.ZERO
        )
    }

    fun sendEth(
        params: EthSendParams
    ): EthSendTransaction? {
        val rawTransaction =
            RawTransactionManager(web3j, Credentials.create(params.privateKey))
        return rawTransaction.sendTransaction(
            params.gasPrice,
            params.gasLimit,
            params.toAddr,
            "",
            params.amount
        )
    }

    data class EthSendParams(
        val toAddr: String,
        val amount: BigInteger,
        val gasPrice: BigInteger,
        val gasLimit: BigInteger,
        val privateKey: String,
        val erc20ContractAddr: String?
    )


}