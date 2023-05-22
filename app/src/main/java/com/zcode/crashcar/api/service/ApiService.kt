package com.zcode.crashcar.api.service

import com.zcode.crashcar.api.controller.LoginClass
import com.zcode.crashcar.api.controller.RegisterClass
import com.zcode.crashcar.api.response.ResponseAuth
import com.zcode.crashcar.api.response.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/*
 *    Created by Nono on 20/05/2023.
 */
interface ApiService {
    @POST("auth/login")
    suspend fun loginUser(@Body loginClass: LoginClass): Response<ResponseAuth>

    @POST("auth/register")
    suspend fun registerUser(@Body registerClass: RegisterClass): Response<ResponseAuth>

    @PUT("update/user/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: Usuario): Response<Usuario>
}