package com.zcode.crashcar.api.controller


import com.google.gson.annotations.SerializedName

data class ConductorItem(
    @SerializedName("apellidos")
    var apellidos: String,
    @SerializedName("codpostal")
    var codpostal: Int,
    @SerializedName("direccion")
    var direccion: String,
    @SerializedName("dni")
    var dniConductor: String,
    @SerializedName("email")
    var email: String,
    @SerializedName("id")
    var idConductor: Int?,
    @SerializedName("localidad")
    var localidad: String,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("pais")
    var pais: String,
    @SerializedName("phone")
    var phone: String
)