package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemConductor
import com.zcode.crashcar.adapter.AdapterItemSeguros
import com.zcode.crashcar.adapter.AdapterItemVehiculoSeguro
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.api.controller.ListConductorDni
import com.zcode.crashcar.api.controller.ListIdVehiculoSeguro
import com.zcode.crashcar.api.controller.ListSeguros
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.databinding.ActivityNewVehiculoParteBinding
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewVehiculoParteActivity : AppCompatActivity(), AdapterItemSeguros.OnSizeChangeListener,
    AdapterItemVehiculoSeguro.OnItemClickListener, AdapterItemConductor.OnItemClickListener {
    private lateinit var binding: ActivityNewVehiculoParteBinding
    private lateinit var vehiculoParte: VehiculoParte
    private lateinit var btnCloseMyData: ImageButton
    private lateinit var recyclerMyData: RecyclerView
    private lateinit var titleMyData: TextView
    private lateinit var progressBarData: LottieAnimationView
    private lateinit var viewMyData: LinearLayout
    private lateinit var alertDialogMyData: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewVehiculoParteBinding.inflate(layoutInflater)
        vehiculoParte = VehiculoParte()
        initComponent()
        setContentView(binding.root)
    }

    private fun initComponent() {
        binding.btnRegitrarVehiculoParte.setOnClickListener {
            registrarVehiculoParte()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnShowDialoMyData.setOnClickListener {
            showDialogMyData()
        }
    }

    override fun onStart() {
        super.onStart()
        showDialogMyData()
    }

    private fun registrarVehiculoParte() {
        //Hacer comprobaciones de datos introducidos antes de reegistrar
    }

    private fun showDialogMyData() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_my_data, null, false)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnMyData: TextView = dialogView.findViewById(R.id.btnMyData)
        val btnOtherData: TextView = dialogView.findViewById(R.id.btnOtherData)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        alertDialog.setCancelable(false)

        btnMyData.setOnClickListener {
            showActionsMyData()
            alertDialog.dismiss()
        }

        btnOtherData.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showActionsMyData() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_my_data_actions, null, false)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        btnCloseMyData = dialogView.findViewById(R.id.btnCloseMyData)
        recyclerMyData = dialogView.findViewById(R.id.recyclerMyData)
        titleMyData = dialogView.findViewById(R.id.titleDialogMyData)
        progressBarData = dialogView.findViewById(R.id.progress_bar_my_data)
        viewMyData = dialogView.findViewById(R.id.viewMyDataActions)

        alertDialogMyData = dialogBuilder.create()
        alertDialogMyData.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        alertDialogMyData.setCancelable(false)

        recyclerMyData.layoutManager = LinearLayoutManager(applicationContext)
        loadSegurosMyData()
        btnCloseMyData.setOnClickListener { alertDialogMyData.dismiss() }

        alertDialogMyData.show()
    }

    private fun loadSegurosMyData() {
        val listSeguros = ListSeguros()
        stateMyData(1)
        titleMyData.text = getString(R.string.lblSelectSeguro)
        val adapterSeguros = AdapterItemSeguros(listSeguros, this, true, click = false)
        recyclerMyData.adapter = adapterSeguros
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    RetrofitObject.getCallRetrofit().getListSeguros(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    runOnUiThread {
                        response.body()?.let { listSeguros.addAll(it) }
                        adapterSeguros.notifyDataSetChanged()
                        stateMyData(0)
                    }
                } else {
                    Log.e("Response", "Error en la respuesta")
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                        alertDialogMyData.dismiss()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    DialogAlert.showDialogAlert(
                        this@NewVehiculoParteActivity,
                        "No es posible conectar con los servidores de Crash Car",
                        R.raw.ic_connection_error
                    )
                    alertDialogMyData.dismiss()
                }
            }
        }
    }

    private fun stateMyData(i: Int) {
        when (i) {
            1 -> {
                viewMyData.visibility = View.GONE
                progressBarData.visibility = View.VISIBLE
            }

            0 -> {
                viewMyData.visibility = View.VISIBLE
                progressBarData.visibility = View.GONE
            }
        }
    }

    override fun onChange(sizeList: Int) {
        //Nada
    }

    override fun onItem(item: SegurosItem) {
        vehiculoParte.idSeguro = item.idSeguro
        binding.txtNombreAseguradora.setText(item.nombreAgencia)
        binding.txtAgencia.setText(item.nombreAgencia)
        binding.txtDireccionAseguradora.setText(item.direccionAgencia)
        binding.txtTelfAgencia.setText(item.phoneAgencia)
        binding.txtPaisAgencia.setText(item.paisAgencia)
        binding.txtEmailAgencia.setText(item.emailAgencia)
        loadVehiculosSeguro(item)
    }

    private lateinit var segurosItem: SegurosItem
    private lateinit var listVehiculosSeguro: ArrayList<VehiculoSeguro>
    private lateinit var adapterVehiculoSeguro: AdapterItemVehiculoSeguro

    private fun loadVehiculosSeguro(item: SegurosItem) {
        titleMyData.text = getString(R.string.lblSelectVehiculoSeguro)
        listVehiculosSeguro = ArrayList()
        segurosItem = item
        adapterVehiculoSeguro = AdapterItemVehiculoSeguro(listVehiculosSeguro, null, this, false)
        recyclerMyData.adapter = adapterVehiculoSeguro
        loadVehiculos(item)
    }

    override fun onItemClick(item: VehiculoSeguro, position: Int) {
        stateMyData(1)
        binding.txtNPoliza.setText(item.numeroPoliza)
        binding.txtNCartaVerde.setText(item.numeroCartaVerde)
        binding.textFechaInicioCartaVerde.setText(item.fechaCartaVerdeInicio)
        binding.textFechaVencimientoCartaVerde.setText(item.fechaCartaVerdeFin)
        getLoadDataVehicle(item.idVehiculo)
        vehiculoParte.idVehiculo = item.idVehiculoSeguro
    }

    private fun getLoadDataVehicle(idVehiculo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getVehiculo(idVehiculo)
                if (response.isSuccessful) {
                    runOnUiThread {
                        val vehiculo = response.body()
                        if (vehiculo != null) {
                            binding.txtMatricula.setText(vehiculo.matricula)
                            binding.txtModelo.setText(vehiculo.modelo)
                            binding.txtMarca.setText(vehiculo.marca)
                            binding.txtPaisMatricula.setText(vehiculo.paisMatricula)
                            binding.imgTypeVehicle.post {
                                binding.imgTypeVehicle.setImageResource(
                                    Herramientas.getIconVehicle(
                                        vehiculo.tipoVehiculo.lowercase()
                                    )
                                )
                            }
                        }
                        loadConductoresSeguro(segurosItem)
                    }
                } else {
                    Log.e("Response", "Error en la respuesta")
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                        alertDialogMyData.dismiss()
                    }
                }
            } catch (e: Exception) {
                DialogAlert.showDialogAlert(
                    this@NewVehiculoParteActivity,
                    "No es posible conectar con los servidores de Crash Car",
                    R.raw.ic_connection_error
                )
                alertDialogMyData.dismiss()
            }
        }
    }


    private fun loadVehiculos(
        item: SegurosItem,
    ) {
        if (item.idsVehiculosSeguro.isNotEmpty()) {
            listVehiculosSeguro.clear()
            adapterVehiculoSeguro.notifyDataSetChanged()
            val idVehiculoSeguro =
                Gson().fromJson(item.idsVehiculosSeguro, ListIdVehiculoSeguro::class.java)
            for (vehicle in idVehiculoSeguro) {
                getVehiculoSeguro(vehicle.idVehiculoSeguro)
            }
            stateMyData(0)
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
            listVehiculosSeguro.add(vehicleSeguro)
            adapterVehiculoSeguro.notifyItemInserted(listVehiculosSeguro.size - 1)
        }
    }

    private lateinit var listConductoresSeguro: ArrayList<ConductorItem>
    private lateinit var adapterConductores: AdapterItemConductor

    private fun loadConductoresSeguro(segurosItem: SegurosItem) {
        titleMyData.text = getString(R.string.lblSelectConductor)
        listConductoresSeguro = ArrayList()
        adapterConductores = AdapterItemConductor(listConductoresSeguro, null, this, false)
        recyclerMyData.adapter = adapterConductores
        loadConductores(segurosItem.idsConductoresSeguro)
    }

    private fun loadConductores(idsConductoresSeguro: String) {
        if (idsConductoresSeguro.isNotEmpty()) {
            listConductoresSeguro.clear()
            adapterConductores.notifyDataSetChanged()
            val listIdConductor =
                Gson().fromJson(idsConductoresSeguro, ListConductorDni::class.java)
            for (idConductor in listIdConductor) {
                getConductor(idConductor.id)
            }
            stateMyData(0)
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

    private fun saveDriver(conductor: ConductorItem?) {
        if (conductor != null) {
            listConductoresSeguro.add(conductor)
            adapterConductores.notifyItemInserted(listConductoresSeguro.size - 1)
        }
    }

    override fun onItemClick(item: ConductorItem, position: Int) {
        stateMyData(1)
        binding.txtNombreConductor.setText(item.nombre)
        binding.txtApellidosConductor.setText(item.apellidos)
        binding.txtDireccionConductor.setText(item.direccion)
        binding.txtTelfConductor.setText(item.phone)
        binding.txtEmailConductor.setText(item.email)
        binding.txtLocalidadConductor.setText(item.localidad)
        loadDataAsegurado()
    }

    private fun loadDataAsegurado() {
        val user = prefsSetting.getUser()
        if (user != null) {
            binding.txtNombre.setText(user.nombre)
            binding.txtApellidos.setText(user.apellidos)
            binding.txtDireccion.setText(user.direccion)
            binding.txtCodPostal.setText(user.codpostal)
            binding.txtPais.setText(user.pais)
            binding.txtPhone.setText(user.telefono)
            binding.txtEmail.setText(user.email)
        }
        alertDialogMyData.dismiss()
        showVerifySuccess()
    }

    private fun showVerifySuccess() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verify_change_password, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedVerify)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgDialog)

        imgLottie.setAnimation(R.raw.ic_verify_animated)

        textMsg.text = getString(R.string.verifyMyDataInjected)

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        alertDialog.show()

        imgLottie.playAnimation()

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}