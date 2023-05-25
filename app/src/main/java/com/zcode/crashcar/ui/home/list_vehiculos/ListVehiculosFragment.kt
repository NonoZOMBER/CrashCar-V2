package com.zcode.crashcar.ui.home.list_vehiculos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemVehiculoUsuario
import com.zcode.crashcar.api.controller.VehiculosUsuario
import com.zcode.crashcar.databinding.FragmentListVehiculosBinding
import com.zcode.crashcar.ui.home.activities.vehiculos.VehiculoUsuarioActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListVehiculosFragment : Fragment(), AdapterItemVehiculoUsuario.OnSizeChangeListener {
    private lateinit var binding: FragmentListVehiculosBinding
    private lateinit var listVehiculos: VehiculosUsuario

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListVehiculosBinding.inflate(inflater, container, false)
        loadVehiculos()
        initComponent()
        return binding.root
    }

    private fun loadVehiculos() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = RetrofitObject.getCallRetrofit().getListVehiculos(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.posgresBar.visibility = View.INVISIBLE
                        createItems(response.body() ?: VehiculosUsuario())
                    }
                } else {
                    Log.e("ErrorSegurosResponse", response.message().toString())
                }
            } catch (e: Exception) {
                Log.e("ConnectionError", e.message.toString())
            }
        }
    }

    private fun createItems(vehiculosUsuario: VehiculosUsuario) {
        if (vehiculosUsuario.isNotEmpty()) {
            binding.lblNotItem.visibility = View.INVISIBLE
            binding.recyclerVehiculos.layoutManager = LinearLayoutManager(requireContext())
            listVehiculos = vehiculosUsuario
            val adapter = AdapterItemVehiculoUsuario(listVehiculos, this,
                delete = true,
                update = true,
                null
            )
            binding.recyclerVehiculos.adapter = adapter
        } else {
            binding.lblNotItem.visibility = View.VISIBLE
        }
    }

    private fun initComponent() {
        binding.btnAddVehiculos.setOnClickListener {
            startActivity(Intent(requireContext(), VehiculoUsuarioActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadVehiculos()
    }

    override fun onChange(sizeList: Int) {
        if (sizeList == 0) {
            binding.lblNotItem.visibility = View.VISIBLE
        } else {
            binding.lblNotItem.visibility = View.INVISIBLE
        }
    }
}