package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 29/05/2023.
 */
data class Asegurado(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("direccion")
    val direccion: String,
    @SerializedName("codpostal")
    val codPostas: String,
    @SerializedName("pais")
    val pais: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("email")
    val email: String
)
