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
import com.zcode.crashcar.api.controller.TestigoItem

/*
 *    Created by Nono on 24/05/2023.
 */
class AdapterItemTestigo(
    private val listTestigos: List<TestigoItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdapterItemTestigo
.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: TestigoItem, position: Int)
        fun deleteItem(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val name: TextView
        private val address: TextView
        private val phone: TextView
        private val btnDelete: ImageButton
        private val container: CardView

        init {
            name = view.findViewById(R.id.textNameTestigoItem)
            address = view.findViewById(R.id.textDireccionTestigoItem)
            phone = view.findViewById(R.id.textPhoneTestigoItem)
            btnDelete = view.findViewById(R.id.btnDeleteTestigoItem)
            container = view.findViewById(R.id.container_card_testigo)
        }

        fun bind(item: TestigoItem, position: Int) {
            name.text = item.nombre
            address.text = String.format(itemView.context.getString(R.string.lblDireccionTestigoItem), item.direccion)
            phone.text = String.format(itemView.context.getString(R.string.lbl_phone_item), item.telefono)
            btnDelete.setOnClickListener {
                showDialogDeleteItem(item, position, itemView.context)
            }
            container.setOnClickListener { listener.onItemClick(item, position) }
        }
    }

    private fun showDialogDeleteItem(item: TestigoItem, position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_conductor))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                listener.deleteItem(position)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_testigo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listTestigos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTestigos[position], position)
    }
}