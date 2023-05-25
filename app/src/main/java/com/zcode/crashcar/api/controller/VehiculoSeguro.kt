package com.zcode.crashcar.api.controller


import com.google.gson.annotations.SerializedName

data class VehiculoSeguro(
    @SerializedName("fechaFinCv")
    var fechaCartaVerdeFin: String,
    @SerializedName("fechaInicioCv")
    var fechaCartaVerdeInicio: String,
    @SerializedName("idVehiculo")
    var idVehiculo: Int,
    @SerializedName("id")
    var idVehiculoSeguro: Int?,
    @SerializedName("numeroCartaVerde")
    var numeroCartaVerde: String,
    @SerializedName("numeroPoliza")
    var numeroPoliza: String
)