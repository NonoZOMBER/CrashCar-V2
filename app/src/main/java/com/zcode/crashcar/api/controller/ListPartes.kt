package com.zcode.crashcar.api.controller

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/*
 *    Created by Nono on 13/05/2023.
 */
class ListPartes : ArrayList<ParteItem>()

data class ParteItem(
    @SerializedName("idUsuario")
    var dniUsuario: String? = "",
    @SerializedName("idParte")
    var idParte: Int? = null,
    @SerializedName("idsVehiculosParte")
    var vehiculosParte: String? = "", //Es un JSON con una lista de objetos VehiculoParte
    @SerializedName("descripcion")
    var descripcion: String? = "",
    @SerializedName("fechaAccidente")
    var fechAccidente: String? = "",
    @SerializedName("direccion")
    var direccion: String? = "",
    @SerializedName("victimas")
    var visctimas: Boolean = false,
    @SerializedName("damageMaterial")
    var damageMaterial: Boolean = false,
    @SerializedName("intervencionAutoridades")
    var isAutoridades: Boolean = false,
    @SerializedName("numeroPlacaAutoridad")
    var nPlacaAutoridad: String? = null,
    @SerializedName("imagenes")
    var imagenes: String? = "", // Es un JSON con una lista de objetos Imagenes
    @SerializedName("testigo")
    var testigo: String? = "", // Es un JSON con una lista de nombres, direccion y teléfono de los testigos
    @SerializedName("activo")
    var activo: Boolean = false,
    var isOtherVehicles: Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dniUsuario)
        parcel.writeValue(idParte)
        parcel.writeString(vehiculosParte)
        parcel.writeString(descripcion)
        parcel.writeString(fechAccidente)
        parcel.writeString(direccion)
        parcel.writeByte(if (visctimas) 1 else 0)
        parcel.writeByte(if (damageMaterial) 1 else 0)
        parcel.writeByte(if (isAutoridades) 1 else 0)
        parcel.writeString(nPlacaAutoridad)
        parcel.writeString(imagenes)
        parcel.writeString(testigo)
        parcel.writeByte(if (activo) 1 else 0)
        parcel.writeByte(if (isOtherVehicles) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParteItem> {
        override fun createFromParcel(parcel: Parcel): ParteItem {
            return ParteItem(parcel)
        }

        override fun newArray(size: Int): Array<ParteItem?> {
            return arrayOfNulls(size)
        }
    }
}