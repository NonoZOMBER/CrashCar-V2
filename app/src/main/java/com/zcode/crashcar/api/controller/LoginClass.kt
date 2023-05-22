package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 20/05/2023.
 */
data class LoginClass(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("tipo_login")
    val tipoLogin: Int
)
