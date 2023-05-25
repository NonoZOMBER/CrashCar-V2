package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 24/05/2023.
 */
class TestigosList() : ArrayList<TestigoItem>()

data class TestigoItem(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("direccion")
    var direccion: String,
    @SerializedName("telefono")
    var telefono: String
) {
    constructor(nombre: String, direccion: String, telefono: String): this(null, nombre, direccion, telefono)
}
