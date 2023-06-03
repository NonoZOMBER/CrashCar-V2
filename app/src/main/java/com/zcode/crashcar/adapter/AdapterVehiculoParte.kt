package com.zcode.crashcar.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 *    Created by Nono on 03/06/2023.
 */
class AdapterVehiculoParte(private val listVehiculo: ArrayList<VehiculoParte>) :
    RecyclerView.Adapter<AdapterVehiculoParte.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nombreVehiculo: TextView
        private val matriculaVehiculo: TextView
        private val nPolizaVehiculo: TextView
        private val imgType: ImageView

        init {
            nombreVehiculo = view.findViewById(R.id.textNombreVehiculoParteItem)
            matriculaVehiculo = view.findViewById(R.id.textMatriculaItemParte)
            nPolizaVehiculo = view.findViewById(R.id.textNpolizaItemParte)
            imgType = view.findViewById(R.id.img_type_vehicle_parte)
        }

        fun loadVehicleSecure(item: VehiculoParte) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = item.idVehiculo?.let {
                    RetrofitObject.getCallRetrofit().getVehiculoSeguro(
                        it
                    )
                }
                if (response != null) {
                    if (response.isSuccessful) {
                        bindDataVehicle(response.body())
                    } else {
                        Log.e("ResponseVehicle", response.body().toString())
                    }
                }
            }
        }

        private fun bindDataVehicle(body: VehiculoSeguro?) {
            if (body != null) {
                nPolizaVehiculo.text = body.numeroPoliza
                loadVehicle(body.idVehiculo)
            }
        }

        private fun loadVehicle(idVehiculo: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().getVehiculo(idVehiculo)
                if (response.isSuccessful) {
                    matriculaVehiculo.text = response.body()?.matricula
                    nombreVehiculo.text = String.format(itemView.context.getString(R.string.lblNombreApellidos), response.body()?.marca, response.body()?.modelo)
                    imgType.post {
                        response.body()?.tipoVehiculo?.let {
                            Herramientas.getIconVehicle(
                                it.lowercase())
                        }?.let { imgType.setImageResource(it) }
                    }
                } else {
                    Log.e("ResponseVehicle", response.body().toString())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_vehiculo_parte, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listVehiculo.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.loadVehicleSecure(listVehiculo[position])
    }
}