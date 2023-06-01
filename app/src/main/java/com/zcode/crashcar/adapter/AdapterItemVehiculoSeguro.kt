package com.zcode.crashcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.utils.Utils
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 *    Created by Nono on 07/05/2023.
 */
class AdapterItemVehiculoSeguro(
    private val listVehiculoSeguro: List<VehiculoSeguro>,
    private val onSizeChangeListener: OnSizeChangeListener?,
    private val onItemClickListener: OnItemClickListener,
    private val delete: Boolean
) :
    RecyclerView.Adapter<AdapterItemVehiculoSeguro.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: VehiculoSeguro, position: Int)
    }

    interface OnSizeChangeListener {
        fun onChangeListVehiculoUsuario(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nVehiculo: TextView
        private val nPoliza: TextView
        private val icon: ImageView
        private val container: CardView
        private val btnDelete: ImageButton

        init {
            nVehiculo = view.findViewById(R.id.textNombreVehiculoSeguroItem)
            nPoliza = view.findViewById(R.id.textNpolizaItem)
            icon = view.findViewById(R.id.img_type_vehicle_seguro)
            container = view.findViewById(R.id.container_card_vehiculo_seguro)
            btnDelete = view.findViewById(R.id.btnDeleteVehiculoSeguroItem)
        }

        fun getVehiculo(itemS: VehiculoSeguro) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = RetrofitObject.getCallRetrofit().getVehiculo(itemS.idVehiculo)
                if (response.isSuccessful) {
                    bindDataVehicle(response.body())
                } else {
                    Log.e("ResponseVehicle", response.body().toString())
                }
            }
        }

        private fun bindDataVehicle(itemV: VehiculoItem?) {
            if (itemV != null) {
                nVehiculo.text = String.format(
                    itemView.context.getString(R.string.lblNombreApellidos),
                    itemV.marca,
                    itemV.modelo
                )
                icon.post { icon.setImageResource(Herramientas.getIconVehicle(itemV.tipoVehiculo.lowercase())) }
            }
        }

        fun bind(item: VehiculoSeguro, position: Int) {
            nPoliza.text = String.format(itemView.context.getString(R.string.lbl_num_poliza), item.numeroPoliza)
            btnDelete.setOnClickListener { deleteVehiculoSeguro(itemView.context, position) }
            container.setOnClickListener { onItemClickListener.onItemClick(item, position) }
            if (!delete) btnDelete.visibility = View.GONE
        }
    }

    private fun deleteVehiculoSeguro(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_vehiculo_seguro))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                onSizeChangeListener?.onChangeListVehiculoUsuario(position)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehiculo_seguro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listVehiculoSeguro.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.getVehiculo(listVehiculoSeguro[position])
        holder.bind(listVehiculoSeguro[position], position)
    }
}