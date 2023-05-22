package com.zcode.crashcar.utils

import android.content.Context
import android.util.Patterns
import android.view.inputmethod.InputMethodManager

/*
 *    Created by Nono on 20/05/2023.
 */
object Herramientas {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


}