package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.databinding.FragmentPage2Binding

class Page2Fragment : Fragment() {
    private lateinit var binding: FragmentPage2Binding
    private lateinit var parte: ParteItem
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
        binding.btnNewVehiculoA.setOnClickListener {
            startActivity(Intent(requireContext(), NewVehiculoParteActivity::class.java))
        }
    }
}