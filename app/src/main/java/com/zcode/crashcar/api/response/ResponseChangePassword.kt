package com.zcode.crashcar.api.response

import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 23/05/2023.
 */
data class ResponseChangePassword(
    @SerializedName("code")
    var code: Int,
    @SerializedName("msg")
    var msg: String
)

