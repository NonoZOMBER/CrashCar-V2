package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 13/05/2023.
 */
data class VehiculoParte(
    @SerializedName("id")
    val idVehiculoParte: Int,
    @SerializedName("idVehiculo")
    val idVehiculoSeguro: Int,
    @SerializedName("idSeguro")
    val idSeguro: Int,
    @SerializedName("circunstancias")
    val circunstancias: String, // Lista JSON de objeto preguntas en total 21 Preguntas
    @SerializedName("remolque")
    val remolque: Boolean,
    @SerializedName("matriculaRemolque")
    val matriculaRemolque: String,
    @SerializedName("paisMatriculaRemolque")
    val paisMatriculaRemolque: String,
    @SerializedName("puntoChoque")
    val puntoChoque: String // El String es el enum seleccionado para marcar el punto de choque del vehiculo
)
