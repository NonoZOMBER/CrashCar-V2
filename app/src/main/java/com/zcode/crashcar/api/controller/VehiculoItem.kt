package com.zcode.crashcar.api.controller


import com.google.gson.annotations.SerializedName

class VehiculosUsuario : ArrayList<VehiculoItem>()

data class VehiculoItem(
    @SerializedName("activo")
    var activo: Boolean,
    @SerializedName("idUsuario")
    var dniUsuario: String,
    @SerializedName("id")
    var idVehiculo: Int?,
    @SerializedName("marca")
    var marca: String,
    @SerializedName("matricula")
    var matricula: String,
    @SerializedName("modelo")
    var modelo: String,
    @SerializedName("paisMatricula")
    var paisMatricula: String,
    @SerializedName("tipoVehiculo")
    var tipoVehiculo: String
) {
    constructor(
        activo: Boolean,
        dniUsuario: String,
        marca: String,
        matricula: String,
        modelo: String,
        paisMatricula: String,
        tipoVehiculo: String
    ) : this(
        activo,
        dniUsuario,
        null,
        marca,
        matricula,
        modelo,
        paisMatricula,
        tipoVehiculo
    )
}