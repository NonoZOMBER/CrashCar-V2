package com.zcode.crashcar.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.LoginClass
import com.zcode.crashcar.databinding.ActivityLoginBinding
import com.zcode.crashcar.ui.home.HomeActivity
import com.zcode.crashcar.ui.home.profile.activities.EditUserActivity
import com.zcode.crashcar.ui.register.RegisterActivity
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleApiClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        initGoogleApi()
        initComponent()
    }

    private fun initGoogleApi() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        mGoogleApiClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initComponent() {
        binding.btnLogin.setOnClickListener {
            ocultarTeclado()
            if (Herramientas.isValidEmail(binding.textEmail.text.toString())) {
                state(1)
                login(
                    1,
                    binding.textEmail.text.toString(),
                    binding.textPassword.text.toString()
                )

            } else {
                showDialogAlertEmail("El email no es válido, por favor vuelve a introducirlo")
            }
        }
        passwordVisivility()
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()

        }
        binding.btnLoginGoogle.setOnClickListener {
            ocultarTeclado()
            loginGoogle()
        }
    }

    private fun showDialogVerifyEmail(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_verify_email)

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

    private fun passwordVisivility() {
        binding.btnVisibility.setOnClickListener {
            if (binding.textPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.textPassword.transformationMethod = PasswordTransformationMethod()
                binding.textPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnVisibility.setImageResource(R.drawable.ic_visibilitty_off)
                binding.textPassword.text?.length?.let { it1 ->
                    binding.textPassword.setSelection(
                        it1
                    )
                }
            } else {
                binding.textPassword.transformationMethod = null
                binding.textPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnVisibility.setImageResource(R.drawable.ic_visibilitty_on)
                binding.textPassword.text?.length?.let { it1 ->
                    binding.textPassword.setSelection(
                        it1
                    )
                }
            }
        }
    }

    private fun showDialogAlertEmail(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_error_email)
        imgLottie.loop(true)

        textMsg.text = msg

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showDialogAlertNotRegister(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_caution)
        imgLottie.loop(true)

        textMsg.text = msg

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showDialogAlertNotGoogle(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_google_error)
        imgLottie.loop(true)

        textMsg.text = msg

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onRestart() {
        super.onRestart()
        state(0)
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
                                binding.checkRemember.isChecked,
                                response.body()?.usuario?.tipologin ?: 1,
                                response.body()?.usuario?.id ?: ""
                            )
                            if(response.body()?.usuario?.tipologin == 1) {
                                auth.signInWithEmailAndPassword(
                                    binding.textEmail.text.toString(), binding.textPassword.text.toString()
                                ).addOnCompleteListener {
                                    val user = auth.currentUser
                                    if (user != null && user.isEmailVerified) {
                                        if (response.body()?.usuario?.nombre == null) {
                                            val intent = Intent(
                                                this@LoginActivity, EditUserActivity::class.java
                                            )
                                            intent.putExtra("code", 0)
                                            startActivity(intent)
                                        } else {
                                            val intent = Intent(
                                                applicationContext, HomeActivity::class.java
                                            )
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                            finish()
                                        }
                                    } else {
                                        showDialogVerifyEmail("Debes de verificar tu correo electrónico")
                                    }
                                }
                            } else {
                                if (response.body()?.usuario?.nombre == null) {
                                    val intent = Intent(
                                        this@LoginActivity, EditUserActivity::class.java
                                    )
                                    intent.putExtra("code", 0)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(
                                        applicationContext, HomeActivity::class.java
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else if (response.body()?.code == -2) {
                            binding.passwordIncorrecta.visibility = View.VISIBLE
                            state(0)
                        } else if (response.body()?.code == 0) {
                            showDialogAlertNotRegister(response.body()?.msg ?: "")
                            state(0)
                        } else if (response.body()?.code == -4) {
                            showDialogAlert(response.body()?.msg ?: "")
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

    private fun showDialogAlert(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_caution)
        imgLottie.loop(true)

        textMsg.text = msg

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showDialogErrorServer(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

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
                binding.viewLogin.visibility = View.GONE
                binding.btnBack.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.passwordIncorrecta.visibility = View.GONE
            }

            0 -> {
                binding.viewLogin.visibility = View.VISIBLE
                binding.btnBack.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }

            else -> {
                binding.viewLogin.visibility = View.VISIBLE
                binding.btnBack.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loginGoogle() {
        val signInIntent = mGoogleApiClient.signInIntent
        loginGoogleLauncher.launch(signInIntent)
    }

    private val loginGoogleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            login(2, account.result.email.toString(), "")
        } else {
            showDialogAlertNotGoogle("Error con los servicios de Google, inténtelo más tarde")
            Log.e("Response", "No ha habido respuesta")
        }
    }

    fun ocultarTeclado() {
        val focusedView: View? = currentFocus
        focusedView?.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedView?.windowToken, 0)
    }
}