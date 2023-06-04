package com.zcode.crashcar.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zcode.crashcar.R

/*
 *    Created by Nono on 04/06/2023.
 */
class AdapterImagesParte(
    private val listImages: ArrayList<Bitmap>,
    private val delete: Boolean
) : RecyclerView.Adapter<AdapterImagesParte.ViewHolder>() {

    interface OnClickImageListener {
        fun deleteItem(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView
        private val btnDelete: ImageButton

        init {
            img = view.findViewById(R.id.imgItemImage)
            btnDelete = view.findViewById(R.id.btnDeleteImage)
        }

        fun bind(item: Bitmap, position: Int) {
            img.post {
                img.setImageBitmap(item)
            }

            btnDelete.setOnClickListener {
                showDialogDelete(position, itemView.context)
            }

            if (!delete) btnDelete.visibility = View.GONE
        }
    }

    private fun showDialogDelete(position: Int, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.query_delete_image))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.affirmationResponse)) { _, _ ->
                listImages.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listImages.size - position)
            }
            .setNegativeButton(context.getString(R.string.negationResponse)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_parte, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listImages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listImages[position], position)
    }

}