package com.zcode.crashcar.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.LoginClass
import com.zcode.crashcar.databinding.ActivityMainBinding
import com.zcode.crashcar.ui.home.HomeActivity
import com.zcode.crashcar.ui.home.profile.activities.EditUserActivity
import com.zcode.crashcar.ui.login.LoginActivity
import com.zcode.crashcar.ui.register.RegisterActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (prefsSetting.getRemember()) {
            state(1)
            login(prefsSetting.getTypeLogin(), prefsSetting.getEmail(), prefsSetting.getPassword())
        } else {
            state(0)
            initComponent()
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.imgAnimatedMain.playAnimation()
    }

    private fun initComponent() {
        binding.btnEntrar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.btnRegister.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun login(type: Int, email: String, password: String) {
        state(1)
        val loginClass = if (type == 1) {
            LoginClass(email, password, type)
        } else {
            LoginClass(email, "", type)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().loginUser(loginClass)
                if (response.isSuccessful) {
                    runOnUiThread {
                        if (response.body()?.code == 1) {
                            prefsSetting.saveCredentials(
                                response.body()?.usuario?.email ?: "",
                                response.body()?.usuario?.password ?: "",
                                prefsSetting.getRemember(),
                                response.body()?.usuario?.tipologin ?: 1,
                                response.body()?.usuario?.id ?: ""
                            )
                            response.body()!!.usuario?.let { prefsSetting.saveUser(it) }
                            if (response.body()?.usuario?.nombre == null) {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        EditUserActivity::class.java
                                    ).putExtra("code", 0)
                                )
                                overridePendingTransition(R.anim.anim_show_block, R.anim.anim_hide_block)
                                finish()
                            } else {
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                overridePendingTransition(R.anim.anim_show_block, R.anim.anim_hide_block)
                                finish()
                            }
                        } else if (response.body()?.code == -2) {
                            state(0)
                        } else if (response.body()?.code == 0) {
                            state(0)
                        }
                    }
                } else {
                    Log.e("ResponseError", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error: ${e.message}")
                runOnUiThread {
                    showDialogErrorServer("No es posible conectar con los servicios de CrashCar en este momento, intentelo de nuevo más tarde")
                }

            }
        }
    }

    private fun showDialogErrorServer(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_connection_error)
        imgLottie.loop(true)

        textMsg.text = msg

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            state(0)
            alertDialog.dismiss()
        }
    }

    private fun state(code: Int) {
        when (code) {
            1 -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.viewScreen.visibility = View.GONE
            }

            0 -> {
                binding.progressBar.visibility = View.GONE
                binding.viewScreen.visibility = View.VISIBLE
                initComponent()
            }

            else -> {
                binding.progressBar.visibility = View.GONE
                binding.viewScreen.visibility = View.VISIBLE
            }
        }
    }


}