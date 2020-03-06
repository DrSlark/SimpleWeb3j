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


    private val safeBoxKey = "SingleEasyWalletAutoUnlockKey"

    private val lastUnlockWalletKey = "lastUnlockWallet"

    private val balancePollMap = ConcurrentHashMap<String, TokenBalancePoll>()

    @Volatile
    var unlockedWallet: EasyWalletProfile? = null
        private set

    fun loadAllWallet() = EasyWalletCenter.loadAllWallet()

    fun listAllWalletNames() = EasyWalletCenter.listAllWalletProfile().map { it.name }

    fun listAllWalletProfile() = EasyWalletCenter.listAllWalletProfile()

    fun getLastWalletName() = EasyWeb3JGlobalConfig.kvStore.getString(lastUnlockWalletKey, "")

    fun tryToAutoUnlock(): Boolean {
        val lastSession = EasyWeb3JGlobalConfig.safeBox.get(safeBoxKey)?.let {
            gson.fromJson(String(it), LastUnlockWallet::class.java)
        } ?: return false

        return kotlin.runCatching {
            unlock(lastSession.name, lastSession.password)
        }.isSuccess.also {
            if (!it) {
                EasyWeb3JGlobalConfig.safeBox.delete(safeBoxKey)
            }
        }
    }

    fun unlock(name: String, password: String): EasyWalletProfile {
        return (EasyWalletCenter.getUnlockedWallet(name)
            ?: EasyWalletCenter.unlock(name, password)).also {
            unlockedWallet = it
            EasyWeb3JGlobalConfig.kvStore.edit().putString(lastUnlockWalletKey, it.name).apply()
            EasyWeb3JGlobalConfig.safeBox.delete(safeBoxKey)
            EasyWeb3JGlobalConfig.safeBox.store(
                safeBoxKey,
                gson.toJson(
                    LastUnlockWallet(
                        name = it.name,
                        password = password
                    )
                ).toByteArray()
            )
        }
    }

    fun lock(name: String? = null) {
        val lockName = name ?: unlockedWallet?.name ?: return
        EasyWalletCenter.lock(lockName)
        if (lockName == unlockedWallet?.name) {
            unlockedWallet = null
            EasyWeb3JGlobalConfig.safeBox.delete(safeBoxKey)
        }
    }

    fun generate(name: String, password: String): EasyWalletProfile {
        return EasyWalletCenter.generate(name, password).also {
            unlock(name, password)
        }
    }

    fun recover(mnemonic: String, name: String, password: String): EasyWalletProfile {
        return EasyWalletCenter.recover(mnemonic, name, password).also {
            unlock(name, password)
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

    fun isNameExist(name: String) = EasyWalletCenter.nameToWalletMap.containsKey(name)

    fun changeName(oldName: String, newName: String) {
        EasyWalletCenter.changeName(oldName, newName)
        val lastSession = EasyWeb3JGlobalConfig.safeBox.get(safeBoxKey)?.let {
            gson.fromJson(String(it), LastUnlockWallet::class.java)
        } ?: return

        EasyWeb3JGlobalConfig.kvStore.edit().putString(lastUnlockWalletKey, newName).apply()
        EasyWeb3JGlobalConfig.safeBox.store(
            safeBoxKey, gson.toJson(
                lastSession.copy(name = newName)
            ).toByteArray()
        )
    }

    fun startPollToken(
        tokenAddr: String,
        onBalanceGetFunc: OnBalanceGetFunc,
        ethAddr: String
    ) {
        if (balancePollMap.contains("$ethAddr$tokenAddr")) return
        balancePollMap["$ethAddr$tokenAddr"] = TokenBalancePoll(
            ethAddr = ethAddr,
            subscribeToken = tokenAddr,
            onBalanceGetFunc = onBalanceGetFunc
        ).also { it.start() }
    }

    fun endPollToken(
        tokenAddr: String,
        ethAddr: String
    ) {
        balancePollMap["$ethAddr$tokenAddr"]?.let {
            it.end()
            balancePollMap.remove("$ethAddr$tokenAddr")
        }
    }

    /**
     *
     * @param tokenAddr
     * @param ethAddr
     * @param isDoubleResponse true表示一次从缓存中返回 一次从网络返回
     * @return
     */
    fun getTokenBalance(
        tokenAddr: String,
        ethAddr: String,
        isDoubleResponse: Boolean = false
    ): Observable<EthTokenBalanceInfo> {

        return BehaviorSubject.create<EthTokenBalanceInfo> { emitter ->
            val cache = TokenBalanceCache.getBalanceInfo(ethAddr, tokenAddr)
            if (cache != null) {
                emitter.onNext(cache)
            }
            if (isDoubleResponse) {
                val tokenBalanceInfo = EthNet.getTokenBalance(ethAddr, tokenAddr)
                TokenBalanceCache.addBalanceInfo(ethAddr, tokenBalanceInfo)
                emitter.onNext(tokenBalanceInfo)
            }
        }.subscribeOn(Schedulers.io())
    }

    fun getTokenBalanceInCache(
        tokenAddr: String,
        ethAddr: String
    ) = TokenBalanceCache.getBalanceInfo(ethAddr, tokenAddr)

    /**
     * @param myAddr: 你现在用的地址,
     * @param toAddr 发给谁
     * @param erc20ContractAddr 哪个币
     * @param amount 多少钱 注意这里都是最小精度比如 1USDT 的话 这里应该传 100_000_00 单位转化用EthTokenBalanceInfo里面的 方法
     * @param gasPrice 通过 #getGasPrice() 获得一个推荐值 也可以让用户自己填
     * @param gasLimit 暂时写死60000 也可以改
     * @return 这笔交易的状态 根据产品需求去展示吧
     */
    fun sendErc20Tx(
        myAddr: String,
        toAddr: String,
        erc20ContractAddr: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        val credentials = unlockedWallet?.getCredentialsByAddress(myAddr)
            ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)

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
        myAddr: String,
        toAddr: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger = 60000.toBigInteger()
    ): Flowable<TransactionReceipt> {
        val credentials = unlockedWallet?.getCredentialsByAddress(myAddr)
            ?: throw EasyWalletException(EasyWalletErrCode.LOCKED)

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