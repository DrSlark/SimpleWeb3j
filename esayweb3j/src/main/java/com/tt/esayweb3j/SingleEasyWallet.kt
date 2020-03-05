package com.tt.esayweb3j

import com.tt.esayweb3j.impl.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

object SingleEasyWallet {

    private val balancePollMap = ConcurrentHashMap<String, TokenBalancePoll>()

    @Volatile
    var unlockedWallet: EasyWalletProfile? = null
        private set

    fun listAllWalletNames() = EasyWalletCenter.listAllWalletNames()

    fun unlock(name: String, password: String): EasyWalletProfile {
        return (EasyWalletCenter.getUnlockedWallet(name)
            ?: EasyWalletCenter.unlock(name, password)).also {
            unlockedWallet = it
        }
    }

    fun lock(name: String? = null) {
        val lockName = name ?: unlockedWallet?.name ?: return
        EasyWalletCenter.lock(lockName)
        if (lockName == unlockedWallet?.name) {
            unlockedWallet = null
        }
    }

    fun generate(name: String, password: String): EasyWalletProfile {
        return EasyWalletCenter.generate(name, password).also {
            unlockedWallet = it
        }
    }

    // 如果传空 就删除当前钱包，也可以指定删除其它钱包
    fun deleteWallet(name: String? = null) {
        val deleteName = name ?: unlockedWallet?.name ?: return
        EasyWalletCenter.deleteWallet(deleteName)
        if (deleteName == unlockedWallet?.name) {
            unlockedWallet = null
        }
    }

    /**
     *
     * @param tokenAddr
     * @param onBalanceGetFunc
     * @param pollEthAddr 传空 就轮询当前登录的地址余额，不然就是轮询指定的地址
     */
    fun startPollToken(
        tokenAddr: String,
        onBalanceGetFunc: OnBalanceGetFunc,
        pollEthAddr: String? = null
    ) {
        val ethAddr = pollEthAddr ?: unlockedWallet?.defaultEthAddress ?: return
        if (balancePollMap.contains("$ethAddr$tokenAddr")) return
        balancePollMap["$ethAddr$tokenAddr"] = TokenBalancePoll(
            ethAddr = ethAddr,
            subscribeToken = tokenAddr,
            onBalanceGetFunc = onBalanceGetFunc
        ).also { it.start() }
    }

    /**
     * @param tokenAddr
     * @param pollEthAddr 传空 就结束轮询当前登录的地址余额，不然就是结束轮询指定的地址
     */
    fun endPollToken(
        tokenAddr: String,
        pollEthAddr: String? = null
    ) {
        val ethAddr = pollEthAddr ?: unlockedWallet?.defaultEthAddress ?: return
        balancePollMap["$ethAddr$tokenAddr"]?.let {
            it.end()
            balancePollMap.remove("$ethAddr$tokenAddr")
        }
    }

    /**
     *
     * @param tokenAddr
     * @param ethAddr 传空 就轮询当前登录的地址余额，不然就是轮询指定的地址
     * @param isDoubleResponse true表示一次从缓存中返回 一次从网络返回
     * @return
     */
    fun getTokenBalance(
        tokenAddr: String,
        ethAddr: String? = null,
        isDoubleResponse: Boolean = false
    ): Observable<EthTokenBalanceInfo> {
        val realEthAddr = ethAddr ?: unlockedWallet?.defaultEthAddress ?: return Observable.error(
            Exception("empty eth addr")
        )
        return BehaviorSubject.create<EthTokenBalanceInfo> { emitter ->
            val cache = TokenBalanceCache.getBalanceInfo(realEthAddr, tokenAddr)
            if (cache != null) {
                emitter.onNext(cache)
            }
            if (isDoubleResponse) {
                val tokenBalanceInfo = EthNet.getTokenBalance(realEthAddr, tokenAddr)
                TokenBalanceCache.addBalanceInfo(realEthAddr, tokenBalanceInfo)
                emitter.onNext(tokenBalanceInfo)
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     *
     * @param toAddr 发给谁
     * @param erc20ContractAddr 哪个币
     * @param amount 多少钱 注意这里都是最小精度比如 1USDT 的话 这里应该传 100_000_00 单位转化用EthTokenBalanceInfo里面的 方法
     * @param gasPrice 通过 #getGasPrice() 获得一个推荐值 也可以让用户自己填
     * @param gasLimit 暂时写死60000 也可以改
     * @return 这笔交易的状态 根据产品需求去展示吧
     */
    fun sendErc20Tx(
        toAddr: String,
        erc20ContractAddr: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        val credentials = unlockedWallet?.easyBip44Wallet?.defaultEthCredentials
            ?: return Flowable.error(EasyWalletException(EasyWalletErrCode.LOCKED))

        return EthNet.sendErc20Tx(
            toAddr,
            erc20ContractAddr,
            amount,
            credentials,
            gasPrice,
            gasLimit
        ).subscribeOn(Schedulers.io())
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
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        val credentials = unlockedWallet?.easyBip44Wallet?.defaultEthCredentials
            ?: return Flowable.error(EasyWalletException(EasyWalletErrCode.LOCKED))

        return EthNet.sendEth(
            toAddr,
            amount,
            credentials,
            gasPrice,
            gasLimit
        ).subscribeOn(Schedulers.io())
    }


    /**
     * 获取推荐的 GAS费用 注意线程
     *
     */
    fun getGasPrice() = EthNet.getGasPrice()


}