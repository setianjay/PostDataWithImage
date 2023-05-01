package com.setianjay.postdataandimageexample.helper.view

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.setianjay.postdataandimageexample.util.DateUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class ImageHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val resolver = context.contentResolver
    private var outputStream: OutputStream? = null
    private val storageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        "Testing"
    )
    private val imageDisplayName get() = "IMG_${DateUtil.dateWithFormat("yyyyMMdd_HHmmss")}.jpg"

    /**
     * get the storage directory in the form of a URI, storage location at "DCIM/Testing/"
     *
     * @return [Uri]
     * */
    private fun getStorageDirUri(): Uri? {
        val images = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }


        val contentValues = ContentValues().also {
            it.put(MediaStore.Images.Media.DISPLAY_NAME, imageDisplayName)
            it.put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                it.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    STORAGE_DIRECTORY
                )
            else
                it.put(MediaStore.Images.Media.DATA, storageDir.absolutePath)

        }

        return resolver.insert(images, contentValues)
    }

    /**
     * save image to gallery, save location at "DCIM/Testing/IMAGE_DISPLAY_NAME.jpg".
     *
     * @return [File]
     * */
    fun saveImageToGallery(): File {
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }

        val pathFile = File(storageDir, imageDisplayName)

        val storageUri = getStorageDirUri()
        try {
            outputStream = storageUri?.let {
                resolver.openOutputStream(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.let {
                it.close()
                null
            }
        }

        return pathFile
    }

    /**
     * convert URI to Bitmap.
     *
     * @param uri [Uri] URI of image
     * @return [Bitmap] or Null
     * */
    fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream: InputStream? = resolver.openInputStream(uri)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return BitmapFactory.decodeStream(inputStream, null, options)
    }

    /**
     * compress size of image.
     *
     * @param filePath file path location of image
     * @param targetMB target MB expected
     * */
    fun compressImage(filePath: String, targetMB: Double = 1.0) {
        var image: Bitmap = BitmapFactory.decodeFile(filePath)

        val exif = ExifInterface(filePath)
        val exifOrientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val exifDegrees = exifOrientationToDegrees(exifOrientation)

        image = rotateImage(image, exifDegrees.toFloat())

        try {
            val file = File(filePath)
            val length = file.length()

            val fileSizeInKB = (length / 1024).toString().toDouble()
            val fileSizeInMB = (fileSizeInKB / 1024).toString().toDouble()

            var quality = 100

            if (fileSizeInMB > targetMB) {
                quality = ((targetMB / fileSizeInMB) * 100).toInt()
            }

            val fileOutputStream = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

//    private fun saveImageToGalleryExpected(bitmap: Bitmap) {
//        val dir = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
//            "Testing"
//        )
//
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//
//        val imageFile = File(dir, imageDisplayName)
//        var fileOutputStream: FileOutputStream? = null
//
//        try {
//            fileOutputStream = FileOutputStream(imageFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//            fileOutputStream.flush()
//        } finally {
//            fileOutputStream?.close()
//        }
//    }


    companion object {
        private const val MIME_TYPE = "images/*"
        private val STORAGE_DIRECTORY = Environment.DIRECTORY_DCIM + "/Testing"
    }
}