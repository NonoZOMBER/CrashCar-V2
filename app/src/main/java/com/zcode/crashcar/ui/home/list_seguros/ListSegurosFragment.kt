package com.zcode.crashcar.ui.home.list_seguros

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
import com.zcode.crashcar.adapter.AdapterItemSeguros
import com.zcode.crashcar.api.controller.ListSeguros
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.databinding.FragmentListSegurosBinding
import com.zcode.crashcar.ui.home.activities.seguros.SeguroActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListSegurosFragment : Fragment(), AdapterItemSeguros.OnSizeChangeListener {
    private lateinit var binding: FragmentListSegurosBinding
    private lateinit var adapter: AdapterItemSeguros
    private lateinit var listSeguros: ListSeguros

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListSegurosBinding.inflate(inflater, container, false)
        loadSeguros()
        initComponent()
        return binding.root
    }

    private fun initComponent() {
        binding.btnAddSeguros.setOnClickListener {
            requireContext().startActivity(
                Intent(
                    requireContext(), SeguroActivity
                    ::class.java
                )
            )
        }
    }

    private fun loadSeguros() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListSeguros(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.posgresBar.visibility = View.INVISIBLE
                        createItems(response.body() ?: ListSeguros())
                    }
                } else {
                    Log.e("ErrorSegurosResponse", response.message().toString())
                }
            } catch (e: Exception) {
                Log.e("ConnectionError", e.message.toString())
            }
        }
    }

    private fun createItems(listResponse: ListSeguros) {
        if (listResponse.isNotEmpty()) {
            binding.lblNotItem.visibility = View.INVISIBLE
            binding.recyclerSeguros.layoutManager = LinearLayoutManager(requireContext())
            listSeguros = listResponse
            val adapter = getAdapter()
            binding.recyclerSeguros.adapter = adapter
        } else {
            binding.lblNotItem.visibility = View.VISIBLE
        }
    }

    private fun getAdapter(): AdapterItemSeguros {
        adapter = AdapterItemSeguros(listSeguros, this, false, click = true)
        return adapter
    }

    override fun onResume() {
        super.onResume()
        loadSeguros()
    }

    override fun onChange(sizeList: Int) {
        if (sizeList == 0) {
            binding.lblNotItem.visibility = View.VISIBLE
        } else {
            binding.lblNotItem.visibility = View.INVISIBLE
        }
    }

    override fun onItem(item: SegurosItem) {}
}