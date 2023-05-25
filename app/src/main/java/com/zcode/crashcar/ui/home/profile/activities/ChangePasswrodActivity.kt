package com.zcode.crashcar.ui.home.profile.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ChangePassword
import com.zcode.crashcar.databinding.ActivityChangePasswrodBinding
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePasswrodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswrodBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswrodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
    }

    private fun initComponent() {
        binding.btnChangePass.setOnClickListener { changePassword() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun changePassword() {
        resetError()
        if (binding.textPasswordActual.text.isNotEmpty()) {
            if (binding.textPasswordNew.text.toString().length >= 8) {
                if (binding.textPasswordNew.text.toString() == binding.textRePasswordNew.text.toString()) {
                    changePass()
                } else {
                    binding.passwordIncorrectaReNew.visibility = View.VISIBLE
                }
            } else {
                binding.passwordIncorrectaNew.visibility = View.VISIBLE
            }
        } else {
            binding.passwordIncorrecta.text = String.format("Debes de introducir la contraseña actual")
            binding.passwordIncorrecta.visibility = View.VISIBLE
        }
    }

    private fun changePass() {
        val changePassword = ChangePassword(
            prefsSetting.getIdUser(),
            binding.textPasswordActual.text.toString(),
            binding.textPasswordNew.text.toString()
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().changePassword(changePassword)
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        runOnUiThread {
                            when (response.body()!!.code) {
                                1 -> {
                                    showDialogVerifyChange(response.body()!!.msg)
                                    prefsSetting.savePassword(binding.textPasswordNew.text.toString())
                                }
                                -1 -> {
                                    binding.passwordIncorrecta.text = String.format("Contraseña incorrecta")
                                    binding.passwordIncorrecta.visibility = View.VISIBLE
                                }
                                else -> {
                                    Log.i("ResponseCode", response.body()!!.msg)
                                }
                            }
                        }
                    }
                } else {
                    Log.e("ResponseChangePassword", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showDialogVerifyChange(s: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verify_change_password, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_verify_animated)

        textMsg.text = s

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }
    }

    private fun resetError() {
        binding.passwordIncorrecta.visibility = View.GONE
        binding.passwordIncorrectaNew.visibility = View.GONE
        binding.passwordIncorrectaReNew.visibility = View.GONE
    }
}