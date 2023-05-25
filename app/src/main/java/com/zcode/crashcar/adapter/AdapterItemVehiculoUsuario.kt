package com.zcode.crashcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.ui.home.activities.vehiculos.VehiculoUsuarioActivity
import com.zcode.crashcar.utils.Herramientas
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 *    Created by Nono on 27/04/2023.
 */
class AdapterItemVehiculoUsuario(
    private val listVehiculos: ArrayList<VehiculoItem>,
    private val onSizeChangeListener: OnSizeChangeListener?,
    private val delete: Boolean,
    private val update: Boolean,
    private val onItemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<AdapterItemVehiculoUsuario.ViewHolder>() {

    interface OnSizeChangeListener {
        fun onChange(sizeList: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(item: VehiculoItem)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textVehiculo: TextView
        private val textMatricula: TextView
        private val container: CardView
        private val btnDelete: ImageButton
        private val imgType: ImageView

        init {
            textVehiculo = view.findViewById(R.id.textNameVehiculoUsuarioItem)
            textMatricula = view.findViewById(R.id.textMatriculaVehiculoUsuarioItem)
            container = view.findViewById(R.id.container_card_vehiculo_usuario)
            btnDelete = view.findViewById(R.id.btnDeleteVehiculoUsuarioItem)
            imgType = view.findViewById(R.id.img_type_vehicle)
        }

        fun bind(item: VehiculoItem, position: Int) {
            textVehiculo.text = String.format(
                itemView.context.getString(R.string.lblNombreApellidos),
                item.marca,
                item.modelo
            )
            textMatricula.text =
                String.format(itemView.context.getString(R.string.lbl_matricula), item.matricula)
            if (update) {
                container.setOnClickListener { startViewVehiculo(item, itemView.context) }
            } else {
                container.setOnClickListener { onItemClickListener?.onItemClick(item) }
            }

            btnDelete.setOnClickListener { deleteItem(item, position, itemView.context) }
            imgType.post {
                imgType.setImageResource(Herramientas.getIconVehicle(item.tipoVehiculo.lowercase()))
            }
            if (!delete) btnDelete.isVisible = false
        }
    }

    private fun deleteItem(item: VehiculoItem, position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_vehiculo_usuario))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                deleteVehiculo(item)
                listVehiculos.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listVehiculos.size - position)
                onSizeChangeListener?.onChange(listVehiculos.size)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun deleteVehiculo(item: VehiculoItem) {
        item.activo = false
        CoroutineScope(Dispatchers.IO).launch {
            val response =
                RetrofitObject.getCallRetrofit().updateVehiculo(item.idVehiculo ?: 0, item)
            if (response.isSuccessful) {
                Log.i("ResponseDisableItem", response.body().toString())
            } else {
                Log.e("ResponseDisableItem", response.errorBody().toString())
            }
        }
    }

    private fun startViewVehiculo(item: VehiculoItem, context: Context) {
        val intent = Intent(context, VehiculoUsuarioActivity::class.java)
        intent.putExtra("vehiculo", item.idVehiculo)
        context.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehiculos_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listVehiculos.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listVehiculos[position], position)
    }
}