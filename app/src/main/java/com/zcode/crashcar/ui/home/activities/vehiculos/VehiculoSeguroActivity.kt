package com.zcode.crashcar.ui.home.activities.vehiculos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemVehiculoUsuario
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.databinding.ActivityVehiculoSeguroBinding
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehiculoSeguroActivity : AppCompatActivity(),
    AdapterItemVehiculoUsuario.OnSizeChangeListener,
    AdapterItemVehiculoUsuario.OnItemClickListener {
    private lateinit var binding: ActivityVehiculoSeguroBinding
    private lateinit var vehiculoSeguro: VehiculoSeguro
    private lateinit var adapterVehiculo: AdapterItemVehiculoUsuario
    private lateinit var listVehiculoUsuario: ArrayList<VehiculoItem>
    private var viewListVehiculosUsuario: BottomSheetDialog? = null
    private lateinit var listVehiculosRegistrados: ArrayList<VehiculoItem>
    private lateinit var adapterVehiculosRegistrados: AdapterItemVehiculoUsuario
    private var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehiculoSeguroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listVehiculoUsuario = ArrayList()
        listVehiculosRegistrados = ArrayList()
        val idVehiculoSeguro = intent.extras?.getInt("idVehiculoSeguro", 0)
        if (idVehiculoSeguro != null && idVehiculoSeguro != 0) {
            update = true
            getVehiculoSeguro(idVehiculoSeguro)
        } else {
            update = false
            initComponent(null)
        }
        getVehiculosUsuario()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getVehiculosUsuario() {

        adapterVehiculosRegistrados =
            AdapterItemVehiculoUsuario(
                listVehiculosRegistrados, null,
                delete = false,
                update = false,
                onItemClickListener = this
            )

        CoroutineScope(Dispatchers.IO).launch {
            val response =
                RetrofitObject.getCallRetrofit().getListVehiculos(prefsSetting.getIdUser())
            if (response.isSuccessful) {
                response.body()?.let { listVehiculosRegistrados.addAll(it) }
                adapterVehiculosRegistrados.notifyDataSetChanged()
            } else {
                Log.e("ResponseVehicles", "Error: ${response.message()}")
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        getVehiculo(vehiculoSeguro.idVehiculo)
    }

    private fun initComponent(vehiculoSeguro: VehiculoSeguro?) {
        adapterVehiculo = AdapterItemVehiculoUsuario(
            listVehiculoUsuario, this,
            delete = false,
            update = true,
            null
        )
        binding.listVehiculo.layoutManager = LinearLayoutManager(this)
        binding.listVehiculo.adapter = adapterVehiculo

        if (vehiculoSeguro != null) {
            this.vehiculoSeguro = vehiculoSeguro
            binding.lblVehiculoActivity.text = getString(R.string.lblEditandoVehiculoSeguro)
            binding.textNumeroPoliza.setText(vehiculoSeguro.numeroPoliza)
            binding.textFechaInicioCartaVerde.setText(vehiculoSeguro.fechaCartaVerdeInicio)
            binding.textFechaVencimientoCartaVerde.setText(vehiculoSeguro.fechaCartaVerdeFin)
            binding.textNumeroCartaVede.setText(vehiculoSeguro.numeroCartaVerde)
            getVehiculo(vehiculoSeguro.idVehiculo)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectVehiculoSeguro.setOnClickListener { showBottomSheet() }
        binding.btnRegitrarVehiculoSeguro.setOnClickListener {
            if (update) {
                actualizarVehiculoSeguro()
            } else {
                addVehiculoSeguro()
            }
        }
    }

    private fun addVehiculoSeguro() {
        if (comprobar()) {
            val addVehiculoSeguro = listVehiculoUsuario[0].idVehiculo?.let {
                VehiculoSeguro(
                    binding.textFechaVencimientoCartaVerde.text.toString(),
                    binding.textFechaInicioCartaVerde.text.toString(),
                    it,
                    null,
                    binding.textNumeroCartaVede.text.toString(),
                    binding.textNumeroPoliza.text.toString()
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                val response = addVehiculoSeguro?.let {
                    RetrofitObject.getCallRetrofit().saveVehiculoSeguro(
                        it
                    )
                }
                if (response != null) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            val intent = Intent()
                            intent.putExtra("idVehiculoSeguro", response.body()?.idVehiculoSeguro)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }

        } else {
            DialogAlert.showDialogAlert(
                this,
                getString(R.string.err_not_vehicle_seguro),
                R.raw.ic_caution
            )
        }
    }

    private fun comprobar(): Boolean {
        return binding.textNumeroPoliza.text.isNotEmpty()
                && listVehiculoUsuario.isNotEmpty()
    }

    private fun actualizarVehiculoSeguro() {
        if (comprobar()) {
            val updateVehiculoSeguro = listVehiculoUsuario[0].idVehiculo?.let {
                VehiculoSeguro(
                    binding.textFechaVencimientoCartaVerde.text.toString(),
                    binding.textFechaInicioCartaVerde.text.toString(),
                    it,
                    vehiculoSeguro.idVehiculoSeguro,
                    binding.textNumeroCartaVede.text.toString(),
                    binding.textNumeroPoliza.text.toString()
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                val response = updateVehiculoSeguro?.let {
                    RetrofitObject.getCallRetrofit().updateVehiculoSeguro(
                        it.idVehiculo, updateVehiculoSeguro
                    )
                }
                if (response?.isSuccessful == true) {
                    runOnUiThread {
                        val intent = Intent()
                        intent.putExtra("idVehiculoSeguro", response.body()?.idVehiculoSeguro)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun getVehiculoSeguro(idVehiculoSeguro: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getVehiculoSeguro(idVehiculoSeguro)
            if (response.isSuccessful) {
                runOnUiThread {
                    initComponent(response.body())
                }
            } else {
                Log.e("ResponseVehicle", "Error: ${response.message()}")
            }
        }
    }

    private fun getVehiculo(idVehiculo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getVehiculo(idVehiculo)
            if (response.isSuccessful) {
                runOnUiThread {
                    response.body()?.let { injectVehiculo(it) }
                }
            }
        }
    }

    private fun injectVehiculo(vehiculo: VehiculoItem) {
        listVehiculoUsuario.clear()
        listVehiculoUsuario.add(vehiculo)
        adapterVehiculo.notifyItemChanged(0)
    }

    override fun onChange(sizeList: Int) {}
    override fun onItemClick(item: VehiculoItem) {
        viewListVehiculosUsuario?.dismiss()
        injectVehiculo(item)
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_shet_vehiculo_usuario, null)


        val lista = view.findViewById<RecyclerView>(R.id.listSegurosVehiculo)
        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = adapterVehiculosRegistrados

        viewListVehiculosUsuario = BottomSheetDialog(this)
        viewListVehiculosUsuario?.setContentView(view)
        viewListVehiculosUsuario?.setCanceledOnTouchOutside(true)
        viewListVehiculosUsuario?.show()
    }
}