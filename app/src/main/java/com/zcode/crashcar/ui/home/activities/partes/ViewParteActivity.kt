package com.zcode.crashcar.ui.home.activities.partes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.Asegurado
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.api.controller.TestigoItem
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.databinding.ActivityViewParteBinding
import com.zcode.crashcar.utils.Animations
import com.zcode.crashcar.utils.RetrofitObject
import com.zcode.crashcar.utils.objects.Circunstancias
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ViewParteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewParteBinding

    private lateinit var parte: ParteItem
    private var vehiculoParA: VehiculoParte? = null
    private var vehiculoParB: VehiculoParte? = null
    private var vehiculoSeguroA: VehiculoSeguro? = null
    private var vehiculoSeguroB: VehiculoSeguro? = null
    private var seguroA: SegurosItem? = null
    private var seguroB: SegurosItem? = null
    private var vehiculoA: VehiculoItem? = null
    private var vehiculoB: VehiculoItem? = null
    private var aseguradoA: Asegurado? = null
    private var aseguradoB: Asegurado? = null
    private var conductorA: ConductorItem? = null
    private var conductorB: ConductorItem? = null
    private var testigos: ArrayList<TestigoItem>? = null
    private var imagenes: List<String>? = null
    private var testigosCargados = false
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewParteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state(1)
        val idParte = intent.extras?.getString("parte", "").toString()
        loadParte(idParte)
    }

    private fun loadParte(idParte: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getParte(idParte.toInt())
                if (response.isSuccessful) {
                    runOnUiThread {
                        parte = response.body()!!
                        startServiceParte()
                        initComponent()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showDialogAlertConnection(
                        "No es posible conectar con los servicios de CrashCar en este momento, intentelo de nuevo más tarde",
                        R.raw.ic_connection_error
                    )
                }
            }
        }
    }

    private fun showDialogAlertConnection(msg: String, idLottie: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null, false)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
        val textMsg: TextView = dialogView.findViewById(R.id.textMsgAlertDialog)
        val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedAlertDialog)

        textMsg.text = msg
        imgLottie.setAnimation(idLottie)
        imgLottie.loop(true)
        imgLottie.playAnimation()

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
        btnOk.setOnClickListener {
            finish()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun startServiceParte() {
        Thread {
            kotlin.run {
                while (true) {
                    if (cumpleCondiciones() && testigosCargados) break
                    Thread.sleep(1000)
                }
                runOnUiThread {
                    loadPdfParte()
                }
            }
        }.start()
    }

    private fun cumpleCondiciones(): Boolean {
        return vehiculoParA != null &&
                vehiculoParB != null &&
                vehiculoA != null &&
                vehiculoB != null &&
                vehiculoSeguroA != null &&
                vehiculoSeguroB != null &&
                conductorA != null &&
                conductorB != null &&
                imagenes != null &&
                testigos != null &&
                aseguradoA != null &&
                aseguradoB != null &&
                seguroA != null &&
                seguroB != null

    }

    private fun initComponent() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnShare.setOnClickListener { openFolder() }
        loadDataParte()
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(this, this.packageName + ".provider", file)

        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            this.startActivity(Intent.createChooser(intent, "Compartir Parte Crash-Car"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun loadDataParte() {
        loadVehiclePartA(
            (Gson().fromJson<ArrayList<Int>>(
                parte.vehiculosParte,
                (object : TypeToken<ArrayList<Int>>() {}.type)
            ))[0]
        )
        loadVehiclePartB(
            (Gson().fromJson<ArrayList<Int>>(
                parte.vehiculosParte,
                (object : TypeToken<ArrayList<Int>>() {}.type)
            ))[1]
        )
        loadTestigos()
        imagenes = Gson().fromJson<ArrayList<String>>(
            parte.imagenes,
            (object : TypeToken<ArrayList<String>>() {}.type)
        )
    }

    private fun loadTestigos() {
        val lista = ArrayList<TestigoItem>()

        val listaTestigos = (Gson().fromJson<ArrayList<Int>>(
            parte.testigo,
            (object : TypeToken<ArrayList<Int>>() {}.type)
        ))

        for (idTestigo in listaTestigos) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitObject.getCallRetrofit().getTestigo(idTestigo)
                    if (response.isSuccessful) {
                        response.body()?.let { lista.add(it) }
                    }
                } catch (e: Exception) {
                    //
                }
            }
        }
        Thread {
            kotlin.run {
                while (true) {
                    if (listaTestigos.size == lista.size) {
                        this.testigos = lista
                        testigosCargados = true
                        break
                    }
                    Thread.sleep(1000)
                }
            }
        }.start()
    }

    private fun loadVehiclePartB(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getVehiculoParte(id)
                if (response.isSuccessful) {
                    vehiculoParB = response.body()
                    loadVehicleBSeguro(vehiculoParB?.idVehiculo)
                    loadConductorB(vehiculoParB?.idConductor)
                    loadAseguradoB(vehiculoParB?.idAsegurado)
                    loadSeguroB(vehiculoParB?.idSeguro)
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadSeguroB(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getSeguro(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        seguroB = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadAseguradoB(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getAsegurado(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        aseguradoB = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadConductorB(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getConductor(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        conductorB = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadVehicleBSeguro(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getVehiculoSeguro(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        vehiculoSeguroB = response.body()
                        loadVehicleB(vehiculoSeguroB?.idVehiculo)
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadVehicleB(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getVehiculo(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        vehiculoB = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadVehiclePartA(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getVehiculoParte(id)
                if (response.isSuccessful) {
                    vehiculoParA = response.body()
                    loadVehicleASeguro(vehiculoParA?.idVehiculo)
                    loadConductorA(vehiculoParA?.idConductor)
                    loadAseguradoA(vehiculoParA?.idAsegurado)
                    loadSeguroA(vehiculoParA?.idSeguro)
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadSeguroA(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getSeguro(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        seguroA = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadAseguradoA(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getAsegurado(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        aseguradoA = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadConductorA(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getConductor(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        conductorA = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadVehicleASeguro(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getVehiculoSeguro(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        vehiculoSeguroA = response.body()
                        loadVehicleA(vehiculoSeguroA?.idVehiculo)
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadVehicleA(id: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = id?.let { RetrofitObject.getCallRetrofit().getVehiculo(it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        vehiculoA = response.body()
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun state(i: Int) {
        when (i) {
            1 -> {
                binding.pdfView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }

            0 -> {
                binding.pdfView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadPdfParte() {
        val pdfReader = PdfReader(assets.open("parte_crash_car_form.pdf"))
        val pdfFile = File(
            getExternalFilesDir(null), "parte_crash-car_${parte.idParte}.pdf"
        )
        val pdfWriter = PdfWriter(pdfFile.absolutePath)
        val pdfDocument = PdfDocument(pdfReader, pdfWriter)
        var page = pdfDocument.getPage(3)
        var canvas = PdfCanvas(page)
        val acroForm = PdfAcroForm.getAcroForm(pdfDocument, true)

        /* Rellenamos el PDF valor a valor */
        acroForm.getField("idParte").setValue(parte.idParte.toString())
        acroForm.getField("textFecha").setValue(parte.fechAccidente)
        acroForm.getField("textHora").setValue(parte.horaAccidente)
        acroForm.getField("textDireccion").setValue(parte.direccion)
        acroForm.getField("textPais").setValue(parte.paisAccidente)
        if (parte.visctimas) acroForm.getField("checkVictimas").setValue("On")
        if (parte.isOtherVehicles) acroForm.getField("checkOtherVehicles").setValue("On")
        if (parte.damageMaterial) acroForm.getField("checkDmageeMaterial").setValue("On")
        if (testigos?.isEmpty() == true) {
            acroForm.getField("testigos").setValue("No hay testigos registrados")
        } else {
            acroForm.getField("testigos").setValue(loadStringTestigos())
        }
        acroForm.getField("nombreAseguradoA").setValue(aseguradoA?.nombre)
        acroForm.getField("apellidosAseguradoA").setValue(aseguradoA?.apellidos)
        acroForm.getField("direccionAseguradoA").setValue(aseguradoA?.direccion)
        acroForm.getField("cpAseguradoA").setValue(aseguradoA?.codPostas)
        acroForm.getField("paisAseguradoA").setValue(aseguradoA?.pais)
        acroForm.getField("phoneAseguradoA").setValue(aseguradoA?.telefono)
        acroForm.getField("emailAseguradoA").setValue(aseguradoA?.email)
        acroForm.getField("marcaA").setValue(vehiculoA?.marca)
        acroForm.getField("modeloA").setValue(vehiculoA?.modelo)
        acroForm.getField("paisMatA").setValue(vehiculoA?.paisMatricula)
        acroForm.getField("typeA").setValue(vehiculoA?.tipoVehiculo)
        acroForm.getField("matA").setValue(vehiculoA?.matricula)
        if (vehiculoParA?.remolque == true) {
            acroForm.getField("matRA").setValue(vehiculoParA?.matriculaRemolque)
            acroForm.getField("paisMatRA").setValue(vehiculoParA?.paisMatriculaRemolque)
        } else {
            acroForm.getField("matRA").setValue("No lleva remolque")
            acroForm.getField("paisMatRA").setValue("No lleva remolque")
        }

        acroForm.getField("nombreSegA").setValue(seguroA?.nombreAgencia)
        acroForm.getField("numeroPolA").setValue(vehiculoSeguroA?.numeroPoliza)
        if (vehiculoSeguroA?.numeroCartaVerde?.isEmpty() == true) {
            acroForm.getField("numCVA").setValue("No tiene Carta Verde")
            acroForm.getField("fechaIniCVA").setValue("No tiene Carta Verde")
            acroForm.getField("fechaVencCVA").setValue("No tiene Carta Verde")
        } else {
            acroForm.getField("numCVA").setValue(vehiculoSeguroA?.numeroCartaVerde)
            acroForm.getField("fechaIniCVA").setValue(vehiculoSeguroA?.fechaCartaVerdeInicio)
            acroForm.getField("fechaVencCVA").setValue(vehiculoSeguroA?.fechaCartaVerdeFin)
        }
        acroForm.getField("nombreAgA").setValue(seguroA?.nombreAgencia)
        acroForm.getField("direcSegA").setValue(seguroA?.direccionAgencia)
        acroForm.getField("paisSegA").setValue(seguroA?.paisAgencia)
        acroForm.getField("phoneSegA").setValue(seguroA?.phoneAgencia)
        acroForm.getField("emailSegA").setValue(seguroA?.emailAgencia)
        acroForm.getField("dniConA").setValue(conductorA?.dniConductor)
        acroForm.getField("nombreConA").setValue(conductorA?.nombre)
        acroForm.getField("apellidosConA").setValue(conductorA?.apellidos)
        acroForm.getField("direccionConA").setValue(conductorA?.direccion)
        acroForm.getField("codPostConA").setValue(conductorA?.codpostal.toString())
        acroForm.getField("paisConA").setValue(conductorA?.pais)
        acroForm.getField("phoneConA").setValue(conductorA?.phone)
        acroForm.getField("emailConA").setValue(conductorA?.email)
        acroForm.getField("puntoChoqueA").setValue(vehiculoParA?.puntoChoque)
        if (vehiculoParA?.observaciones?.isEmpty() == true) {
            acroForm.getField("observacionesA").setValue("No hay observaciones")
        } else {
            acroForm.getField("observacionesA").setValue(vehiculoParA?.observaciones)
        }
        val circunstanciasA =
            Gson().fromJson(vehiculoParA?.circunstancias, Circunstancias::class.java)
        for (circunstancia in circunstanciasA) {
            if (circunstancia.check) {
                acroForm.getField("c${circunstancia.id}a").setValue("On")
            }
        }

        acroForm.getField("nombreAseguradoB").setValue(aseguradoB?.nombre)
        acroForm.getField("apellidosAseguradoB").setValue(aseguradoB?.apellidos)
        acroForm.getField("direccionAseguradoB").setValue(aseguradoB?.direccion)
        acroForm.getField("cpAseguradoB").setValue(aseguradoB?.codPostas)
        acroForm.getField("paisAseguradoB").setValue(aseguradoB?.pais)
        acroForm.getField("phoneAseguradoB").setValue(aseguradoB?.telefono)
        acroForm.getField("emailAseguradoB").setValue(aseguradoB?.email)
        acroForm.getField("marcaB").setValue(vehiculoB?.marca)
        acroForm.getField("modeloB").setValue(vehiculoB?.modelo)
        acroForm.getField("paisMatB").setValue(vehiculoB?.paisMatricula)
        acroForm.getField("typeB").setValue(vehiculoB?.tipoVehiculo)
        acroForm.getField("matB").setValue(vehiculoB?.matricula)
        if (vehiculoParB?.remolque == true) {
            acroForm.getField("matRB").setValue(vehiculoParB?.matriculaRemolque)
            acroForm.getField("paisMatRB").setValue(vehiculoParB?.paisMatriculaRemolque)
        } else {
            acroForm.getField("matRB").setValue("No lleva remolque")
            acroForm.getField("paisMatRB").setValue("No lleva remolque")
        }

        acroForm.getField("nombreSegB").setValue(seguroB?.nombreAgencia)
        acroForm.getField("numeroPolB").setValue(vehiculoSeguroB?.numeroPoliza)
        if (vehiculoSeguroB?.numeroCartaVerde?.isEmpty() == true) {
            acroForm.getField("numCVB").setValue("No tiene Carta Verde")
            acroForm.getField("fechaIniCVB").setValue("No tiene Carta Verde")
            acroForm.getField("fechaVencCVB").setValue("No tiene Carta Verde")
        } else {
            acroForm.getField("numCVB").setValue(vehiculoSeguroB?.numeroCartaVerde)
            acroForm.getField("fechaIniCVB").setValue(vehiculoSeguroB?.fechaCartaVerdeInicio)
            acroForm.getField("fechaVencCVB").setValue(vehiculoSeguroB?.fechaCartaVerdeFin)
        }
        acroForm.getField("nombreAgB").setValue(seguroB?.nombreAgencia)
        acroForm.getField("direcSegB").setValue(seguroB?.direccionAgencia)
        acroForm.getField("paisSegB").setValue(seguroB?.paisAgencia)
        acroForm.getField("phoneSegB").setValue(seguroB?.phoneAgencia)
        acroForm.getField("emailSegB").setValue(seguroB?.emailAgencia)
        acroForm.getField("dniConB").setValue(conductorB?.dniConductor)
        acroForm.getField("nombreConB").setValue(conductorB?.nombre)
        acroForm.getField("apellidosConB").setValue(conductorB?.apellidos)
        acroForm.getField("direccionConB").setValue(conductorB?.direccion)
        acroForm.getField("codPostConB").setValue(conductorB?.codpostal.toString())
        acroForm.getField("paisConB").setValue(conductorB?.pais)
        acroForm.getField("phoneConB").setValue(conductorB?.phone)
        acroForm.getField("emailConB").setValue(conductorB?.email)
        acroForm.getField("puntoChoqueB").setValue(vehiculoParB?.puntoChoque)
        if (vehiculoParB?.observaciones?.isEmpty() == true) {
            acroForm.getField("observacionesB").setValue("No hay observaciones")
        } else {
            acroForm.getField("observacionesB").setValue(vehiculoParB?.observaciones)
        }
        val circunstanciasB =
            Gson().fromJson(vehiculoParB?.circunstancias, Circunstancias::class.java)
        for (circunstancia in circunstanciasB) {
            if (circunstancia.check) {
                acroForm.getField("c${circunstancia.id}b").setValue("On")
            }
        }

        val firmaEncodeA = vehiculoParA?.firma
        var imageData = ImageDataFactory.create(firmaEncodeA?.let { base64 ->
            base64ToBitmap(base64)?.let {
                bitmapToByteArray(
                    it
                )
            }
        })
        canvas.addImage(imageData, 100f, 130f, 100f, true)

        val firmaEncodeB = vehiculoParB?.firma
        page = pdfDocument.getPage(5)
        canvas = PdfCanvas(page)
        imageData = ImageDataFactory.create(firmaEncodeB?.let { base64 ->
            base64ToBitmap(base64)?.let {
                bitmapToByteArray(
                    it
                )
            }
        })
        canvas.addImage(imageData, 100f, 130f, 100f, true)


        for (image in imagenes!!) {
            page = pdfDocument.addNewPage()
            val pageSize = page.pageSize
            canvas = PdfCanvas(page)
            imageData = ImageDataFactory.create(image.let { base64 ->
                base64ToBitmap(base64)?.let {
                    bitmapToByteArray(
                        it
                    )
                }
            })

            canvas.addImage(imageData, 50f, 50f, pageSize.width - 100, true)
        }
        /* Ya hemos rellenado el parte */
        acroForm.flattenFields()
        pdfDocument.close()

        binding.pdfView.fromFile(pdfFile).load()
        state(0)
        binding.btnShare.animation = Animations.fadeInAnimation(this)
        binding.btnShare.visibility = View.VISIBLE
        file = pdfFile
    }

    private fun loadStringTestigos(): String {
        val stringBuilder = StringBuilder()
        for (testigo in testigos!!) {
            stringBuilder.append(
                "Nombre: ${testigo.nombre}, " +
                        "Dirección: ${testigo.direccion}, " +
                        "Teléfono: ${testigo.telefono}\n\n"
            )
        }
        return stringBuilder.toString()
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        if (base64.isNotEmpty()) {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
        return null
    }

    private fun bitmapToByteArray(bitmap: Bitmap?): ByteArray {
        val stream = com.itextpdf.io.source.ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        return stream.toByteArray()
    }
}