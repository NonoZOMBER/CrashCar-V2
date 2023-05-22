package com.zcode.crashcar.utils

import com.google.gson.GsonBuilder
import com.zcode.crashcar.api.service.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 *    Created by Nono on 20/05/2023.
 */
object RetrofitObject {

    private const val URL_BASE: String = "http://152.228.218.35:8000/crash-car/"

    fun getCallRetrofit(): ApiService {
        return retrofit().create(ApiService::class.java)
    }

    private fun retrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}