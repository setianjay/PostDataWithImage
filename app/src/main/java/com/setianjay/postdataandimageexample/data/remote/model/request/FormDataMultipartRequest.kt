package com.setianjay.postdataandimageexample.data.remote.model.request

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

data class FormDataMultipartRequest(
    @Part("id-pelapor") val idPelapor: RequestBody,
    @Part("nama-pelapor") val namaPelapor: RequestBody,
    @Part("waktu-kejadian") val waktuKejadian: RequestBody,
    @Part("jenis") val jenisKecelakaan: RequestBody,
    @Part("lokasi-kejadian") val lokasiKejadian: RequestBody,
    @Part("notelp") val notelp: RequestBody,
    @Part val file: MultipartBody.Part
)
