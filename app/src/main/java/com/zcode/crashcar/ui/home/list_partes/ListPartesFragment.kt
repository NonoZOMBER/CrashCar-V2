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
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ListPartes
import com.zcode.crashcar.databinding.FragmentListPartesBinding
import com.zcode.crashcar.ui.home.activities.partes.NewParteActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListPartesFragment : Fragment() {
    private lateinit var binding: FragmentListPartesBinding
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListPartes(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.posgresBar.visibility = View.GONE
                        createItems(response.body() ?: ListPartes())
                    }
                } else {
                    Log.e("ResponsePartes", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ErrorConection", "Error: ${e.message}")
            }

        }
    }

    private fun createItems(listPartes: ListPartes) {
        if (listPartes.isNotEmpty()) {
            binding.lblNotItem.isVisible = false
            binding.recyclerPartes.layoutManager = LinearLayoutManager(requireContext())
        } else {
            binding.lblNotItem.isVisible = true
        }
    }

    override fun onResume() {
        super.onResume()
        loadPartes()
    }
}