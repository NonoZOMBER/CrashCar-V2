package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 13/05/2023.
 */
data class VehiculoParte(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("idVehiculo")
    var idVehiculo: Int? = null,
    @SerializedName("idSeguro")
    var idSeguro: Int? = null,
    @SerializedName("circunstancias")
    var circunstancias: String = "", // Lista JSON de objeto preguntas en total 21 Preguntas
    @SerializedName("remolque")
    var remolque: Boolean = false,
    @SerializedName("matriculaRemolque")
    var matriculaRemolque: String? = "",
    @SerializedName("paisMatriculaRemolque")
    var paisMatriculaRemolque: String? = "",
    @SerializedName("puntoChoque")
    var puntoChoque: String = "", // El String es el enum seleccionado para marcar el punto de choque del vehiculo
    @SerializedName("firma")
    var firma: String = "",
    @SerializedName("observaciones")
    var observaciones: String = "",
    @SerializedName("idAsegurado")
    var idAsegurado: Int? = null,
    @SerializedName("idConductor")
    var idConductor: Int? = null,
    @SerializedName("numeroPermisoConducir")
    var numeroConducir: String = "",
    @SerializedName("categoriaPermisoConducir")
    var categoriaPermiso: String = "",
    @SerializedName("fechaValidezPermisoConducir")
    var fechaVelidezPermiso: String = ""
)
