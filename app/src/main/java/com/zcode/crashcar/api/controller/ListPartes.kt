package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 13/05/2023.
 */
class ListPartes : ArrayList<ParteItem>()

data class ParteItem(
    @SerializedName("idUsuario")
    var idUsuario: String? = "",
    @SerializedName("id")
    var idParte: Int? = null,
    @SerializedName("idsVehiculosParte")
    var vehiculosParte: String? = "", //Es un JSON con una lista de objetos VehiculoParte
    @SerializedName("horaAccidente")
    var horaAccidente: String? = "",
    @SerializedName("paisAccidente")
    var paisAccidente: String? = "",
    @SerializedName("fechaAccidente")
    var fechAccidente: String? = "",
    @SerializedName("direccion")
    var direccion: String? = "",
    @SerializedName("victimas")
    var visctimas: Boolean = false,
    @SerializedName("damageMaterial")
    var damageMaterial: Boolean = false,
    @SerializedName("imagenes")
    var imagenes: String? = "", // Es un JSON con una lista de objetos Imagenes
    @SerializedName("idsTestigos")
    var testigo: String? = "", // Es un JSON con una lista de nombres, direccion y teléfono de los testigos
    @SerializedName("activo")
    var activo: Boolean = false,
    @SerializedName("otherVehicles")
    var isOtherVehicles: Boolean = false
)