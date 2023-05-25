package com.zcode.crashcar.utils

import android.util.Patterns
import com.example.crashcar.tools.objects.SpinnerItemsTypesVehicles
import com.zcode.crashcar.R

/*
 *    Created by Nono on 20/05/2023.
 */
object Herramientas {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateDNI(dni: String): Boolean {
        if (dni.length != 9) {
            return false
        }

        val letrasDNI = "TRWAGMYFPDXBNJZSQVHLCKE"

        return try {
            val numero = dni.substring(0, 8).toInt()
            val letra = dni.substring(8, 9).uppercase()
            val resto = numero % 23
            val letraCalculada = letrasDNI[resto]
            letraCalculada.toString() == letra
        } catch (e: Exception) {
            false
        }
    }

    fun getSpinnerItems(): List<SpinnerItemsTypesVehicles> {
        return listOf(
            SpinnerItemsTypesVehicles(R.drawable.ic_car_type, "Turismo"),
            SpinnerItemsTypesVehicles(R.drawable.ic_truck_type, "Furgoneta/Camión"),
            SpinnerItemsTypesVehicles(R.drawable.ic_motocicle_type, "Motocicleta")
        )
    }

    fun getIconVehicle(type: String): Int {
        return when(type) {
            "turismo" -> R.drawable.ic_car_type
            "furgoneta/camión" -> R.drawable.ic_truck_type
            "motocicleta" -> R.drawable.ic_motocicle_type
            else -> 0
        }
    }


}