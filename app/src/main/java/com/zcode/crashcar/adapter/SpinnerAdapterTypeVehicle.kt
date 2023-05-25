package com.zcode.crashcar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.crashcar.tools.objects.SpinnerItemsTypesVehicles
import com.zcode.crashcar.R

/*
 *    Created by Nono on 04/05/2023.
 */
class SpinnerAdapterTypeVehicle(context: Context, items: List<SpinnerItemsTypesVehicles>) :
    ArrayAdapter<SpinnerItemsTypesVehicles>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_type_vehicle, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.spinner_img_icon)
        val text = view.findViewById<TextView>(R.id.spinner_text_type)

        item?.icon?.let { imageView.setImageResource(it) }
        text.text = item?.text

        return view
    }
}