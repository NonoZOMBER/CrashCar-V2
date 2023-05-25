package com.zcode.crashcar.ui.register

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.zcode.crashcar.MainApplication
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.LoginClass
import com.zcode.crashcar.api.controller.RegisterClass
import com.zcode.crashcar.databinding.ActivityRegisterBinding
import com.zcode.crashcar.ui.home.HomeActivity
import com.zcode.crashcar.ui.home.profile.activities.EditUserActivity
import com.zcode.crashcar.ui.login.LoginActivity
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleApiClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGoogleApi()
        mGoogleApiClient.signOut()
        auth = FirebaseAuth.getInstance()
        initComponent()
    }

    private fun initGoogleApi() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initComponent() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
        binding.btnLoginGoogle.setOnClickListener {
            ocultarTeclado()
            if (binding.checkTerms.isChecked) {
                loginGoogle()
            } else {
                showDialogAlert("Debes de aceptar los terminos de uso para poder continuar")
            }
        }
        binding.btnRegister.setOnClickListener {
            ocultarTeclado()
            binding.passwordIncorrecta.visibility = View.GONE
            if (Herramientas.isValidEmail(binding.textEmail.text.toString())) {
                if (binding.textPassword.text.toString() == binding.textRePassword.text.toString()) {
                    if (binding.checkTerms.isChecked) {
                        register(
                            1,
                            binding.textEmail.text.toString(),
                            binding.textPassword.text.toString()
                        )
                    } else {
                        showDialogAlert("Debes de aceptar los terminos de uso para poder continuar")
                    }
                } else {
                    binding.passwordIncorrecta.visibility = View.VISIBLE
                }
            } else {
                showDialogAlertEmail("El email no es válido, por favor vuelve a introducirlo")
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun showDialogAlertEmail(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

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

    private fun loginGoogle() {
        val signInIntent = mGoogleApiClient.signInIntent
        loginGoogleLauncher.launch(signInIntent)
    }

    private val loginGoogleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkGoogleServicesAvailability(this)) {
            when (it.resultCode) {
                RESULT_OK -> {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    login(2, account.result.email.toString(), "")
                }
                RESULT_CANCELED -> {
                    Log.i("GoogleResponse", "Google response Cancelled")
                }
            }
        } else {
            showDialogAlertNotGoogle("Error con los servicios de Google, inténtelo más tarde")
            Log.e("Response", "No ha habido respuesta")
        }
    }

    private fun checkGoogleServicesAvailability(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun register(type: Int, email: String, password: String) {
        state(1)
        val registerClass = if (type == 1) {
            RegisterClass(email, password, type)
        } else {
            RegisterClass(email, "", type)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().registerUser(registerClass)
                if (response.isSuccessful) {
                    runOnUiThread {
                        if (response.body()?.code == 1) {
                            MainApplication.prefsSetting.saveCredentials(
                                response.body()?.usuario?.email ?: "",
                                response.body()?.usuario?.password ?: "",
                                false,
                                response.body()?.usuario?.tipologin ?: 1,
                                response.body()?.usuario?.id ?: ""
                            )
                            if (type == 1) {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            user?.sendEmailVerification()?.addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    showDialogVerifyEmail("Debes de verificar tu correo electrónico")
                                                }
                                            }
                                        }
                                    }
                            } else {
                                login(2, response.body()!!.usuario?.email.toString(), "")
                            }
                        } else if (response.body()?.code == -2) {
                            state(0)
                        } else if (response.body()?.code == 0) {
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

    private fun login(type: Int, email: String, password: String) {
        state(1)
        val loginClass = LoginClass(email, "", type)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().loginUser(loginClass)
                if (response.isSuccessful) {
                    runOnUiThread {
                        if (response.body()?.code == 1) {
                            MainApplication.prefsSetting.saveCredentials(
                                response.body()?.usuario?.email ?: "",
                                response.body()?.usuario?.password ?: "",
                                false,
                                response.body()?.usuario?.tipologin ?: 1,
                                response.body()?.usuario?.id ?: ""
                            )
                            if (response.body()?.usuario?.nombre == null) {
                                val intent = Intent(
                                    this@RegisterActivity, EditUserActivity::class.java
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
                        } else if (response.body()?.code == -2) {
                            binding.passwordIncorrecta.visibility = View.VISIBLE
                            state(0)
                        } else if (response.body()?.code == 0) {
                            showDialogAlert(response.body()?.msg ?: "")
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

    private fun showDialogVerifyEmail(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

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
            alertDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        state(0)
        mGoogleApiClient.signOut()
    }

    private fun showDialogAlertNotGoogle(msg: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

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

    fun ocultarTeclado() {
        val focusedView: View? = currentFocus
        focusedView?.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedView?.windowToken, 0)
    }
}