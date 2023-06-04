package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemTestigo
import com.zcode.crashcar.api.controller.ListTestigos
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.TestigoId
import com.zcode.crashcar.api.controller.TestigoItem
import com.zcode.crashcar.api.controller.TestigosList
import com.zcode.crashcar.databinding.FragmentPage1Binding
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.dialogs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Page1Fragment : Fragment(), AdapterItemTestigo.OnItemClickListener {
    private lateinit var binding: FragmentPage1Binding
    private lateinit var listTestigos: TestigosList
    private lateinit var adapterTestigos: AdapterItemTestigo
    private lateinit var parteItem: ParteItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPage1Binding.inflate(inflater, container, false)
        listTestigos = TestigosList()
        parteItem = ParteItem()
        initComponent()
        return binding.root
    }

    private fun initComponent() {
        binding.recyclerTestigos.layoutManager = LinearLayoutManager(requireContext())
        adapterTestigos = AdapterItemTestigo(listTestigos, this, true)
        binding.recyclerTestigos.adapter = adapterTestigos
        binding.btnNewTestigo.setOnClickListener {
            if (listTestigos.size < 3) {
                showDialogTestigo(null, null)
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Solo puedes registrar un máximo de 3 testigos",
                    R.raw.ic_caution
                )
            }
        }
        binding.btnNextPage.setOnClickListener {
            if (comprobar()) {
                parteItem.fechAccidente = binding.textFechaAccidente.text.toString()
                parteItem.direccion = binding.textLocationAccidente.text.toString()
                parteItem.horaAccidente = binding.textHoraAccidente.text.toString()
                parteItem.paisAccidente = binding.textPaisAccidente.text.toString()
                parteItem.visctimas = binding.checkVictimas.isChecked
                parteItem.damageMaterial = binding.checkDamageObjetos.isChecked
                parteItem.isOtherVehicles = binding.checkOtherVehicles.isChecked
                parteItem.testigo = Gson().toJson(listTestigos)
                view?.findNavController()
                    ?.navigate(
                        R.id.go_to_page2,
                        bundleOf("parteItem" to (Gson().toJson(parteItem)))
                    )
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Debes de rellenar los campos obligatorios marcados con ' * '",
                    R.raw.ic_caution
                )
            }
        }
    }



    private fun comprobar(): Boolean {
        return binding.textFechaAccidente.text.isNotEmpty() &&
                binding.textHoraAccidente.text.isNotEmpty() &&
                binding.textLocationAccidente.text.isNotEmpty() &&
                binding.textPaisAccidente.text.isNotEmpty()
    }

    override fun onItemClick(item: TestigoItem, position: Int) {
        showDialogTestigo(item, position)
    }

    override fun deleteItem(position: Int) {
        listTestigos.removeAt(position).apply {
            adapterTestigos.notifyItemRemoved(position)
        }
    }

    private fun showDialogTestigo(item: TestigoItem?, position: Int?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_testigo, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnSaveTestigo)
        val name: EditText = dialogView.findViewById(R.id.txtNombreTestigo)
        val address: EditText = dialogView.findViewById(R.id.txtDireccionTestigo)
        val phone: EditText = dialogView.findViewById(R.id.txtPhoneTestigo)

        if (item != null) {
            name.setText(item.nombre)
            address.setText(item.direccion)
            phone.setText(item.telefono)
        }

        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        btnOk.setOnClickListener {
            if (name.text.isNotEmpty() && address.text.isNotEmpty() && phone.text.isNotEmpty()) {
                if (item != null && position != null) {
                    listTestigos[position].nombre = name.text.toString()
                    listTestigos[position].direccion = address.text.toString()
                    listTestigos[position].telefono = phone.text.toString()
                    updateTesigo(listTestigos[position], position)
                } else {
                    val testigoItem = TestigoItem(
                        name.text.toString(),
                        address.text.toString(),
                        phone.text.toString()
                    )
                    listTestigos.add(testigoItem)
                    adapterTestigos.notifyItemInserted(listTestigos.size - 1)
                }
                alertDialog.dismiss()
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Debes de rellenar todos los campos",
                    R.raw.ic_caution
                )
            }
        }
        alertDialog.show()
    }

    private fun updateTesigo(testigoItem: TestigoItem, position: Int) {
        listTestigos[position] = testigoItem
        adapterTestigos.notifyItemChanged(position)
    }

    private fun saveTestigo(testigoItem: TestigoItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().saveTestigo(testigoItem)
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        response.body()?.let {
                            listTestigos.add(it)
                            adapterTestigos.notifyItemInserted(listTestigos.size - 1)
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
}