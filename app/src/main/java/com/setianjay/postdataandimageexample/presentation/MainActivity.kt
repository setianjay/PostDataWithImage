package com.setianjay.postdataandimageexample.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.setianjay.postdataandimageexample.R
import com.setianjay.postdataandimageexample.databinding.ActivityMainBinding
import com.setianjay.postdataandimageexample.helper.view.FormValidationHelper
import com.setianjay.postdataandimageexample.helper.view.ImageHelper
import com.setianjay.postdataandimageexample.helper.view.model.FormValidationHelperModel
import dagger.hilt.android.AndroidEntryPoint
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
 * - Mendapatkan nilai dari masing-masing form
 * - Mengirim semua nilai dari masing-masing form ke server menggunakan retrofit
 * */

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding

    /* input */
    private var photoUri: Uri? = null
    private var photoFilePath: String = ""
    private var photoBitmap: Bitmap? = null
    /*private val idPelapor = 1
    private lateinit var namaPelapor: String
    private lateinit var lokasiKejadian: String
    private var jenisKecelakaan: Int = 0
    private val tglPelaporan = DateUtil.currentDate()
    private lateinit var waktuKejadian: String
    private lateinit var notelp: String*/

    @Inject
    lateinit var formValidationHelper: FormValidationHelper

    @Inject
    lateinit var imageHelper: ImageHelper

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
            Toast.makeText(this@MainActivity, "Input aman", Toast.LENGTH_SHORT).show()
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

    companion object {

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