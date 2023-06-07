package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.github.gcacace.signaturepad.views.SignaturePad
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
import java.io.ByteArrayOutputStream


class BorradorFragment : Fragment() {
    private lateinit var binding: FragmentBorradorBinding
    private lateinit var parte: ParteItem
    private lateinit var vehiculoA: VehiculoParte
    private lateinit var vehiculoB: VehiculoParte
    private lateinit var listImages: ArrayList<String>
    private lateinit var listVehiculoA: ArrayList<VehiculoParte>
    private lateinit var listVehiculoB: ArrayList<VehiculoParte>
    private lateinit var listTestigos: ArrayList<TestigoItem>
    private lateinit var adapterVehiculoA: AdapterVehiculoParte
    private lateinit var adapterVehiculoB: AdapterVehiculoParte
    private lateinit var adapterImagesParte: AdapterImagesParte
    private lateinit var adapterTestigoItem: AdapterItemTestigo

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
        val listTypeImages = object : TypeToken<ArrayList<String>>() {}.type
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
        binding.progressBar.visibility = View.VISIBLE
        binding.viewBorrador.visibility = View.GONE
        showDialogFirma("A")
    }

    private fun showDialogFirma(s: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sign, null, false)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)

        val btnOk: Button = dialogView.findViewById(R.id.btn_firmar)
        val textMsg: TextView = dialogView.findViewById(R.id.title_ventana)
        val signaturePad: SignaturePad = dialogView.findViewById(R.id.signaturePad)

        textMsg.text = String.format(getString(R.string.lbl_firma), s)

        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        btnOk.setOnClickListener {
            if (s == "A") {
                vehiculoA.firma = bitmapToBase64(signaturePad.signatureBitmap)
                alertDialog.dismiss()
                showDialogFirma("B")
            } else if (s == "B") {
                vehiculoB.firma = bitmapToBase64(signaturePad.signatureBitmap)
                alertDialog.dismiss()
                guardarVehiculoA()
            }
        }
        alertDialog.show()
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
                    desactivarProgressBar()
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
                        desactivarProgressBar()
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
                    desactivarProgressBar()
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
        parte.activo = true
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
                    desactivarProgressBar()
                }
            }
        }
    }

    private fun desactivarProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.viewBorrador.visibility = View.VISIBLE
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}