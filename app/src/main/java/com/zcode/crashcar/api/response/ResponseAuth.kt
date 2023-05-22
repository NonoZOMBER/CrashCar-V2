package com.zcode.crashcar.api.response


import com.google.gson.annotations.SerializedName

data class ResponseAuth(
    @SerializedName("code")
    var code: Int?,
    @SerializedName("msg")
    var msg: String?,
    @SerializedName("usuario")
    var usuario: Usuario?
)

data class Usuario(
    @SerializedName("apellidos")
    var apellidos: String?,
    @SerializedName("codpostal")
    var codpostal: String?,
    @SerializedName("direccion")
    var direccion: String?,
    @SerializedName("email")
    var email: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("localidad")
    var localidad: String?,
    @SerializedName("nombre")
    var nombre: String?,
    @SerializedName("pais")
    var pais: String?,
    @SerializedName("password")
    var password: String?,
    @SerializedName("provincia")
    var provincia: String?,
    @SerializedName("telefono")
    var telefono: String?,
    @SerializedName("tipologin")
    var tipologin: Int?
)