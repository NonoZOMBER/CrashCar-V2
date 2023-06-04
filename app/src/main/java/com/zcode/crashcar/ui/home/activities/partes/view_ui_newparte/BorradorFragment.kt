package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterImagesParte
import com.zcode.crashcar.adapter.AdapterItemTestigo
import com.zcode.crashcar.adapter.AdapterVehiculoParte
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.TestigoItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.databinding.FragmentBorradorBinding
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BorradorFragment : Fragment() {
    private lateinit var binding: FragmentBorradorBinding
    private lateinit var parte: ParteItem
    private lateinit var vehiculoA: VehiculoParte
    private lateinit var vehiculoB: VehiculoParte
    private lateinit var listImages: ArrayList<Bitmap>
    private lateinit var listVehiculoA: ArrayList<VehiculoParte>
    private lateinit var listVehiculoB: ArrayList<VehiculoParte>
    private lateinit var listTestigos: ArrayList<TestigoItem>
    private lateinit var adapterVehiculoA: AdapterVehiculoParte
    private lateinit var adapterVehiculoB: AdapterVehiculoParte
    private lateinit var adapterImagesParte: AdapterImagesParte
    private lateinit var adapterTestigoItem: AdapterItemTestigo
    private var firmaA = false
    private val firmaB = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBorradorBinding.inflate(inflater, container, false)
        parte = Gson().fromJson(
            (arguments?.getString("parteItem", "").toString()),
            ParteItem::class.java
        )
        vehiculoA = Gson().fromJson(
            (arguments?.getString("vehiculoA", "").toString()),
            VehiculoParte::class.java
        )
        vehiculoB = Gson().fromJson(
            (arguments?.getString("vehiculoB", "").toString()),
            VehiculoParte::class.java
        )
        listImages = ArrayList()
        listTestigos = ArrayList()
        val listTypeImages = object : TypeToken<ArrayList<Bitmap>>() {}.type
        listImages.addAll(Gson().fromJson(parte.imagenes, listTypeImages))
        val listTypeTestigos = object : TypeToken<ArrayList<TestigoItem>>() {}.type
        listTestigos.addAll(Gson().fromJson(parte.testigo, listTypeTestigos))
        initComponent()
        return binding.root
    }

    private fun initComponent() {
        loadData()
        binding.btnSaveParte.setOnClickListener {
            firmaVehiculoA()
        }
    }

    private fun firmaVehiculoA() {
        val intent = Intent(requireContext(), SignActivity::class.java)
        intent.putExtra("turno", "Vehículo A")
        resultFirmaA.launch(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private val resultFirmaA =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                listVehiculoA[0].firma = it.data?.getStringExtra("firma").toString()
                val intent = Intent(requireContext(), SignActivity::class.java)
                intent.putExtra("turno", "Vehículo B")
                resultFirmaB.launch(intent)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            } else {
                Log.i("Response", "No se ha obtenido firma")
                DialogAlert.showDialogAlert(requireContext(), "Debes de firmar el Parte", R.raw.ic_caution)
            }
        }

    private val resultFirmaB =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                listVehiculoB[0].firma = it.data?.getStringExtra("firma").toString()
                guardarVehiculoA()
            } else {
                Log.i("Response", "No se ha obtenido firma")
                DialogAlert.showDialogAlert(requireContext(), "Debes de firmar el Parte", R.raw.ic_caution)
            }
        }

    private fun guardarVehiculoA() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveVehiculoParte(listVehiculoA[0])
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        guardarVehiculoB(response.body()?.id)
                    }
                } else {
                    Log.e("Response", "No hay response vehiculo A")
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    DialogAlert.showDialogAlert(requireContext(), "No es posible conectar con los servicios de CrashCar en este momento, intentelo de nuevo más tarde", R.raw.ic_connection_error)
                }
            }
        }
    }

    private fun guardarVehiculoB(idVehicleA: Int?) {
        if (idVehicleA != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitObject.getCallRetrofit().saveVehiculoParte(listVehiculoB[0])
                    if (response.isSuccessful) {
                        requireActivity().runOnUiThread {
                            guardarVehiculos(response.body()?.id, idVehicleA)
                            guardarTestigos()
                        }
                    } else {
                        Log.e("Response", "No hay response vehiculo b")
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        DialogAlert.showDialogAlert(requireContext(), "No es posible conectar con los servicios de CrashCar en este momento, intentelo de nuevo más tarde", R.raw.ic_connection_error)
                    }
                }
            }
        }
    }

    private lateinit var idsTestigos: ArrayList<Int>

    private fun guardarTestigos() {
        if (listTestigos.isNotEmpty()) {
            for (testigo in listTestigos) {
                saveTestigo(testigo)
            }
        } else {
            guardarParte()
        }
    }

    private fun saveTestigo(testigoItem: TestigoItem) {
        idsTestigos = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveTestigo(testigoItem)
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        response.body()?.let {
                            response.body()!!.id?.let { it1 -> idsTestigos.add(it1) }
                            listTestigos.remove(testigoItem)
                            if (listTestigos.isEmpty()) {
                                parte.testigo = Gson().toJson(idsTestigos)
                                guardarParte()
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        DialogAlert.showDialogAlert(
                            requireContext(),
                            getString(R.string.error_connection),
                            R.raw.ic_connection_error
                        )
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    DialogAlert.showDialogAlert(
                        requireContext(),
                        getString(R.string.error_connection),
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun guardarVehiculos(idVehicleB: Int?, idVehicleA: Int) {
        if (idVehicleB != null) {
            parte.vehiculosParte = Gson().toJson(arrayListOf(idVehicleA, idVehicleB))
        }
    }

    private fun guardarParte() {
        parte.idUsuario = prefsSetting.getIdUser()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveParte(parte)
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        requireActivity().finish()
                    }
                } else {
                    Log.e("Response", "No hay response parte")
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    DialogAlert.showDialogAlert(
                        requireContext(),
                        getString(R.string.error_connection),
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun loadData() {
        listVehiculoA = ArrayList()
        listVehiculoA.add(vehiculoA)
        listVehiculoB = ArrayList()
        listVehiculoB.add(vehiculoB)
        adapterImagesParte = AdapterImagesParte(listImages, false)
        adapterTestigoItem = AdapterItemTestigo(listTestigos, null, false)
        adapterVehiculoA = AdapterVehiculoParte(listVehiculoA)
        adapterVehiculoB = AdapterVehiculoParte(listVehiculoB)
        binding.recyclerTestigos.layoutManager = LinearLayoutManager(requireContext())
        binding.listImages.layoutManager = LinearLayoutManager(requireContext())
        binding.listVehiculoA.layoutManager = LinearLayoutManager(requireContext())
        binding.listVehiculoB.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTestigos.adapter = adapterTestigoItem
        binding.listImages.adapter = adapterImagesParte
        binding.listVehiculoA.adapter = adapterVehiculoA
        binding.listVehiculoB.adapter = adapterVehiculoB
        binding.textFechaAccidente.setText(parte.fechAccidente)
        binding.textHoraAccidente.setText(parte.horaAccidente)
        binding.textPaisAccidente.setText(parte.paisAccidente)
        binding.textLocationAccidente.setText(parte.direccion)
        binding.checkVictimas.isChecked = parte.visctimas
        binding.checkDamageObjetos.isChecked = parte.damageMaterial
        binding.checkOtherVehicles.isChecked = parte.isOtherVehicles
    }
}