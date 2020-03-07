package com.tt.simpleweb3j

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tt.esayweb3j.EasyWalletErrCode
import com.tt.esayweb3j.EasyWalletException
import com.tt.esayweb3j.SingleEasyWallet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.web3j.crypto.MnemonicUtils

fun Disposable.addTo(c: CompositeDisposable): Boolean {
    return c.add(this)
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

private fun Context.copyText(text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
        ClipData.newPlainText(
            null,
            text
        )
    )
    showToast("copied")
}

val gson = GsonBuilder().setPrettyPrinting().create()


class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    fun refresh() {
        val unlockWalletJson = SingleEasyWallet.unlockedWallet?.let {
            gson.toJson(it)
        } ?: ""
        val str =
            SingleEasyWallet.listAllWalletNames().takeIf { it.isNotEmpty() }?.reduce { acc, s ->
                "$s\n$acc"
            } + unlockWalletJson

        currentStatus.text = "当前钱包状态 点击刷新\n$str"
        currentStatus.setOnLongClickListener {
            copyText(unlockWalletJson)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh()

        create()
        unlock()
        deleteWallet()
        lock()
        recover()
        changeName()
        autoUnlock()

        addAddress.setOnClickListener {
            kotlin.runCatching {
                SingleEasyWallet.unlockedWallet?.easyAddCommunityAddress()
                refresh()
            }.getOrElse {
                opLog.append("addAddress\n" + it.localizedMessage)
            }
        }

        gotoEthTran.setOnClickListener {
            if (SingleEasyWallet.unlockedWallet == null) {
                // 未解锁任何钱包
                showToast("Please unlock a wallet")
                return@setOnClickListener
            }
            startActivity(Intent(this, EthActivity::class.java))
        }

        currentStatus.setOnClickListener {
            refresh()
        }

    }


    private fun changeName() {
        changeName.setOnClickListener {
            val oldWalletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty old walletName")
                        return@setOnClickListener
                    }
            if (!SingleEasyWallet.isNameExist(oldWalletNameStr)) {
                showToast("not found old walletName")
                return@setOnClickListener
            }

            val newWalletNameStr =
                mnemonic.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty new walletName")
                        return@setOnClickListener
                    }
            if (SingleEasyWallet.isNameExist(newWalletNameStr)) {
                showToast("new walletName duplicated")
                return@setOnClickListener
            }
            kotlin.runCatching {
                SingleEasyWallet.changeName(oldWalletNameStr, newWalletNameStr)
                refresh()
            }.getOrElse {
                it.printStackTrace()
            }
        }
    }


    private fun lock() {
        lock.setOnClickListener {
            val walletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty walletName")
                        return@setOnClickListener
                    }
            SingleEasyWallet.lock(walletNameStr)
            showToast("已经加锁")
            refresh()
        }
    }

    private fun deleteWallet() {
        delete.setOnClickListener {
            val walletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
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
    }

    private fun unlock() {
        unlock.setOnClickListener {
            val walletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty walletName")
                        return@setOnClickListener
                    }

            if (!SingleEasyWallet.isNameExist(walletNameStr)) {
                showToast("$walletNameStr 不存在")
                return@setOnClickListener
            }

            val passwordStr =
                password.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty password")
                        return@setOnClickListener
                    }

            Observable.fromCallable {
                SingleEasyWallet.unlock(name = walletNameStr, password = passwordStr)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    refresh()
                    opLog.append("unlock\n" + gson.toJson(it) + "\n\n\n")
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
    }

    private fun create() {
        create.setOnClickListener {
            val walletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty walletName")
                        return@setOnClickListener
                    }
            if (SingleEasyWallet.isNameExist(walletNameStr)) {
                showToast("$walletNameStr 已经存在")
                return@setOnClickListener
            }

            val passwordStr =
                password.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
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
                    opLog.append("generate\n${it.easyBip44Wallet?.mnemonic}\n\n${gson.toJson(it)}\n\n\n")
                }, {
                    if (it is EasyWalletException && it.code == EasyWalletErrCode.WALLET_NAME_DUPLICATED) {
                        showToast("$walletNameStr 已经存在")
                    } else {
                        showToast("发生未知错误 ${it.message}")
                        it.printStackTrace()
                    }
                }).addTo(disposable)

        }
    }


    private fun recover() {
        recover.setOnClickListener {

            val walletNameStr =
                walletName.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty walletName")
                        return@setOnClickListener
                    }

            if (SingleEasyWallet.isNameExist(walletNameStr)) {
                showToast("$walletNameStr 已经存在")
                return@setOnClickListener
            }

            val passwordStr =
                password.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        // 一般>8位
                        showToast("empty password")
                        return@setOnClickListener
                    }

            val mnemonicStr =
                mnemonic.editText?.editableText?.toString()?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        showToast("empty mnemonic")
                        return@setOnClickListener
                    }
            if (!MnemonicUtils.validateMnemonic(mnemonicStr)) {
                showToast("error mnemonic")
                return@setOnClickListener
            }

            SingleEasyWallet.hasExistSameMnemonic(mnemonicStr)?.let {
                AlertDialog.Builder(this)
                    .setMessage("已经存在名为$it 的助记词，是否覆盖？")
                    .setPositiveButton("Yes") { dialog, which ->
                        dialog.dismiss()
                        asyncRecover(mnemonicStr, walletNameStr, passwordStr)
                    }
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                return@setOnClickListener
            }

            asyncRecover(mnemonicStr, walletNameStr, passwordStr)
        }
    }

    private fun asyncRecover(
        mnemonicStr: String,
        walletNameStr: String,
        passwordStr: String
    ) {
        Observable.fromCallable {
            SingleEasyWallet.recover(
                mnemonic = mnemonicStr,
                name = walletNameStr,
                password = passwordStr
            )
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                refresh()
                opLog.append("recover\n${it.easyBip44Wallet?.mnemonic}\n\n${gson.toJson(it)}\n\n\n")
            }, {
                if (it is EasyWalletException && it.code == EasyWalletErrCode.WALLET_NAME_DUPLICATED) {
                    showToast("$walletNameStr 已经存在")
                } else {
                    showToast("发生未知错误 ${it.message}")
                    it.printStackTrace()
                }
            }).addTo(disposable)
    }

    private fun autoUnlock() {
        autoUnlock.setOnClickListener {
            if (SingleEasyWallet.tryToAutoUnlock()) {
                showToast("auto unlock success")
                refresh()
            } else {
                showToast("auto unlock failed")
            }
        }
    }


}
