package com.zcode.crashcar.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zcode.crashcar.R
import com.zcode.crashcar.databinding.ActivityHomeBinding
import com.zcode.crashcar.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActions()
    }

    private fun initActions() {
        val navController = findNavController(R.id.main_fragment_nav)
        binding.bottomNavigation.setupWithNavController(navController)
    }
}