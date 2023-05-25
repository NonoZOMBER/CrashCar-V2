package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName


class ListSeguros : ArrayList<SegurosItem>()

data class SegurosItem(
    @SerializedName("activo")
    var activo: Boolean,
    @SerializedName("direccion")
    var direccionAgencia: String,
    @SerializedName("idUsuario")
    var dniUsuario: String,
    @SerializedName("email")
    var emailAgencia: String,
    @SerializedName("id")
    var idSeguro: Int?,
    @SerializedName("idsConductoresSeguro")
    var idsConductoresSeguro: String,
    @SerializedName("idsVehiculosSeguro")
    var idsVehiculosSeguro: String,
    @SerializedName("nombre")
    var nombreAgencia: String,
    var nombreAseguradora: String,
    @SerializedName("pais")
    var paisAgencia: String,
    @SerializedName("telefono")
    var phoneAgencia: String
)