package com.zcode.crashcar.ui.home.profile.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.response.Usuario
import com.zcode.crashcar.databinding.ActivityEditUserBinding
import com.zcode.crashcar.ui.home.HomeActivity
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditUserActivity : AppCompatActivity(), TextWatcher {
    private lateinit var binding: ActivityEditUserBinding
    private var btnsaveActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
    }

    private fun initComponent() {
        if (intent.extras?.getInt("code", 0) == 1) {
            binding.btnBackEditUser.setOnClickListener { finish() }
            binding.btnSaveEditUser.setOnClickListener {
                if (btnsaveActive) {
                    if (comprobarDatos()) {
                        questEditUser()
                    } else {
                        DialogAlert.showDialogAlert(
                            this,
                            "Debes de rellenar todos los campos",
                            R.raw.ic_caution
                        )
                    }
                } else {
                    finish()
                }
            }
        } else {
            binding.btnBackEditUser.visibility = View.INVISIBLE
            binding.btnSaveEditUser.setOnClickListener {
                if (comprobarDatos()) {
                    questEditUser()
                } else {
                    DialogAlert.showDialogAlert(
                        this,
                        "Debes de rellenar todos los campos",
                        R.raw.ic_caution
                    )
                }
            }
        }
        loadUserProfile()
        initChangeData()
    }

    private fun showDialogAlert(msg: String) {
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
            alertDialog.dismiss()
        }
    }

    private fun initChangeData() {
        binding.txtNombre.addTextChangedListener(this)
        binding.txtApellidos.addTextChangedListener(this)
        binding.txtPais.addTextChangedListener(this)
        binding.txtPhone.addTextChangedListener(this)
        binding.txtLocalidad.addTextChangedListener(this)
        binding.txtCodPos.addTextChangedListener(this)
        binding.txtCalle.addTextChangedListener(this)
    }

    private fun loadUserProfile() {
        btnsaveActive = false
        val user = prefsSetting.getUser()
        if (user != null) {
            binding.txtNombre.setText(user.nombre)
            binding.txtApellidos.setText(user.apellidos)
            binding.txtCalle.setText(user.direccion)
            binding.txtLocalidad.setText(user.localidad)
            binding.txtCodPos.setText(user.codpostal)
            binding.txtPhone.setText(user.telefono)
            binding.txtPais.setText(user.pais)
            binding.txtProvincia.setText(user.provincia)
        }
    }

    private fun questEditUser() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verify_edit_user, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btn_ok_close_session)
        val btnCancel: FrameLayout = dialogView.findViewById(R.id.btn_cancel_close_session)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        btnOk.setOnClickListener {
            alertDialog.dismiss()
            editUser()
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    private fun editUser() {
        val userEdit =
            Usuario(
                binding.txtApellidos.text.toString(),
                binding.txtCodPos.text.toString(),
                binding.txtCalle.text.toString(),
                prefsSetting.getEmail(),
                prefsSetting.getIdUser(),
                binding.txtLocalidad.text.toString(),
                binding.txtNombre.text.toString(),
                binding.txtPais.text.toString(),
                prefsSetting.getPassword(),
                binding.txtProvincia.text.toString(),
                binding.txtPhone.text.toString(),
                prefsSetting.getTypeLogin()
            )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    RetrofitObject.getCallRetrofit().updateUser(prefsSetting.getIdUser(), userEdit)
                if (response.isSuccessful) {
                    runOnUiThread {
                        prefsSetting.saveUser(response.body()!!)
                        if (intent.extras?.getInt("code", 0) == 1) {
                            prefsSetting.saveUser(response.body()!!)
                            finish()
                        } else {
                            val intent = Intent(
                                applicationContext,
                                HomeActivity::class.java
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            prefsSetting.saveUser(response.body()!!)
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("UserEditError", response.errorBody().toString())
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showDialogErrorServer("No es posible conectar con los servicios de CrashCar en este momento, intentelo de nuevo más tarde")
                }
            }
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
            alertDialog.dismiss()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val user = prefsSetting.getUser()
        btnsaveActive =
            s.toString() != user?.nombre && s.toString() != user?.apellidos && s.toString() != user?.direccion && s.toString() != user?.codpostal.toString() && s.toString() != user?.localidad && s.toString() != user?.pais && s.toString() != user?.telefono.toString() && s.toString() != user?.email
    }

    override fun afterTextChanged(s: Editable?) {}

    private fun comprobarDatos(): Boolean {

        return binding.txtCalle.text.isNotEmpty()
                && binding.txtApellidos.text.isNotEmpty()
                && binding.txtNombre.text.isNotEmpty()
                && binding.txtPhone.text.isNotEmpty()
                && binding.txtCalle.text.isNotEmpty()
                && binding.txtCodPos.text.isNotEmpty()
                && binding.txtProvincia.text.isNotEmpty()
                && binding.txtLocalidad.text.isNotEmpty()
    }
}