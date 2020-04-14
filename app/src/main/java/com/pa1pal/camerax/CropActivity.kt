package com.pa1pal.camerax

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pa1pal.camerax.CameraActivity.Companion.EXTRA_URI
import kotlinx.android.synthetic.main.activity_image_crop.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class CropActivity : AppCompatActivity() {

    lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        uri = intent.getParcelableExtra(EXTRA_URI)
        cropImageView.setImageUriAsync(uri)
    }

    fun complete(v: View) {
        val croppedImage = cropImageView.croppedImage
        bitmapToFile(croppedImage, File(cacheDir.absolutePath, "cropped.jpg"))
        Toast.makeText(this, "Crop Complete", Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    private fun bitmapToFile(bitmap: Bitmap, path: File) {

        val mediaStorageDir = File(
            getExternalFilesDir(null),
            "MarsPlay"
        )

        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MarsPlay", "failed to create directory")
                }
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun rotateLeft(v: View) {
        cropImageView.rotateImage(-90)
    }

    fun rotateRight(v: View) {
        cropImageView.rotateImage(90)
    }
}