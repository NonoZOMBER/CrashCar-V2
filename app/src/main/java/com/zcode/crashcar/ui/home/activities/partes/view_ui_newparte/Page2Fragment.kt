package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterVehiculoParte
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.databinding.FragmentPage2Binding
import com.zcode.crashcar.utils.dialogs.DialogAlert

class Page2Fragment : Fragment() {
    private lateinit var binding: FragmentPage2Binding
    private lateinit var parte: ParteItem
    private lateinit var listVehiculoA: ArrayList<VehiculoParte>
    private lateinit var listVehiculoB: ArrayList<VehiculoParte>
    private lateinit var adapterVehiculoA: AdapterVehiculoParte
    private lateinit var adapterVehiculoB: AdapterVehiculoParte

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPage2Binding.inflate(inflater, container, false)
        initComponent()
        val parteString = arguments?.getString("parteItem", "").toString()
        parte = Gson().fromJson(parteString, ParteItem::class.java)
        return binding.root
    }

    private fun initComponent() {
        listVehiculoA = ArrayList()
        listVehiculoB = ArrayList()

        adapterVehiculoA = AdapterVehiculoParte(listVehiculoA)
        adapterVehiculoB = AdapterVehiculoParte(listVehiculoB)

        binding.listVehiculoA.layoutManager = LinearLayoutManager(requireContext())
        binding.listVehiculoB.layoutManager = LinearLayoutManager(requireContext())

        binding.listVehiculoA.adapter = adapterVehiculoA
        binding.listVehiculoB.adapter = adapterVehiculoB

        binding.btnNextPage.setOnClickListener {
            if (listVehiculoA.size > 0) {
                if (listVehiculoB.size > 0) {
                    view?.findNavController()
                        ?.navigate(
                            R.id.go_to_page3,
                            bundleOf(
                                "parteItem" to (Gson().toJson(parte)),
                                "vehiculoA" to (Gson().toJson(listVehiculoA[0])),
                                "vehiculoB" to (Gson().toJson(listVehiculoB[0]))
                            )
                        )
                } else {
                    DialogAlert.showDialogAlert(
                        requireContext(),
                        "Debe de agregar el vehículo B",
                        R.raw.ic_caution
                    )
                }
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Debe de agregar el vehículo A",
                    R.raw.ic_caution
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.btnNewVehiculoA.setOnClickListener {
            newResultVehiculoA.launch(
                Intent(
                    requireContext(),
                    NewVehiculoParteActivity::class.java
                )
            )
        }
        binding.btnNewVehiculoB.setOnClickListener {
            newResultVehiculoB.launch(
                Intent(
                    requireContext(),
                    NewVehiculoParteActivity::class.java
                )
            )
        }
    }

    private val newResultVehiculoA =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val vehiculoParte = it.data?.getStringExtra("vehiculoParte").toString()
                loadVehiculoAParte(vehiculoParte)
            } else {
                Log.i("ResponseConductor", "No se ha obtenido ningún conductor")
            }
        }

    private val newResultVehiculoB =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val vehiculoParte = it.data?.getStringExtra("vehiculoParte").toString()
                loadVehiculoBParte(vehiculoParte)
            } else {
                Log.i("ResponseConductor", "No se ha obtenido ningún conductor")
            }
        }

    private fun loadVehiculoBParte(vehiculoParte: String) {
        val vehiculoB: VehiculoParte = Gson().fromJson(vehiculoParte, VehiculoParte::class.java)
        listVehiculoB.clear()
        listVehiculoB.add(vehiculoB)
        adapterVehiculoB.notifyDataSetChanged()
    }

    private fun loadVehiculoAParte(vehiculoParte: String) {
        val vehiculoA: VehiculoParte = Gson().fromJson(vehiculoParte, VehiculoParte::class.java)
        listVehiculoA.clear()
        listVehiculoA.add(vehiculoA)
        adapterVehiculoA.notifyDataSetChanged()
    }
}