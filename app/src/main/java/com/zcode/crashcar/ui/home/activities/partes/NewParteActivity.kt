package com.zcode.crashcar.ui.home.activities.partes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
        binding.btnReloadNewParte.setOnClickListener { showResetDialog() }
    }

    private fun showResetDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verify_reload, null, false)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btn_ok_reload)
        val btnCancel: FrameLayout = dialogView.findViewById(R.id.btn_cancel_reload)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        alertDialog.setCancelable(false)

        btnOk.setOnClickListener {
            val intent = Intent(this, NewParteActivity::class.java)
            startActivity(intent)
            finish()
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}