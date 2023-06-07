package com.zcode.crashcar.api.service

import com.zcode.crashcar.api.controller.Asegurado
import com.zcode.crashcar.api.controller.ChangePassword
import com.zcode.crashcar.api.controller.ConductorItem
import com.zcode.crashcar.api.controller.ListPartes
import com.zcode.crashcar.api.controller.ListSeguros
import com.zcode.crashcar.api.controller.LoginClass
import com.zcode.crashcar.api.controller.ParteItem
import com.zcode.crashcar.api.controller.RegisterClass
import com.zcode.crashcar.api.controller.SegurosItem
import com.zcode.crashcar.api.controller.TestigoItem
import com.zcode.crashcar.api.controller.VehiculoItem
import com.zcode.crashcar.api.controller.VehiculoParte
import com.zcode.crashcar.api.controller.VehiculoSeguro
import com.zcode.crashcar.api.controller.VehiculosUsuario
import com.zcode.crashcar.api.response.ResponseAuth
import com.zcode.crashcar.api.response.ResponseChangePassword
import com.zcode.crashcar.api.response.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    @POST("auth/change-password")
    suspend fun changePassword(@Body changePassword: ChangePassword): Response<ResponseChangePassword>

    @PUT("update/user/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: Usuario): Response<Usuario>

    @GET("list/partes/{id}")
    suspend fun getListPartes(@Path("id") idUser: String): Response<ListPartes>

    @GET("list/seguros/{id}")
    suspend fun getListSeguros(@Path("id") idUser: String): Response<ListSeguros>

    @GET("list/vehiculo/{id}")
    suspend fun getListVehiculos(@Path("id") idUser: String): Response<VehiculosUsuario>

    @GET("id/seguro/{id}")
    suspend fun getSeguro(@Path("id") idSeguro: Int): Response<SegurosItem>

    @POST("new/conductor")
    suspend fun saveConductor(
        @Body conductor: ConductorItem
    ): Response<ConductorItem>

    @PUT("update/conductor/{id}")
    suspend fun updateConductor(
        @Path("id") idConductor: Int,
        @Body conductor: ConductorItem
    ): Response<ConductorItem>

    @GET("id/conductor/{id}")
    suspend fun getConductor(@Path("id") idConductor: Int): Response<ConductorItem>

    @POST("new/seguro")
    suspend fun saveSeguro(@Body seguro: SegurosItem): Response<SegurosItem>

    @PUT("update/seguro/{id}")
    suspend fun updateSeguro(
        @Path("id") idSeguro: Int,
        @Body seguro: SegurosItem
    ): Response<SegurosItem>

    @POST("new/vehiculo")
    suspend fun newVehiculo(@Body vahiculo: VehiculoItem): Response<VehiculoItem>

    @PUT("update/vehiculo/{id}")
    suspend fun updateVehiculo(
        @Path("id") idVehiculo: Int,
        @Body vehiculo: VehiculoItem
    ): Response<VehiculoItem>

    @GET("id/vehiculo/{id}")
    suspend fun getVehiculo(@Path("id") idVehiculo: Int): Response<VehiculoItem>

    @GET("id/vehiculo-seguro/{id}")
    suspend fun getVehiculoSeguro(@Path("id") idVehiculo: Int): Response<VehiculoSeguro>

    @POST("new/vehiculo-seguro")
    suspend fun saveVehiculoSeguro(
        @Body vehiculoSeguro: VehiculoSeguro
    ): Response<VehiculoSeguro>

    @PUT("update/vehiculo-seguro/{id}")
    suspend fun updateVehiculoSeguro(
        @Path("id") idVehiculo: Int,
        @Body vehiculoSeguro: VehiculoSeguro
    ): Response<VehiculoSeguro>

    @PUT("update/parte/{id}")
    suspend fun updateParte(
        @Path("id") idParte: Int,
        @Body itemParte: ParteItem
    ): Response<ParteItem>

    @POST("new/testigo")
    suspend fun saveTestigo(@Body newTestigo: TestigoItem): Response<TestigoItem>

    @PUT("update/testigo/{id}")
    suspend fun updateTestigo(
        @Path("id") idTestigo: Int,
        @Body itemTestigo: TestigoItem
    ): Response<TestigoItem>

    @POST("new/asegurado")
    suspend fun saveAsegurado(@Body asegurado: Asegurado): Response<Asegurado>

    @POST("new/vehiculo-parte")
    suspend fun saveVehiculoParte(@Body vehiculoParte: VehiculoParte): Response<VehiculoParte>

    @POST("new/parte")
    suspend fun saveParte(@Body parteItem: ParteItem): Response<ParteItem>

    @GET("id/vehiculo-parte/{id}")
    suspend fun getVehiculoParte(@Path("id") id: Int): Response<VehiculoParte>

    @GET("id/asegurado/{id}")
    suspend fun getAsegurado(@Path("id") id: Int): Response<Asegurado>

    @GET("id/testigo/{id}")
    suspend fun getTestigo(@Path("id") id: Int): Response<TestigoItem>
    @GET("id/parte/{id}")
    suspend fun getParte(@Path("id") id: Int): Response<ParteItem>
}