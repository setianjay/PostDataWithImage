package com.setianjay.postdataandimageexample.data.remote.service.sipela

import com.setianjay.postdataandimageexample.data.remote.model.response.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserService {

    @Multipart
    @POST("create-pelaporan.php")
    fun createReporting(
        @Part("id-pelapor") idPelapor: RequestBody,
        @Part("nama-pelapor") namaPelapor: RequestBody,
        @Part("waktu-kejadian") waktuKejadian: RequestBody,
        @Part("jenis") jenisKecelakaan: RequestBody,
        @Part("lokasi-kejadian") lokasiKejadian: RequestBody,
        @Part("notelp") notelp: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<LoginResponse>

/*
    Untuk mengetest kalau ada request yang salah ke web service
    @GET("create-pelaporan.php")
    fun createReporting(): Call<LoginResponse>*/
}