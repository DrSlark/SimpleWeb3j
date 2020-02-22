package com.tt.simpleweb3j

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.tt.esayweb3j.EasyWalletCenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

fun Disposable.addTo(c: CompositeDisposable): Boolean {
    return c.add(this)
}

class MainActivity : AppCompatActivity() {
    val gson = Gson()
    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Observable.fromCallable {
            EasyWalletCenter.listAllWalletName()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            walletNames.text = it.reduce { acc, s ->
                "$s\n$acc"
            }
        }.addTo(disposable)


        create.setOnClickListener {
            val walletName = walletName.editText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }
            val password = password.editText?.toString() ?: kotlin.run {
                showToast("empty password")
                return@setOnClickListener
            }
            Observable.fromCallable {
                EasyWalletCenter.generate(name = walletName, password = password)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    walletDetails.append(gson.toJson(it) + "\n")
                }
        }

        login.setOnClickListener {
            val walletName = walletName.editText?.toString() ?: kotlin.run {
                showToast("empty walletName")
                return@setOnClickListener
            }
            val password = password.editText?.toString() ?: kotlin.run {
                showToast("empty password")
                return@setOnClickListener
            }

            Observable.fromCallable {
                EasyWalletCenter.unlock(name = walletName, password = password)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    walletDetails.append(gson.toJson(it) + "\n")
                }
        }


    }


}
