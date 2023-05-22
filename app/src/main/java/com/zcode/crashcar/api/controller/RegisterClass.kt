package com.zcode.crashcar.api.controller

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 20/05/2023.
 */
data class RegisterClass(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("tipologin")
    val tipoLogin: Int
)
