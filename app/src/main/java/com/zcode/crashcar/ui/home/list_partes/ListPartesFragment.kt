package com.zcode.crashcar.ui.home.list_partes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterItemParte
import com.zcode.crashcar.api.controller.ListPartes
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.databinding.FragmentListPartesBinding
import com.zcode.crashcar.ui.home.activities.partes.NewParteActivity
import com.zcode.crashcar.ui.home.activities.partes.ViewParteActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ListPartesFragment : Fragment(), AdapterItemParte.OnItemSelect, AdapterItemParte.OnSizeChange {
    private lateinit var binding: FragmentListPartesBinding
    private lateinit var listPartes: ListPartes
    private lateinit var adapter: AdapterItemParte
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListPartesBinding.inflate(inflater, container, false)
        loadPartes()
        initComponent()
        return binding.root
    }

    private fun initComponent() {
        binding.btnAddPartes.setOnClickListener {
            startActivity(Intent(requireContext(), NewParteActivity::class.java))
        }
    }

    private fun loadPartes() {
        Log.i("UserId", prefsSetting.getIdUser())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListPartes(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.posgresBar.visibility = View.GONE
                        this@ListPartesFragment.listPartes = response.body() ?: ListPartes()
                        createItems()
                    }
                } else {
                    Log.e("ResponsePartes", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ErrorConection", "Error: ${e.message}")
            }

        }
    }

    private fun createItems() {
        if (listPartes.isNotEmpty()) {
            binding.recyclerPartes.visibility = View.VISIBLE
            binding.lblNotItem.isVisible = false
            binding.recyclerPartes.layoutManager = LinearLayoutManager(requireContext())
            adapter = AdapterItemParte(listPartes, this, this)
            binding.recyclerPartes.adapter = adapter
        } else {
            binding.lblNotItem.isVisible = true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerPartes.visibility = View.GONE
        binding.posgresBar.visibility = View.VISIBLE
        loadPartes()
    }

    override fun onItemSelect(item: ParteItem) {
        val intent = Intent(requireContext(), ViewParteActivity::class.java)
        intent.putExtra("parte", item.idParte.toString())
        startActivity(intent)
    }

    override fun onChangeSize(size: Int) {
        if (size == 0) {
            binding.lblNotItem.isVisible = true
        }
    }
}