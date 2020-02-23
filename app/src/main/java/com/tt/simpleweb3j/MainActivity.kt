package com.tt.simpleweb3j

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.tt.esayweb3j.EasyWalletErrCode
import com.tt.esayweb3j.EasyWalletException
import com.tt.esayweb3j.SingleEasyWallet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

fun Disposable.addTo(c: CompositeDisposable): Boolean {
    return c.add(this)
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}


class MainActivity : AppCompatActivity() {
    val gson = Gson()

    private val disposable = CompositeDisposable()

    fun refresh() {
        Observable.fromCallable {
            SingleEasyWallet.listAllWalletNames()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            walletNames.text = it.takeIf { it.isNotEmpty() }?.reduce { acc, s ->
                "$s\n$acc"
            }
        }.addTo(disposable)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh()


        create.setOnClickListener {
            val walletNameStr = walletName.editText?.editableText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }
            val passwordStr = password.editText?.editableText?.toString() ?: kotlin.run {
                // 一般>8位
                showToast("empty password")
                return@setOnClickListener
            }

            Observable.fromCallable {
                SingleEasyWallet.generate(name = walletNameStr, password = passwordStr)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    refresh()
                    walletDetails.append("${it.easyBip44Wallet?.mnemonic}\n\n${gson.toJson(it)}\n\n\n")
                }, {
                    if (it is EasyWalletException && it.code == EasyWalletErrCode.WALLET_NAME_DUPLICATED) {
                        showToast("$walletNameStr 已经存在")
                    } else {
                        showToast("发生未知错误 ${it.message}")
                        it.printStackTrace()
                    }
                }).addTo(disposable)

        }


        unlock.setOnClickListener {
            val walletNameStr = walletName.editText?.editableText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }
            val passwordStr = password.editText?.editableText?.toString() ?: kotlin.run {
                showToast("empty password")
                return@setOnClickListener
            }

            Observable.fromCallable {
                SingleEasyWallet.unlock(name = walletNameStr, password = passwordStr)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    walletDetails.append(gson.toJson(it) + "\n\n\n")
                }, {
                    if (it is EasyWalletException) {
                        when (it.code) {
                            EasyWalletErrCode.WALLET_NOT_EXIST -> showToast("$walletNameStr 不存在")
                            EasyWalletErrCode.PASSWORD_ERROR -> showToast("密码错误")
                            else -> {
                                showToast("发生未知错误 ${it.message}")
                                it.printStackTrace()
                            }
                        }
                    } else {
                        showToast("发生未知错误 ${it.message}")
                        it.printStackTrace()
                    }
                }).addTo(disposable)
        }

        delete.setOnClickListener {
            val walletNameStr = walletName.editText?.editableText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }

            Observable.fromCallable {
                SingleEasyWallet.deleteWallet(name = walletNameStr)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showToast("删除成功")
                    refresh()
                }, {
                    showToast("删除失败 ${it.message}")
                    it.printStackTrace()
                }).addTo(disposable)
        }

        lock.setOnClickListener {
            val walletNameStr = walletName.editText?.editableText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }
            showToast("已经加锁")
            SingleEasyWallet.lock(walletNameStr)
        }

        gotoEthTran.setOnClickListener {
            if(SingleEasyWallet.unlockedWallet == null) {
                // 未解锁任何钱包
                showToast("Please unlock a wallet")
                return@setOnClickListener
            }
            startActivity(Intent(this,EthActivity::class.java))
        }

    }


}
