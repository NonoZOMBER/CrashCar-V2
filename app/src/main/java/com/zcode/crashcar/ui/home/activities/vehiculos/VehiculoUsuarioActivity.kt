package com.zcode.crashcar.ui.home.activities.vehiculos

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.SpinnerAdapterTypeVehicle
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.databinding.ActivityVehiculoUsuarioBinding
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehiculoUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVehiculoUsuarioBinding
    private lateinit var vehiculo: VehiculoItem
    private var isUpdate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehiculoUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val idVehiculo = intent.extras?.getInt("vehiculo", 0)
        if (idVehiculo != 0 && idVehiculo != null) {
            isUpdate = true
            getVehiculo(idVehiculo)
        } else {
            isUpdate = false
            initComponent(null)
        }
    }

    private fun getVehiculo(idVehiculo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getVehiculo(idVehiculo)
            if (response.isSuccessful) {
                runOnUiThread {
                    vehiculo = response.body()!!
                    initComponent(vehiculo)
                }
            }
        }
    }

    private fun initComponent(vehiculoItem: VehiculoItem?) {
        binding.spinnerTypeVehicle.adapter =
            SpinnerAdapterTypeVehicle(this, Herramientas.getSpinnerItems())

        if (vehiculoItem != null) {
            binding.lblVehiculoActivity.text = getString(R.string.lblEditandoVehiculo)
            binding.textNameVehiculoUsuario.setText(vehiculoItem.marca)
            binding.textModeloVehiculoUsuario.setText(vehiculoItem.modelo)
            binding.textMatriculaVehiculoUsuario.setText(vehiculoItem.matricula)
            binding.textPaisMatriculaVehiculoUsuario.setText(vehiculoItem.paisMatricula)

            if (vehiculoItem.tipoVehiculo.lowercase() == "turismo") binding.spinnerTypeVehicle.setSelection(
                0
            )
            if (vehiculoItem.tipoVehiculo.lowercase() == "motocicleta") binding.spinnerTypeVehicle.setSelection(
                2
            )
            if (vehiculoItem.tipoVehiculo.lowercase() == "furgoneta/camión") binding.spinnerTypeVehicle.setSelection(
                1
            )
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRegitrarVehiculoUsuario.setOnClickListener {
            if (isUpdate) {
                updateVehiculo()
            } else {
                newVehiculo()
            }
        }

    }

    private fun newVehiculo() {
        if (comprobarDatos()) {
            val typeVehicle = getTypeVehicle()
            val vehiculoAdd = VehiculoItem(
                true, prefsSetting.getIdUser(),
                binding.textNameVehiculoUsuario.text.toString(),
                binding.textMatriculaVehiculoUsuario.text.toString(),
                binding.textModeloVehiculoUsuario.text.toString(),
                binding.textPaisMatriculaVehiculoUsuario.text.toString(),
                typeVehicle
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().newVehiculo(vehiculoAdd)
                if (response.isSuccessful) {
                    finish()
                } else {
                    Log.e("ResponseAddVehicle", response.errorBody().toString())
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

    private fun comprobarDatos(): Boolean {
        return binding.textNameVehiculoUsuario.text.isNotEmpty() &&
                binding.textMatriculaVehiculoUsuario.text.isNotEmpty() &&
                binding.textModeloVehiculoUsuario.text.isNotEmpty() &&
                binding.textPaisMatriculaVehiculoUsuario.text.isNotEmpty()
    }

    private fun updateVehiculo() {
        if (comprobarDatos()) {
            val typeVehicle = getTypeVehicle()

            val vehiculoUpdate = VehiculoItem(
                true,
                prefsSetting.getIdUser(),
                vehiculo.idVehiculo,
                binding.textNameVehiculoUsuario.text.toString(),
                binding.textMatriculaVehiculoUsuario
                    .text.toString(),
                binding.textModeloVehiculoUsuario.text.toString(),
                binding.textPaisMatriculaVehiculoUsuario.text.toString(),
                typeVehicle
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().updateVehiculo(vehiculo.idVehiculo ?: 0, vehiculoUpdate)
                if (response.isSuccessful) {
                    runOnUiThread { finish() }
                } else {
                    Log.e("ResponseUpdateVehicle", response.errorBody().toString())
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

    private fun getTypeVehicle(): String {
        if (binding.spinnerTypeVehicle.selectedItemPosition == 0) return "Turismo"
        if (binding.spinnerTypeVehicle.selectedItemPosition == 1) return "Furgoneta/Camión"
        if (binding.spinnerTypeVehicle.selectedItemPosition == 2) return "Motocicleta"
        return ""
    }
}