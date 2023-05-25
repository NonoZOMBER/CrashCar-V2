package com.zcode.crashcar.adapter
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R
import com.zcode.crashcar.api.controller.ListSeguros
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.ui.home.activities.seguros.SeguroActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 *    Created by Nono on 21/04/2023.
 */



class AdapterItemSeguros(
    private val listSeguros: ListSeguros,
    private val onSizeChangeListener: OnSizeChangeListener,
    private val tipe: Boolean,
    private val click: Boolean
) :
    RecyclerView.Adapter<AdapterItemSeguros.ViewHolder>() {

    interface OnSizeChangeListener {
        fun onChange(sizeList: Int)

        fun onItem(item: SegurosItem)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameItem: TextView
        private val phoneItem: TextView
        private val btnDelete: ImageButton
        private val container: CardView

        init {
            nameItem = view.findViewById(R.id.textNameSeguroItem)
            phoneItem = view.findViewById(R.id.textPhoneSeguroItem)
            btnDelete = view.findViewById(R.id.btnDeleteSeguroItem)
            container = view.findViewById(R.id.container_card_seguro)
        }

        fun bind(item: SegurosItem, position: Int) {
            if (tipe) {
                btnDelete.visibility = View.INVISIBLE
            } else {
                btnDelete.visibility = View.VISIBLE
            }

            if (item.activo) {
                nameItem.text = String.format(
                    itemView.context.getString(R.string.lbl_aseguradora),
                    item.nombreAgencia
                )
                phoneItem.text = String.format(
                    itemView.context.getString(R.string.lbl_phone_item),
                    item.phoneAgencia
                )

                btnDelete.setOnClickListener {
                    deleteDialogSeguros(item, position, itemView.context)
                }

                container.setOnClickListener {
                    if (click) {
                        startViewSeguro(
                            item,
                            itemView.context
                        )
                    } else {
                        onSizeChangeListener.onItem(item)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seguro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listSeguros.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listSeguros[position], position)
    }

    private fun startViewSeguro(seguro: SegurosItem, context: Context) {
        val intent = Intent(context, SeguroActivity::class.java)
        intent.putExtra("seguro", seguro.idSeguro)
        context.startActivity(intent)
    }

    private fun deleteDialogSeguros(item: SegurosItem, position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_seguro))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                item.activo = false
                changeItemActive(item)
                listSeguros.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listSeguros.size - position)
                onSizeChangeListener.onChange(listSeguros.size)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun changeItemActive(item: SegurosItem) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitObject.getCallRetrofit().updateSeguro(item.idSeguro?:0, item)
            if (response.isSuccessful) {
                Log.i("UpdateSeguroActive", response.message().toString())
            } else {
                Log.e("UpdateSeguroActive", response.errorBody().toString())
            }
        }
    }
}