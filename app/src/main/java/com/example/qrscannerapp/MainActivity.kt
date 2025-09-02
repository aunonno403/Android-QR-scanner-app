package com.example.qrscannerapp

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.resultTextView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if(isGranted) {
                startCamera()
            } else {
                resultTextView.text = "Camera permission denied"
            }

        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val screenSize = Size(1280, 720) // Example size, adjust as needed
        val resolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE)
        ).build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        //processimageproxy
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(this))

    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        // Image processing logic here
        imageProxy.close()
    }
}