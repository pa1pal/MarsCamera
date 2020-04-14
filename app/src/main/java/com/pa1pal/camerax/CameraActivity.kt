package com.pa1pal.camerax

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.lang.Exception
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider : ListenableFuture<ProcessCameraProvider>
    private val executor = Executors.newSingleThreadExecutor()
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    companion object {
        const val EXTRA_URI = "EXTRA_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
    }

    private fun askPermissionIfApplicable() {
        for (item in permissions) {
            val permission = ActivityCompat.checkSelfPermission(this, item)
            if (permission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, permissions, 101)
                return
            }
        }
        startCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            var count = 0
            for (results in grantResults) {
                if (results == PackageManager.PERMISSION_DENIED) {
                    count++
                }
            }
            if (count == 0) {
                startCamera()
            } else {
                showMessage(getString(R.string.permission_required_placeholder))
                finish()
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(this)
        cameraProvider.addListener(Runnable {
            val cameraProvider = cameraProvider.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
            preview.setSurfaceProvider(previewView.previewSurfaceProvider)

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            cameraProvider.unbindAll()

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exception: Exception) {
                Log.e("TAG", "Use case binding failed", exception)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun capture(v: View) {
        val savedPath = File(cacheDir.absolutePath, "temp.jpg")
        val imageSavedCallback = object: ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                previewView.post {
                    if (outputFileResults.savedUri == null) {
                    }
                    Toast.makeText(this@CameraActivity, "Image save success!!", Toast.LENGTH_SHORT).show()
                    Intent(this@CameraActivity, CropActivity::class.java).apply {
                            putExtra(EXTRA_URI, Uri.fromFile(savedPath))
                        }.run {
                            startActivity(this)
                        }
                }
            }

            override fun onError(exception: ImageCaptureException) {
            }
        }

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(savedPath).build()
        imageCapture.takePicture(outputFileOptions, executor, imageSavedCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!isCameraAvailable(this)) {
            showMessage(getString(R.string.no_camera_found))
            return
        }
        askPermissionIfApplicable()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    private fun isCameraAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}
