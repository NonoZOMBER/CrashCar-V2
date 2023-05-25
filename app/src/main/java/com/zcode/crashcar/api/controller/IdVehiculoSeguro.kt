package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 05/05/2023.
 */

class ListIdVehiculoSeguro: ArrayList<IdVehiculoSeguro>()
data class IdVehiculoSeguro(@SerializedName("idVehiculoSeguro") val idVehiculoSeguro: Int)