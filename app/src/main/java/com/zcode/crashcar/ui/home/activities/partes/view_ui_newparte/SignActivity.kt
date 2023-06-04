package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.zcode.crashcar.R
import com.zcode.crashcar.databinding.ActivitySignBinding

class SignActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val turno = intent.extras?.getString("turno", "").toString()

        binding.lblSightActivity.text = String.format(getString(R.string.firmar), turno)

        binding.btnBack.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        binding.btnFirmar.setOnClickListener {
            if (!binding.signaturePad.isEmpty) {
                val signatureBitmap: Bitmap? = binding.signaturePad.signatureBitmap
                val intent = Intent()
                intent.putExtra("firma", Gson().toJson(signatureBitmap))
                setResult(Activity.RESULT_OK, intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }
}