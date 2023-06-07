package com.zcode.crashcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 *    Created by Nono on 13/05/2023.
 */
class AdapterItemParte(
    private val listPartes: ArrayList<ParteItem>,
    private val onSizeChange: OnSizeChange?,
    private val onItemSelect: OnItemSelect?
) :
    RecyclerView.Adapter<AdapterItemParte.ViewHolder>() {

    interface OnSizeChange {
        fun onChangeSize(size: Int)
    }

    interface OnItemSelect {
        fun onItemSelect(item: ParteItem)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val textIdParte: TextView
        private val textFechaParte: TextView
        private val btnRemove: ImageButton
        private val container: CardView
        private val context = itemView.context

        init {
            textIdParte = view.findViewById(R.id.textNumParteItem)
            textFechaParte = view.findViewById(R.id.textFechaParteItem)
            btnRemove = view.findViewById(R.id.btnRemoveItemParte)
            container = view.findViewById(R.id.container_card_parte)
        }

        fun bind(item: ParteItem, position: Int) {
            textIdParte.text = String.format(context.getString(R.string.lblNumeroParte), item.idParte)
            textFechaParte.text = String.format(context.getString(R.string.lblFechaParte), item.fechAccidente)
            btnRemove.setOnClickListener { deleteDialogParte(item, position, context) }
            container.setOnClickListener { onItemSelect?.onItemSelect(item) }
        }
    }

    private fun deleteDialogParte(item: ParteItem, position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_parte))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                item.activo = false
                changeItemActive(item)
                listPartes.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listPartes.size - position)
                onSizeChange?.onChangeSize(listPartes.size)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun changeItemActive(item: ParteItem) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().updateParte(item.idParte ?: 0, item)
            if (response.isSuccessful) {
                Log.i("UpdateParteActive", response.message().toString())
            } else {
                Log.e("UpdateParteActive", response.errorBody().toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parte, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listPartes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listPartes[position], position)
    }
}