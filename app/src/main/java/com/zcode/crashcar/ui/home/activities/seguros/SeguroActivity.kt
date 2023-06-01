package com.zcode.crashcar.ui.home.activities.seguros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemConductor
import com.zcode.crashcar.adapter.AdapterItemVehiculoSeguro
import com.zcode.crashcar.api.controller.ConductorId
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.api.controller.IdVehiculoSeguro
import com.zcode.crashcar.api.controller.ListConductorDni
import com.zcode.crashcar.api.controller.ListIdVehiculoSeguro
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.databinding.ActivitySeguroBinding
import com.zcode.crashcar.ui.home.activities.conductores.ConductorActivity
import com.zcode.crashcar.ui.home.activities.vehiculos.VehiculoSeguroActivity
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeguroActivity : AppCompatActivity(), AdapterItemConductor.OnSizeChangeListener,
    AdapterItemVehiculoSeguro.OnItemClickListener,
    AdapterItemVehiculoSeguro.OnSizeChangeListener, AdapterItemConductor.OnItemClickListener {
    private lateinit var binding: ActivitySeguroBinding
    private lateinit var listConductores: ArrayList<ConductorItem>
    private lateinit var listVehiculoSeguro: ArrayList<VehiculoSeguro>
    private lateinit var adapterConductores: AdapterItemConductor
    private lateinit var adapterItemVehiculoSeguro: AdapterItemVehiculoSeguro
    private var isVehiculoSeguro = false
    private var isConductor = false
    private var position = 0
    private lateinit var seguro: SegurosItem
    private var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeguroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        listVehiculoSeguro = ArrayList()
        listConductores = ArrayList()

        val idSeguro = intent.extras?.getInt("seguro", 0)

        if (idSeguro != null && idSeguro != 0) {
            update = true
            getSeguro(idSeguro)
        } else {
            update = false
            initComponent(null)
        }
    }

    private fun initComponent(seguro: SegurosItem?) {

        adapterConductores = AdapterItemConductor(listConductores, this, this, true)
        binding.listConductorNewSeguro.layoutManager = LinearLayoutManager(this)
        binding.listConductorNewSeguro.adapter = adapterConductores

        adapterItemVehiculoSeguro = AdapterItemVehiculoSeguro(listVehiculoSeguro, this, this, true)
        binding.listVehiculoSeguro.layoutManager = LinearLayoutManager(this)
        binding.listVehiculoSeguro.adapter = adapterItemVehiculoSeguro

        if (seguro != null) {
            this.seguro = seguro
            binding.lblSeguroActivity.text = getString(R.string.lblEditandoSeguro)
            binding.textNombreAgencia.setText(seguro.nombreAgencia)
            binding.textDireccionAgencia.setText(seguro.direccionAgencia)
            binding.textphoneAgencia.setText(seguro.phoneAgencia)
            binding.textPaisAgencia.setText(seguro.paisAgencia)
            binding.textEmailAgencia.setText(seguro.emailAgencia)
            loadConductores(seguro.idsConductoresSeguro)
            loadVehiculos(seguro.idsVehiculosSeguro)
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRegitrarSeguro.setOnClickListener {
            if (update) {
                actualizarSeguro()
            } else {
                guardarSeguro()
            }
        }
        binding.btnNewVehiculoSeguro.setOnClickListener {
            registerResultVehiculoUsuario.launch(
                Intent(this, VehiculoSeguroActivity::class.java)
            )
        }
        binding.btnNewConductorSeguro.setOnClickListener {
            registerResultConductor.launch(
                Intent(
                    this,
                    ConductorActivity::class.java
                )
            )
        }
    }

    private fun getSeguro(idSeguro: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getSeguro(idSeguro)
            if (response.isSuccessful) {
                runOnUiThread {
                    initComponent(response.body())
                }
            } else {
                Log.e("ResponseSeguro", response.body().toString())
            }
        }
    }

    private fun loadVehiculos(idsVehiculosSeguro: String) {
        if (idsVehiculosSeguro.isNotEmpty()) {
            listVehiculoSeguro.clear()
            adapterItemVehiculoSeguro.notifyDataSetChanged()
            val idVehiculoSeguro =
                Gson().fromJson(idsVehiculosSeguro, ListIdVehiculoSeguro::class.java)
            for (vehicle in idVehiculoSeguro) {
                getVehiculoSeguro(vehicle.idVehiculoSeguro)
            }
        }
    }

    private fun getVehiculoSeguro(idVehiculoSeguro: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getVehiculoSeguro(idVehiculoSeguro)
            if (response.isSuccessful) runOnUiThread { saveVehicleSeguro(response.body()) } else Log.e(
                "ResponseVehicleSeguro",
                response.body().toString()
            )
        }
    }

    private fun saveVehicleSeguro(vehicleSeguro: VehiculoSeguro?) {
        if (vehicleSeguro != null) {
            listVehiculoSeguro.add(vehicleSeguro)
            adapterItemVehiculoSeguro.notifyItemInserted(listVehiculoSeguro.size - 1)
        }
    }

    private fun loadConductores(idsConductoresSeguro: String) {
        if (idsConductoresSeguro.isNotEmpty()) {
            listConductores.clear()
            adapterConductores.notifyDataSetChanged()
            val listIdConductor =
                Gson().fromJson(idsConductoresSeguro, ListConductorDni::class.java)
            for (idConductor in listIdConductor) {
                getConductor(idConductor.id)
            }
        }
    }

    private fun saveDriver(conductor: ConductorItem?) {
        if (conductor != null) {
            listConductores.add(conductor)
            adapterConductores.notifyItemInserted(listConductores.size - 1)
        }
    }

    private fun comprobarListas(): Boolean {
        return listConductores.isNotEmpty() &&
                listVehiculoSeguro.isNotEmpty()
    }

    private fun guardarSeguro() {
        if (comprobarDatos() && comprobarListas()) {

            val seguroAdd = SegurosItem(
                true,
                binding.textDireccionAgencia.text.toString(),
                prefsSetting.getIdUser(),
                binding.textEmailAgencia.text.toString(),
                null,
                idsConductores(),
                idsVehiculos(),
                binding.textNombreAgencia.text.toString(),
                binding.textNombreAgencia.text.toString(),
                binding.textPaisAgencia.text.toString(),
                binding.textphoneAgencia.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().saveSeguro(seguroAdd)
                if (response.isSuccessful) {
                    runOnUiThread { finish() }
                }
            }

        } else {
            if (listVehiculoSeguro.isEmpty() || listConductores.isEmpty()) {
                DialogAlert.showDialogAlert(
                    this,
                    "Debes registrar un vehículo y un conductor como mínimo",
                    R.raw.ic_caution
                )
            } else if (!comprobarDatos()) {
                DialogAlert.showDialogAlert(
                    this,
                    "Debes de rellenar todos los campos",
                    R.raw.ic_caution
                )
            }
        }
    }

    private fun idsVehiculos(): String {
        val listIdVehiculoSeguro: ArrayList<IdVehiculoSeguro> = ArrayList()
        for (vehiculo in listVehiculoSeguro) {
            listIdVehiculoSeguro.add(IdVehiculoSeguro(vehiculo.idVehiculoSeguro ?: 0))
        }
        return Gson().toJson(listIdVehiculoSeguro)
    }

    private fun idsConductores(): String {
        val listIdsConductores: ArrayList<ConductorId> = ArrayList()
        for (conductor in listConductores) {
            listIdsConductores.add(ConductorId(conductor.idConductor ?: 0))
        }
        return Gson().toJson(listIdsConductores)
    }

    private fun comprobarDatos(): Boolean {
        return binding.textNombreAgencia.text.isNotEmpty() &&
                binding.textDireccionAgencia.text.isNotEmpty() &&
                binding.textphoneAgencia.text.isNotEmpty() &&
                binding.textEmailAgencia.text.isNotEmpty() &&
                binding.textPaisAgencia.text.isNotEmpty()
    }

    private fun actualizarSeguro() {
        if (comprobarDatos() && comprobarListas()) {

            val seguroUpdate = SegurosItem(
                true,
                binding.textDireccionAgencia.text.toString(),
                prefsSetting.getIdUser(),
                binding.textEmailAgencia.text.toString(),
                seguro.idSeguro,
                idsConductores(),
                idsVehiculos(),
                binding.textNombreAgencia.text.toString(),
                binding.textNombreAgencia.text.toString(),
                binding.textPaisAgencia.text.toString(),
                binding.textphoneAgencia.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response = seguroUpdate.idSeguro?.let {
                    RetrofitObject.getCallRetrofit().updateSeguro(
                        it, seguroUpdate
                    )
                }
                if (response != null) {
                    if (response.isSuccessful) {
                        runOnUiThread { finish() }
                    }
                }
            }

        } else {
            if (listVehiculoSeguro.isEmpty() || listConductores.isEmpty())
                Toast.makeText(
                    this,
                    "Debe de registrarse como mínimo un conductor y un vehiculo registrado",
                    Toast.LENGTH_SHORT
                ).show()

            if (!comprobarDatos()) {
                DialogAlert.showDialogAlert(
                    this,
                    "Debes de rellenar todos los campos",
                    R.raw.ic_caution
                )
            }
        }
    }

    private val registerResultConductor =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val idConductor = it.data?.getIntExtra("idConductor", 0)
                if (idConductor != null && idConductor != 0) {
                    getConductor(idConductor)
                }
            } else {
                Log.i("ResponseConductor", "No se ha obtenido ningún conductor")
            }
        }

    private val registerResultVehiculoUsuario =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val idVehiculoSeguro = it.data?.getIntExtra("idVehiculoSeguro", 0)
                if (idVehiculoSeguro != null && idVehiculoSeguro != 0) {
                    getVehiculoSeguro(idVehiculoSeguro)
                }
            } else {
                Log.i("ResponseConductor", "No se ha obtenido ningún conductor")
            }
        }

    private val updateResultVehiculoUsuario =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val idVehiculoSeguro = it.data?.getIntExtra("idVehiculoSeguro", 0)
                if (idVehiculoSeguro != null && idVehiculoSeguro != 0) {
                    updateVehiculoSeguro(idVehiculoSeguro, position)
                }
            }
        }

    private val updateResultConductor =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val idConductor = it.data?.getIntExtra("idConductor", 0)
                if (idConductor != null && idConductor != 0) {
                    updateConductor(idConductor, position)
                }
            } else {
                Log.i("ResponseConductor", "No se ha obtenido ningún conductor")
            }
        }

    private fun updateConductor(idConductor: Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getConductor(idConductor)
            if (response.isSuccessful) {
                runOnUiThread {
                    listConductores[position] = response.body()!!
                    adapterConductores.notifyItemChanged(position)
                }
            } else {
                Log.e(
                    "ResponseDriver",
                    response.body().toString()
                )
            }
        }
    }

    private fun updateVehiculoSeguro(idVehiculoSeguro: Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getVehiculoSeguro(idVehiculoSeguro)
            if (response.isSuccessful) {
                runOnUiThread {
                    listVehiculoSeguro[position] = response.body()!!
                    adapterItemVehiculoSeguro.notifyItemChanged(position)
                }
            } else {
                Log.e(
                    "ResponseVehicleSeguro",
                    response.body().toString()
                )
            }
        }
    }

    private fun getConductor(idConductor: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().getConductor(idConductor)
            if (response.isSuccessful) runOnUiThread { saveDriver(response.body()) } else Log.e(
                "ResponseDriver",
                response.body().toString()
            )
        }
    }

    override fun onChangeListConductor(position: Int) {
        listConductores.removeAt(position)
        adapterConductores.notifyItemRemoved(position)
    }

    override fun onChangeListVehiculoUsuario(position: Int) {
        listVehiculoSeguro.removeAt(position)
        adapterItemVehiculoSeguro.notifyItemRemoved(position)
    }

    override fun onItemClick(item: VehiculoSeguro, position: Int) {
        isVehiculoSeguro = true
        update = true
        this.position = position
        val intent = Intent(this, VehiculoSeguroActivity::class.java)
        intent.putExtra("idVehiculoSeguro", item.idVehiculoSeguro)
        updateResultVehiculoUsuario.launch(intent)
    }

    override fun onItemClick(item: ConductorItem, position: Int) {
        isConductor = true
        update = true
        this.position = position
        val intent = Intent(this, ConductorActivity::class.java)
        intent.putExtra("conductor", item.idConductor)
        startActivity(intent)
    }
}