package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
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
import com.zcode.crashcar.adapter.SpinnerAdapterTypeVehicle
import com.zcode.crashcar.api.controller.Asegurado
import com.zcode.crashcar.api.controller.ConductorId
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.api.controller.IdVehiculoSeguro
import com.zcode.crashcar.api.controller.ListConductorDni
import com.zcode.crashcar.api.controller.ListIdVehiculoSeguro
import com.zcode.crashcar.api.controller.ListSeguros
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.databinding.ActivityNewVehiculoParteBinding
import com.zcode.crashcar.utils.Animations
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import com.zcode.crashcar.utils.objects.Circunstancia
import com.zcode.crashcar.utils.objects.Circunstancias
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
    private var myData: Boolean = false

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
        binding.btnShowDialogMyData.setOnClickListener {
            showDialogMyData()
        }
        binding.checkC1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkC2.isChecked = false
        }
        binding.checkC2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.checkC1.isChecked = false
        }
        binding.checkRemolque.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.containerDataRemolque.animation = Animations.fadeInAnimation(this)
                binding.containerDataRemolque.visibility = View.VISIBLE
            } else {
                binding.containerDataRemolque.visibility = View.GONE
                binding.containerDataRemolque.animation = Animations.fadeOutAnimation(this)
            }
        }
        binding.spinnerTypeVehicle.adapter =
            SpinnerAdapterTypeVehicle(this, Herramientas.getSpinnerItemsTypeVehicles())

        binding.imgTypeVehicle.setImageResource(
            Herramientas.getIconVehicle(
                getTypeVehicleById(0).lowercase()
            )
        )

        binding.spinnerTypeVehicle.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.imgTypeVehicle.post {
                    binding.imgTypeVehicle.setImageResource(
                        Herramientas.getIconVehicle(
                            getTypeVehicleById(position).lowercase()
                        )
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Herramientas.getSpinnerItemsPoints())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.selectPoint.adapter = adapter
    }

    private fun showDialogVerifySave() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verify_save_vehiclepart, null, false)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btn_ok_save_vehiclepart)
        val btnCancel: FrameLayout = dialogView.findViewById(R.id.btn_cancel_save_vehiclepart)


        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            if (myData) {
                guardarAsegurado()
            } else {
                guardarAsegurado()
            }
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun getTypeVehicle(): String {
        if (binding.spinnerTypeVehicle.selectedItemPosition == 0) return "Turismo"
        if (binding.spinnerTypeVehicle.selectedItemPosition == 1) return "Furgoneta/Camión"
        if (binding.spinnerTypeVehicle.selectedItemPosition == 2) return "Motocicleta"
        return ""
    }

    private fun getTypeVehicleById(id: Int): String {
        if (id == 0) return "Turismo"
        if (id == 1) return "Furgoneta/Camión"
        if (id == 2) return "Motocicleta"
        return ""
    }

    override fun onStart() {
        super.onStart()
        showDialogMyData()
    }


    private fun comprobarCircunstanciasObligatorias(): Boolean {
        return binding.checkC1.isChecked ||
                binding.checkC2.isChecked ||
                binding.checkC3.isChecked ||
                binding.checkC4.isChecked ||
                binding.checkC5.isChecked ||
                binding.checkC6.isChecked ||
                binding.checkC7.isChecked ||
                binding.checkC8.isChecked ||
                binding.checkC9.isChecked ||
                binding.checkC10.isChecked ||
                binding.checkC11.isChecked ||
                binding.checkC12.isChecked ||
                binding.checkC13.isChecked ||
                binding.checkC14.isChecked ||
                binding.checkC15.isChecked ||
                binding.checkC16.isChecked ||
                binding.checkC17.isChecked
    }

    private fun registrarVehiculoParte() {
        if (comprobarEditText()) {
            if (comprobarRemolque()) {
                if (binding.selectPoint.selectedItemPosition != 0) {
                    if (comprobarCircunstanciasObligatorias()) {
                        showDialogVerifySave()
                    } else {
                        DialogAlert.showDialogAlert(
                            this,
                            "Debes seleccionar una de las circunstancias",
                            R.raw.ic_caution
                        )
                    }
                } else {
                    DialogAlert.showDialogAlert(
                        this,
                        "Debes de seleccionar el punto de choque más significativo",
                        R.raw.ic_caution
                    )
                }
            } else {
                DialogAlert.showDialogAlert(
                    this,
                    "Si tienes remolque debes de rellenar sus datos",
                    R.raw.ic_caution
                )
            }
        } else {
            DialogAlert.showDialogAlert(
                this,
                "Debes de rellenar todos los campos obligatorios",
                R.raw.ic_caution
            )
        }
    }

    private fun comprobarRemolque(): Boolean {
        return if (binding.checkRemolque.isChecked && binding.txtMatriculaRemolque.text.isNotEmpty() && binding.txtPaisMatricula.text.isNotEmpty()) {
            true
        } else !binding.checkRemolque.isChecked
    }

    private fun comprobarEditText(): Boolean {
        return binding.txtNombre.text.isNotEmpty() &&
                binding.txtApellidos.text.isNotEmpty() &&
                binding.txtDireccion.text.isNotEmpty() &&
                binding.txtCodPostal.text.isNotEmpty() &&
                binding.txtPais.text.isNotEmpty() &&
                binding.txtPhone.text.isNotEmpty() &&
                binding.txtEmail.text.isNotEmpty() &&
                binding.txtMarca.text.isNotEmpty() &&
                binding.txtModelo.text.isNotEmpty() &&
                binding.txtMatricula.text.isNotEmpty() &&
                binding.txtPaisMatricula.text.isNotEmpty() &&
                binding.txtNombreAseguradora.text.isNotEmpty() &&
                binding.txtNPoliza.text.isNotEmpty() &&
                binding.txtAgencia.text.isNotEmpty() &&
                binding.txtDireccionAseguradora.text.isNotEmpty() &&
                binding.txtPaisAgencia.text.isNotEmpty() &&
                binding.txtTelfAgencia.text.isNotEmpty() &&
                binding.txtEmailAgencia.text.isNotEmpty() &&
                binding.txtNombreConductor.text.isNotEmpty() &&
                binding.txtApellidosConductor.text.isNotEmpty() &&
                binding.txtDireccionConductor.text.isNotEmpty() &&
                binding.txtLocalidadConductor.text.isNotEmpty() &&
                binding.txtTelfConductor.text.isNotEmpty() &&
                binding.txtEmailConductor.text.isNotEmpty() &&
                binding.txtDniConductor.text.isNotEmpty() &&
                binding.txtPaisConductor.text.isNotEmpty() &&
                binding.txtCodPostalConductor.text.isNotEmpty()
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
            myData = true
            showActionsMyData()
            alertDialog.dismiss()
        }

        btnOtherData.setOnClickListener {
            myData = false
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
        btnCloseMyData.setOnClickListener {
            myData = false
            alertDialogMyData.dismiss()
        }

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

    override fun onChange(sizeList: Int) {}

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
        vehiculoParte.idConductor = item.idConductor
        binding.txtNombreConductor.setText(item.nombre)
        binding.txtApellidosConductor.setText(item.apellidos)
        binding.txtDireccionConductor.setText(item.direccion)
        binding.txtTelfConductor.setText(item.phone)
        binding.txtEmailConductor.setText(item.email)
        binding.txtLocalidadConductor.setText(item.localidad)
        binding.txtDniConductor.setText(item.dniConductor)
        binding.txtCodPostalConductor.setText(item.codpostal.toString())
        binding.txtPaisConductor.setText(item.pais)
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

    private fun guardarAsegurado() {

        val aseguarado = Asegurado(
            null,
            binding.txtNombre.text.toString(),
            binding.txtApellidos.text.toString(),
            binding.txtDireccion.text.toString(),
            binding.txtCodPostal.text.toString(),
            binding.txtPais.text.toString(),
            binding.txtPhone.text.toString(),
            binding.txtEmail.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveAsegurado(aseguarado)
                if (response.isSuccessful) {
                    runOnUiThread {
                        vehiculoParte.idAsegurado = response.body()?.id
                        if (myData) {
                            createNewVehiculoParte()
                        } else {
                            guardarVehiculo()
                        }
                    }
                } else {
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    DialogAlert.showDialogAlert(
                        this@NewVehiculoParteActivity,
                        "No es posible conectar con los servidores de Crash Car",
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun guardarVehiculo() {
        val typeVehicle = getTypeVehicle()
        val vehiculoItem = VehiculoItem(
            true,
            "",
            binding.txtMarca.text.toString(),
            binding.txtMatricula.text.toString(),
            binding.txtModelo.text.toString(),
            binding.txtPaisMatricula.text.toString(),
            typeVehicle
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().newVehiculo(vehiculoItem)
                if (response.isSuccessful) {
                    runOnUiThread {
                        guardarVehiculoSeguro(response.body()?.idVehiculo)
                    }
                } else {
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    DialogAlert.showDialogAlert(
                        this@NewVehiculoParteActivity,
                        "No es posible conectar con los servidores de Crash Car",
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun guardarVehiculoSeguro(idVehiculo: Int?) {
        if (idVehiculo != null) {
            val vehiculoSegurosItem = VehiculoSeguro(
                binding.textFechaVencimientoCartaVerde.text.toString(),
                binding.textFechaInicioCartaVerde.text.toString(),
                idVehiculo,
                null,
                binding.txtNCartaVerde.text.toString(),
                binding.txtNPoliza.text.toString()
            )
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitObject.getCallRetrofit().saveVehiculoSeguro(vehiculoSegurosItem)
                    if (response.isSuccessful) {
                        runOnUiThread {
                            vehiculoParte.idVehiculo = response.body()?.idVehiculoSeguro
                            guardarConductor(response.body()?.idVehiculoSeguro)
                        }
                    } else {
                        runOnUiThread {
                            DialogAlert.showDialogAlert(
                                this@NewVehiculoParteActivity,
                                "No es posible conectar con los servidores de Crash Car",
                                R.raw.ic_connection_error
                            )
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                    }
                }
            }
        }
    }

    private fun guardarConductor(idVehiculoSeguro: Int?) {
        if (idVehiculoSeguro != null) {
            val conductorItem = ConductorItem(
                binding.txtApellidosConductor.text.toString(),
                binding.txtCodPostalConductor.text.toString().toInt(),
                binding.txtDireccionConductor.text.toString(),
                binding.txtDniConductor.text.toString(),
                binding.txtEmailConductor.text.toString(),
                null,
                binding.txtLocalidadConductor.text.toString(),
                binding.txtNombreConductor.text.toString(),
                binding.txtPaisConductor.text.toString(),
                binding.txtTelfConductor.text.toString()
            )
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitObject.getCallRetrofit().saveConductor(conductorItem)
                    if (response.isSuccessful) {
                        runOnUiThread {
                            vehiculoParte.idConductor = response.body()?.idConductor
                            guardarSeguro(idVehiculoSeguro, response.body()?.idConductor ?: 0)
                        }
                    } else {
                        runOnUiThread {
                            DialogAlert.showDialogAlert(
                                this@NewVehiculoParteActivity,
                                "No es posible conectar con los servidores de Crash Car",
                                R.raw.ic_connection_error
                            )
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                    }
                }
            }
        }
    }

    private fun guardarSeguro(idVehiculoSeguro: Int, idConductor: Int) {
        val segurosItem = SegurosItem(
            true,
            binding.txtDireccionAseguradora.text.toString(),
            "",
            binding.txtEmailAgencia.text.toString(),
            null,
            Gson().toJson(ArrayList<ConductorId>().add(ConductorId(idConductor))),
            Gson().toJson(ListIdVehiculoSeguro().add(IdVehiculoSeguro(idVehiculoSeguro))),
            binding.txtNombreAseguradora.text.toString(),
            binding.txtNombreAseguradora.text.toString(),
            binding.txtPaisAgencia.text.toString(),
            binding.txtTelfAgencia.text.toString()
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveSeguro(segurosItem)
                if (response.isSuccessful) {
                    runOnUiThread {
                        vehiculoParte.idSeguro = response.body()?.idSeguro ?: 0
                        createNewVehiculoParte()
                    }
                } else {
                    runOnUiThread {
                        DialogAlert.showDialogAlert(
                            this@NewVehiculoParteActivity,
                            "No es posible conectar con los servidores de Crash Car",
                            R.raw.ic_connection_error
                        )
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    DialogAlert.showDialogAlert(
                        this@NewVehiculoParteActivity,
                        "No es posible conectar con los servidores de Crash Car",
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun createNewVehiculoParte() {
        val newVehiculoParte = VehiculoParte(
            null,
            vehiculoParte.idVehiculo,
            vehiculoParte.idSeguro,
            getCircunstancias(),
            binding.checkRemolque.isChecked,
            binding.txtMatriculaRemolque.text.toString(),
            binding.txtPaisMatriculaRemolque.text.toString(),
            binding.selectPoint.selectedItem.toString(),
            "",
            binding.txtObservaciones.text.toString(),
            vehiculoParte.idAsegurado,
            vehiculoParte.idConductor
        )
        val intent = Intent()
        intent.putExtra("vehiculoParte", Gson().toJson(newVehiculoParte))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun getCircunstancias(): String {
        val circunstancias = Circunstancias()
        circunstancias.addAll(
            listOf(
                Circunstancia("1", binding.checkC1.isChecked),
                Circunstancia("2", binding.checkC2.isChecked),
                Circunstancia("3", binding.checkC3.isChecked),
                Circunstancia("4", binding.checkC4.isChecked),
                Circunstancia("5", binding.checkC5.isChecked),
                Circunstancia("6", binding.checkC6.isChecked),
                Circunstancia("7", binding.checkC7.isChecked),
                Circunstancia("8", binding.checkC8.isChecked),
                Circunstancia("9", binding.checkC9.isChecked),
                Circunstancia("10", binding.checkC10.isChecked),
                Circunstancia("11", binding.checkC11.isChecked),
                Circunstancia("12", binding.checkC12.isChecked),
                Circunstancia("13", binding.checkC13.isChecked),
                Circunstancia("14", binding.checkC14.isChecked),
                Circunstancia("15", binding.checkC15.isChecked),
                Circunstancia("16", binding.checkC16.isChecked),
                Circunstancia("17", binding.checkC17.isChecked)
            )
        )
        return Gson().toJson(circunstancias)
    }
}


