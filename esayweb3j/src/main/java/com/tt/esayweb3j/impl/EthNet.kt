package com.tt.esayweb3j.impl

import com.tt.esayweb3j.EasyWeb3JGlobalConfig
import com.tt.esayweb3j.EthTokenBalanceInfo
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
import java.math.BigInteger


object EthNet {

    private lateinit var web3j: Web3j

    fun init() {
        web3j = Web3j.build(HttpService(EasyWeb3JGlobalConfig.web3JUrl, EthOkHttpClient.create()))
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

    fun getNonce(
        address: String
    ): BigInteger {
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            address, DefaultBlockParameterName.PENDING
        ).send()

        return ethGetTransactionCount.transactionCount
    }

    /**
     *
     * @param toAddr 发给谁
     * @param erc20ContractAddr 哪个币
     * @param amount 多少钱 注意这里都是最小精度比如 1USDT 的话 这里应该传 100_000_00 单位转化用EthTokenBalanceInfo里面的 方法
     * @param credentials 解锁钱包后能拿到的
     * @param gasPrice 通过 #getGasPrice() 获得一个推荐值 也可以让用户自己填
     * @param gasLimit 暂时写死60000 也可以改
     * @return
     */
    fun sendErc20Tx(
        toAddr: String,
        erc20ContractAddr: String,
        amount: BigInteger,
        credentials: Credentials,
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        return ERC20.load(
            erc20ContractAddr,
            web3j,
            RawTransactionManager(web3j, credentials),
            StaticGasProvider(gasPrice, gasLimit)
        ).transfer(toAddr, amount).flowable()
    }

    /**
     *
     * @param toAddr
     * @param amount 这个单位是WEI 1 ETH = 1 *10^18WEI 单位转化用EthTokenBalanceInfo里面的 方法
     * @param credentials
     * @param gasPrice
     * @param gasLimit
     * @return
     */
    fun sendEth(
        toAddr: String,
        amount: BigInteger,
        credentials: Credentials,
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        return Transfer.sendFunds(
            web3j,
            credentials,
            toAddr,
            amount.toBigDecimal(),
            Convert.Unit.WEI,
            gasPrice,
            gasLimit
        ).flowable()
    }


    fun getTokenBalance(
        ethAddr: String,
        tokenAddr: String,
        knownDecimals: Int? = null
    ): EthTokenBalanceInfo {
        return if (tokenAddr == EthTokenBalanceInfo.ETH_CONTRACT_ADDR) {
            val balanceInSmallest = getEthBalance(ethAddr).toString()
            EthTokenBalanceInfo(
                tokenAddr,
                18,
                balanceInSmallest
            )
        } else {
            val decimals = knownDecimals ?: getErc20TokenDecimals(tokenAddr)

            val balanceInSmallest = getErc20TokenBalance(ethAddr, tokenAddr).toString()

            EthTokenBalanceInfo(
                tokenAddr,
                decimals,
                balanceInSmallest
            )
        }
    }

}