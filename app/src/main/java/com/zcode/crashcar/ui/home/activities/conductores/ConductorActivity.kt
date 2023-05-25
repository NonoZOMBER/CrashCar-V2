package com.zcode.crashcar.ui.home.activities.conductores

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.databinding.ActivityConductorBinding
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConductorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConductorBinding
    private lateinit var conductor: ConductorItem
    private var update = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val idConductor = intent.getIntExtra("conductor", 0)

        if (idConductor != 0) {
            update = true
            getConductor(idConductor)
        } else {
            update = false
            initComponent(null)
        }
    }

    private fun getConductor(idConductor: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getConductor(idConductor)
            if (response.isSuccessful) runOnUiThread { initComponent(response.body()) } else Log.e(
                "ResponseDriver",
                response.body().toString()
            )
        }
    }

    private fun initComponent(conductor: ConductorItem?) {
        if (conductor != null) {
            this.conductor = conductor
            binding.lblConductorActivity.text = getString(R.string.lblEditandoConductor)
            binding.textDniConductor.setText(conductor.dniConductor)
            binding.textEmailConductor.setText(conductor.email)
            binding.textPhoneConductor.setText(conductor.phone.toString())
            binding.textPaisConductor.setText(conductor.pais)
            binding.textLocalidadConductor.setText(conductor.localidad)
            binding.textCodPostal.setText(conductor.codpostal.toString())
            binding.textDireccionConductor.setText(conductor.direccion)
            binding.textApellidosConductor.setText(conductor.apellidos)
            binding.textNombreConductor.setText(conductor.nombre)
            binding.textDniConductor.isEnabled = false
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRegitrarConductor.setOnClickListener {
            if (update) {
                modifircarConductor()
            } else {
                addConductor()
            }
        }
    }

    private fun comprobar(): Boolean {
        return binding.textPhoneConductor.text.isNotEmpty() &&
                binding.textEmailConductor.text.isNotEmpty() &&
                binding.textPaisConductor.text.isNotEmpty() &&
                binding.textLocalidadConductor.text.isNotEmpty() &&
                binding.textDireccionConductor.text.isNotEmpty() &&
                binding.textCodPostal.text.isNotEmpty() &&
                binding.textDniConductor.text.isNotEmpty() &&
                binding.textApellidosConductor.text.isNotEmpty() &&
                binding.textNombreConductor.text.isNotEmpty()
    }

    private fun addConductor() {
        if (comprobar() /*&& Utils.validateDNI(binding.textDniConductor.text.toString())*/) {

            val conductor = ConductorItem(
                binding.textApellidosConductor.text.toString(),
                binding.textCodPostal.text.toString().toInt(),
                binding.textDireccionConductor.text.toString(),
                binding.textDniConductor.text.toString(),
                binding.textEmailConductor.text.toString(),
                null,
                binding.textLocalidadConductor.text.toString(),
                binding.textNombreConductor.text.toString(),
                binding.textPaisConductor.text.toString(),
                binding.textPhoneConductor.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().saveConductor(conductor)
                if (response.isSuccessful) {
                    runOnUiThread {
                        val intent = Intent()
                        intent.putExtra("idConductor", response.body()?.idConductor)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } else {
                    Log.e("ResponseConductor", response.body().toString())
                }
            }

        } else {
            if (comprobar()) {
                DialogAlert.showDialogAlert(
                    this,
                    "Debes de rellenar todos los campos",
                    R.raw.ic_caution
                )
            } else if (!Herramientas.validateDNI(binding.textDniConductor.text.toString())) {
                DialogAlert.showDialogAlert(
                    this,
                    "El DNI no es válido",
                    R.raw.ic_caution
                )
            }
        }
    }

    private fun modifircarConductor() {
        if (comprobar()) {

            val conductor = ConductorItem(
                binding.textApellidosConductor.text.toString(),
                binding.textCodPostal.text.toString().toInt(),
                binding.textDireccionConductor.text.toString(),
                binding.textDniConductor.text.toString(),
                binding.textEmailConductor.text.toString(),
                this.conductor.idConductor,
                binding.textLocalidadConductor.text.toString(),
                binding.textNombreConductor.text.toString(),
                binding.textPaisConductor.text.toString(),
                binding.textPhoneConductor.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().updateConductor(
                    conductor.idConductor ?: 0, conductor
                )
                if (response.isSuccessful) {
                    runOnUiThread {
                        finish()
                    }
                } else {
                    Log.e("ResponseConductor", response.body().toString())
                }
            }

        } else {
            DialogAlert.showDialogAlert(
                this,
                "Debes de rellenar todos los campos",
                R.raw.ic_caution
            )
        }
    }
}