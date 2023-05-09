package com.setianjay.postdataandimageexample.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.setianjay.postdataandimageexample.R
import com.setianjay.postdataandimageexample.data.remote.model.response.ErrorResponse
import com.setianjay.postdataandimageexample.data.remote.model.response.LoginResponse
import com.setianjay.postdataandimageexample.data.remote.service.sipela.UserService
import com.setianjay.postdataandimageexample.databinding.ActivityMainBinding
import com.setianjay.postdataandimageexample.helper.view.FormValidationHelper
import com.setianjay.postdataandimageexample.helper.view.ImageHelper
import com.setianjay.postdataandimageexample.helper.view.model.FormValidationHelperModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


/**
 *
 * Yang masih bug:
 * - Permission, jika permission di tolak aplikasi keluar dan apabila kita kembali masuk ke aplikasi
 * maka akan langsung keluar
 * - Compress image hanya berfungsi untuk kamera belakang, kamera depan akan langsung force close.
 *
 * Yang harus dikerjakan:
 * - Validasi input empty (Done)
 * - Validasi image empty (Done)
 * - Fitur camera + save gambar ke gallery (Done)
 * - Compress image yang di save ke gallery agar size nya menjadi kecil (Done)
 * - Mendapatkan nilai dari masing-masing form (Done)
 * - Mengirim semua nilai dari masing-masing form ke server menggunakan retrofit (Done)
 * - Menghandle request gagal (Done)
 * */

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    /* input */
    private var photoUri: Uri? = null
    private var photoFilePath: String = ""
    private var photoBitmap: Bitmap? = null
    private val idPelapor = "1"
    private lateinit var namaPelapor: String
    private lateinit var lokasiKejadian: String
    private lateinit var jenisKecelakaan: String
    // dalam hal ini tgl pelaporan tidak dipakai karena sudah di handle di server
    // private val tglPelaporan = DateUtil.dateWithFormat("yyyy-MM-dd")
    private lateinit var waktuKejadian: String
    private lateinit var notelp: String

    @Inject
    lateinit var formValidationHelper: FormValidationHelper

    @Inject
    lateinit var imageHelper: ImageHelper

    @Inject
    lateinit var userService: UserService

    // camera launcher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {

                imageHelper.compressImage(photoFilePath, 2.0)

                photoUri?.let { uri ->
                    photoBitmap = imageHelper.uriToBitmap(uri)
//                    binding.ivBuktiFoto.setImageBitmap(photoBitmap)
                    binding.ivBuktiFoto.setImageURI(uri)
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAllPermission()
        initListener()
        getAddress()
        /**
         * untuk membuat sebuah RequestBody di POST method
         * */
//        val imageFile = File("sadfafsafassf")
//        val himageRequestBody = imageFile.asRequestBody()
//        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
//        val testString = "wkwkw"
//        testString.toRequestBody(contentType = "text/plain".toMediaType())
    }

    private fun initListener() {
        binding.also { view ->
            view.btnPhoto.setOnClickListener(this)
            view.btnLapor.setOnClickListener(this)
            view.spinnerLokasiKejadian.onItemSelectedListener = this
            view.spinnerJenisKecelakaan.onItemSelectedListener = this
        }
    }


    private fun getAddress() {
        val geo = Geocoder(this, Locale.getDefault())
        val latitude = "-6.4506505"
        val longitude = "106.8027575"
        var addressList: List<Address> = mutableListOf()

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            geo.getFromLocation(
                latitude.toDouble(),
                longitude.toDouble(),
                1,
                object : GeocodeListener {
                    override fun onGeocode(p0: MutableList<Address>) {
                        addressList = p0
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)
                    }
                })
        } else {
            addressList =
                geo.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1) as List<Address>
        }

        if (addressList.isNotEmpty()) {
            val address = addressList[0]

            binding.tvResult.text = "Admin area ${address.adminArea}, Sub Admin Area ${address.subAdminArea}, Country Name ${address.countryName}, Country Code ${address.countryCode}, locality ${address.locality}, kode post ${address.postalCode}, kelurahan ${address.subLocality}, thoroughfare ${address.thoroughfare}, alamat detail: ${address.getAddressLine(0)}"
            val foundList = kmpSearch(PATTERN_LOCATION, address.getAddressLine(0))

            if (foundList.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "Lokasi kamu diwilayah depok", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Lokasi kamu belum tercover oleh sistem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_photo -> {
                if (allPermissionGranted()) {
                    takePhoto()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Kamu membutuhkan perizinan kamera",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.btn_lapor -> inputProcess()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (p2 != 0) {
            when (p0?.id) {
                R.id.spinner_lokasi_kejadian -> {
                    lokasiKejadian = p0.getItemAtPosition(p2).toString()
                }
                R.id.spinner_jenis_kecelakaan -> {
                    jenisKecelakaan = p2.toString()
                }
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun takePhoto() {
        photoUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".camera-file-provider",
            imageHelper.saveImageToGallery().also {
                photoFilePath = it.absolutePath
                Toast.makeText(
                    this@MainActivity,
                    "File Path: ${it.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        cameraLauncher.launch(photoUri)
    }

    private fun inputProcess() {
        if (photoUri == null) {
            Toast.makeText(this@MainActivity, "Image kosong", Toast.LENGTH_SHORT).show()
        }

        val isAllPassed = formValidationHelper.checkIsEmpty(
            FormValidationHelperModel(binding.etIdPelapor, INPUT_ID_PELAPOR),
            FormValidationHelperModel(binding.etNamaPelapor, INPUT_NAMA_PELAPOR),
            FormValidationHelperModel(binding.spinnerLokasiKejadian, INPUT_SPINNER_LOKASI_KEJADIAN),
            FormValidationHelperModel(
                binding.spinnerJenisKecelakaan,
                INPUT_SPINNER_JENIS_KECELAKAAN
            ),
            FormValidationHelperModel(binding.etWaktuKejadian, INPUT_WAKTU_KEJADIAN),
            FormValidationHelperModel(binding.etNotelpPelapor, INPUT_NOTELP_PELAPOR)
        )

        if (isAllPassed && photoUri != null) {
            namaPelapor = binding.etNamaPelapor.text.toString()
            waktuKejadian = binding.etWaktuKejadian.text.toString()
            notelp = binding.etNotelpPelapor.text.toString()

//            Toast.makeText(this@MainActivity, "Id Pelapor: $idPelapor", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Nama Pelapor: $namaPelapor", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Lokasi Kejadian: $lokasiKejadian", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Jenis Kecelakaan: $jenisKecelakaan", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Tanggal Pelaporan: $tglPelaporan", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Waktu Kejadian: $waktuKejadian", Toast.LENGTH_LONG).show()
//            Toast.makeText(this@MainActivity, "Notelp: $notelp", Toast.LENGTH_LONG).show()

            /* set request body for post process */
            val file = File(photoFilePath)
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val filePart = MultipartBody.Part.createFormData("fotos", file.name, requestFile)

//            val dataMap = mapOf(
//                "id-pelapor" to idPelapor.toRequestBody(),
//                "nama-pelapor" to namaPelapor.toRequestBody(),
//                "waktu-kejadian" to waktuKejadian.toRequestBody(),
//                "jenis" to jenisKecelakaan.toRequestBody(),
//                "lokasi-kejadian" to lokasiKejadian.toRequestBody(),
//                "notelp" to notelp.toRequestBody()
//            )


            userService.createReporting(
                idPelapor.toRequestBody(
                    MultipartBody.FORM
                ),
                namaPelapor.toRequestBody(MultipartBody.FORM),
                waktuKejadian.toRequestBody(MultipartBody.FORM),
                jenisKecelakaan.toRequestBody(MultipartBody.FORM),
                lokasiKejadian.toRequestBody(MultipartBody.FORM),
                notelp.toRequestBody(MultipartBody.FORM),
                filePart
            ).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {

                    } else {
                        val result = Gson().fromJson(
                            response.errorBody()?.string(),
                            ErrorResponse::class.java
                        )
                        Toast.makeText(
                            this@MainActivity,
                            "Message: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Timber.e(t.message)
                }

            })
        }
    }

    private fun checkAllPermission() {
        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this@MainActivity,
                    "Permissions not granted by the user",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun kmpSearch(pattern: String, text: String): List<Int> {
        val result = mutableListOf<Int>()
        val textLength = text.length
        val patternLength = pattern.length
        val lps = IntArray(patternLength)

        // membuat longest prefix suffix
        lpsArray(pattern, patternLength, lps)

        var i = 0
        var j = 0

        while (i < textLength) {
            if (text[i] == pattern[j]) {
                i += 1
                j += 1
            } else {
                if (j != 0) {
                    j = lps[j - 1]
                } else {
                    i += 1
                }
            }

            if (j == patternLength) {
//            println("Found pattern at i($i) and j($j) index ${(i - j)}");
                val indexFound = i - j
                result.add(indexFound)
                j = lps[j - 1];
            }
        }

        return result
    }

    private fun lpsArray(pattern: String, patternLength: Int, lps: IntArray) {
        var len = 0
        var i = 1

        while (i < patternLength) {
            if (pattern[i] == pattern[len]) {
                lps[i] = len + 1
                len += 1
                i += 1
            } else {
                if (len != 0) {
                    len = lps[len - 1]
                } else {
                    lps[i] = len
                    i++
                }
            }
        }
    }

    companion object {

        /* PATTERN */
        private const val PATTERN_LOCATION = "Depok"

        /* INPUT NAME */
        private const val INPUT_ID_PELAPOR = "ID Pelapor"
        private const val INPUT_NAMA_PELAPOR = "Nama Pelapor"
        private const val INPUT_SPINNER_LOKASI_KEJADIAN = "Lokasi Kejadian"
        private const val INPUT_SPINNER_JENIS_KECELAKAAN = "Jenis Kecelakaan"
        private const val INPUT_WAKTU_KEJADIAN = "Waktu Kejadian"
        private const val INPUT_NOTELP_PELAPOR = "Nomor Telepon"

        /* PERMISSIONS */
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    }
}