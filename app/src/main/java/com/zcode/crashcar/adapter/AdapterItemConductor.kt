package com.zcode.crashcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ConductorItem

/*
 *    Created by Nono on 24/04/2023.
 */
class AdapterItemConductor(private val conductores: MutableList<ConductorItem>, private val onSizeChangeListener: OnSizeChangeListener?, private val onItemClickListener: OnItemClickListener, private val delete: Boolean) :
    RecyclerView.Adapter<AdapterItemConductor.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: ConductorItem, position: Int)
    }

    interface OnSizeChangeListener {
        fun onChangeListConductor(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         private val textDni: TextView
         private val textNombre: TextView
         private val textPhone: TextView
         private val container: CardView
         private val btnDeleteConductor: ImageButton

         init {
             textNombre = view.findViewById(R.id.textNameDriverItem)
             textDni = view.findViewById(R.id.textDNIDriverItem)
             textPhone = view.findViewById(R.id.textPhoneDriverItem)
             container = view.findViewById(R.id.container_card_conductor)
             btnDeleteConductor = view.findViewById(R.id.btnDeleteConductorItem)
         }

        fun bind(item: ConductorItem, position: Int) {
            textDni.text = String.format(itemView.context.getString(R.string.lbl_dni_item), item.dniConductor)
            textNombre.text = item.nombre
            textPhone.text = String.format(itemView.context.getString(R.string.lbl_phone_item), item.phone)
            container.setOnClickListener { onItemClickListener.onItemClick(item, position) }
            btnDeleteConductor.setOnClickListener { deleteDialogConductor(position, itemView.context) }
            if (!delete) btnDeleteConductor.visibility = View.GONE
        }
    }

    private fun deleteDialogConductor(position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_conductor))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                onSizeChangeListener?.onChangeListConductor(position)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conductor, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = conductores.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conductores[position], position)
    }
}