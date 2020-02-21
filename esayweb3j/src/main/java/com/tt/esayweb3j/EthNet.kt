package net.vite.wallet.network.eth

import com.tt.esayweb3j.EthOkHttpClient
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
    @Volatile
    private var currentUrl: String? = null

    private var web3: Web3j? = null
    private fun getWeb3j() = web3!!

    fun config(baseUrl: String) {
        currentUrl = baseUrl
        web3 = Web3j.build(HttpService(currentUrl, EthOkHttpClient.create()))
    }


    fun getTokenBalance(accAddr: String, contractAddr: String = ""): BigInteger? {
        return try {
            if (contractAddr != "") {
                ERC20.load(
                    contractAddr,
                    getWeb3j(),
                    ReadonlyTransactionManager(getWeb3j(), accAddr),
                    DefaultGasProvider()
                ).balanceOf(accAddr).send()
            } else {
                getWeb3j().ethGetBalance(accAddr, DefaultBlockParameterName.LATEST).send().balance
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getGasPrice(): BigInteger? {
        return try {
            getWeb3j().ethGasPrice().send().gasPrice
        } catch (e: Exception) {
            null
        }
    }

    data class EthSendParams(
        val toAddr: String,
        val amount: BigInteger,
        val gasPrice: BigInteger,
        val gasLimit: BigInteger,
        val privateKey: String,
        val erc20ContractAddr: String?
    )

    data class EthSignResult(
        val txHash: String,
        val address: String,
        val pubKey: String
    )


    @Throws(IOException::class)
    fun getNonce(address: String): BigInteger {
        val ethGetTransactionCount = getWeb3j().ethGetTransactionCount(
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
            RawTransactionManager(getWeb3j(), Credentials.create(params.privateKey))
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
            RawTransactionManager(getWeb3j(), Credentials.create(params.privateKey))
        return rawTransaction.sendTransaction(
            params.gasPrice,
            params.gasLimit,
            params.toAddr,
            "",
            params.amount
        )
    }


}