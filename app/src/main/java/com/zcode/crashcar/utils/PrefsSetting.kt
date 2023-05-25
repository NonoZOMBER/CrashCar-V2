package com.zcode.crashcar.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.zcode.crashcar.api.response.Usuario
import java.net.IDN

/*
 *    Created by Nono on 20/03/2023.
 */
class PrefsSetting(context: Context) {

    private val SHARE_NAME_LOGIN = "setting_login"
    private val SHARED_USER_EMAIL = "email"
    private val SHARED_USER_PASSWORD = "password"
    private val SHARED_USER_REMEMBER = "remember"
    private val SHARED_USER_TYPELOGIN = "type_login"
    private val SHARED_USER = "user"
    private val SHARED_USER_ID = "id"

    private val storageSetting: SharedPreferences =
        context.getSharedPreferences(SHARE_NAME_LOGIN, Context.MODE_PRIVATE)

    fun saveEmail(email: String) {
        this.storageSetting.edit().putString(SHARED_USER_EMAIL, email).apply()
    }

    fun savePassword(pass: String) {
        this.storageSetting.edit().putString(SHARED_USER_PASSWORD, pass).apply()
    }

    fun saveRemember(isCheck: Boolean) {
        this.storageSetting.edit().putBoolean(SHARED_USER_REMEMBER, isCheck).apply()
    }

    private fun saveTypeLogin(type: Int) {
        this.storageSetting.edit().putInt(SHARED_USER_TYPELOGIN, type).apply()
    }

    fun getEmail(): String {
        return this.storageSetting.getString(SHARED_USER_EMAIL, "").toString()
    }

    fun getPassword(): String {
        return this.storageSetting.getString(SHARED_USER_PASSWORD, "").toString()
    }

    fun getRemember(): Boolean {
        return this.storageSetting.getBoolean(SHARED_USER_REMEMBER, false)
    }

    fun getTypeLogin(): Int {
        return this.storageSetting.getInt(SHARED_USER_TYPELOGIN, 1)
    }

    fun saveCredentials(email: String, pass: String, isCheck: Boolean, type: Int, idUser: String) {
        saveEmail(email)
        savePassword(pass)
        saveRemember(isCheck)
        saveTypeLogin(type)
        saveID(idUser)
    }

    private fun saveID(id: String) {
        this.storageSetting.edit().putString(SHARED_USER_ID, id).apply()
    }

    fun getIdUser(): String {
        return this.storageSetting.getString(SHARED_USER_ID, "").toString()
    }

    fun saveUser(usuario: Usuario) {
        val userString = Gson().toJson(usuario)
        this.storageSetting.edit().putString(SHARED_USER, userString).apply()
    }

    fun getUser(): Usuario? {
        var user: Usuario? = null
        val userString = this.storageSetting.getString(SHARED_USER, null)
        if (userString != null) {
            user = Gson().fromJson(userString, Usuario::class.java)
        }
        return user
    }

    fun resetPrefs() {
        this.storageSetting.edit().clear().apply()
    }

}