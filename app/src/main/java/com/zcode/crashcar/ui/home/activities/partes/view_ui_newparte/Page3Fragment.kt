package com.zcode.crashcar.ui.home.activities.partes.view_ui_newparte

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.zcode.crashcar.R
import com.zcode.crashcar.adapter.AdapterImagesParte
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.databinding.FragmentPage3Binding
import com.zcode.crashcar.utils.dialogs.DialogAlert
import java.io.IOException


class Page3Fragment : Fragment() {
    private lateinit var binding: FragmentPage3Binding
    private lateinit var parte: ParteItem
    private lateinit var vehiculoA: VehiculoParte
    private lateinit var vehiculoB: VehiculoParte
    private lateinit var listImages: ArrayList<Bitmap>
    private lateinit var adapterImages: AdapterImagesParte

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPage3Binding.inflate(inflater, container, false)

        parte = Gson().fromJson(
            (arguments?.getString("parteItem", "").toString()),
            ParteItem::class.java
        )
        vehiculoA = Gson().fromJson(
            (arguments?.getString("vehiculoA", "").toString()),
            VehiculoParte::class.java
        )
        vehiculoB = Gson().fromJson(
            (arguments?.getString("vehiculoB", "").toString()),
            VehiculoParte::class.java
        )

        initComponent()
        return binding.root
    }

    private fun initComponent() {
        binding.btnAddImages.setOnClickListener {
            checkPermissions()
        }
        binding.btnNextPage.setOnClickListener {

            parte.imagenes = Gson().toJson(listImages)

            view?.findNavController()
                ?.navigate(
                    R.id.go_to_borrador,
                    bundleOf(
                        "parteItem" to (Gson().toJson(parte)),
                        "vehiculoA" to (Gson().toJson(vehiculoA)),
                        "vehiculoB" to (Gson().toJson(vehiculoB))
                    )
                )

        }
        listImages = ArrayList()
        adapterImages = AdapterImagesParte(listImages, true)
        binding.listImages.layoutManager = LinearLayoutManager(requireContext())
        binding.listImages.adapter = adapterImages
    }

    private fun showOptionsImages() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_option_images, null, false)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)

        val btnGallery: TextView = dialogView.findViewById(R.id.btnGallery)
        val btnCamera: TextView = dialogView.findViewById(R.id.btnCamera)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        alertDialog.setCancelable(false)

        btnGallery.setOnClickListener {
            if (listImages.size < 6) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                newResultGalleryImages.launch(intent)
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Solo se pueden subir un máximo de 6 imágenes",
                    R.raw.ic_caution
                )
            }
            alertDialog.dismiss()
        }

        btnCamera.setOnClickListener {
            if (listImages.size < 6) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                newResultCameraImage.launch(takePictureIntent)
            } else {
                DialogAlert.showDialogAlert(
                    requireContext(),
                    "Solo se pueden subir un máximo 6 imágenes",
                    R.raw.ic_caution
                )
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = mutableListOf<String>()
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            showOptionsImages()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                requireActivity().runOnUiThread {
                    showOptionsImages()
                }
            } else {
                requireActivity().runOnUiThread {
                    DialogAlert.showDialogAlert(
                        requireContext(),
                        "Debe de aceptar los permisos de camara y almacenamiento",
                        R.raw.ic_caution
                    )
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private val newResultCameraImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val imageBitmap = it.data?.extras?.get("data") as Bitmap
                listImages.add(imageBitmap)
                adapterImages.notifyItemInserted(listImages.size - 1)
            } else {
                Log.i("Response", "No se ha obtenido ninguna imágen")
            }
        }

    private val newResultGalleryImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.clipData != null) {
                    val clipData = it.data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            getBitmapFromUri(uri)?.let { it1 -> listImages.add(it1) }
                            adapterImages.notifyItemInserted(listImages.size - 1)
                            if (listImages.size == 6) break
                        }
                    }
                }
            } else {
                Log.i("Response", "No se ha obtenido ninguna imágen")
            }
        }
}