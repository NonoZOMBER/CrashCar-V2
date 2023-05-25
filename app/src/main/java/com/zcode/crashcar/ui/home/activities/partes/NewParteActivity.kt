package com.zcode.crashcar.ui.home.activities.partes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zcode.crashcar.R
import com.zcode.crashcar.databinding.ActivityNewParteBinding

class NewParteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewParteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewParteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponent()
    }

    private fun initComponent() {
        binding.btnBack.setOnClickListener { finish() }
    }
}