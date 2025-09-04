package com.example.qrscannerapp

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.get
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.core.graphics.scale


class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var scannedImageView: ImageView
    private var isShowingScannedImage = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.resultTextView)
        scannedImageView = findViewById(R.id.scannedImageView)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()


        val galleryButton = findViewById<ImageButton>(R.id.imageButton2)
        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val uri = data.data
                if (uri != null) {
                    // Show image in ImageView, hide PreviewView
                    val inputStream = contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    scannedImageView.post {
                        val scaledBitmap = originalBitmap.scale(
                            scannedImageView.width.coerceAtLeast(1),
                            scannedImageView.height.coerceAtLeast(1)
                        )
                        scannedImageView.setImageBitmap(scaledBitmap)
                        scannedImageView.visibility = View.VISIBLE
                        previewView.visibility = View.GONE
                        isShowingScannedImage = true
                    }
                    scannedImageView.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                    isShowingScannedImage = true

                    val image = InputImage.fromFilePath(this, uri)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                handleBarcode(barcode)
                            }
                        }
                        .addOnFailureListener {
                            resultTextView.text = "Failed to scan"
                        }
                }
            }
        }

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingScannedImage) {
                    scannedImageView.visibility = View.GONE
                    previewView.visibility = View.VISIBLE
                    isShowingScannedImage = false
                } else {
                    finish()
                }
            }
        })


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
                        processImageProxy(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(this))

    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // Image processing logic here
        val mediaImage= imageProxy.image
        if(mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                       handleBarcode(barcode)
                    }
                }
                .addOnFailureListener {
                    resultTextView.text ="failed to scan"
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }

        }

    }

    private fun handleBarcode(barcode: Barcode){
        val url = barcode.url?.url ?: barcode.displayValue
        if(url != null) {
            resultTextView.text = url
            resultTextView.setOnClickListener {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }

        }  else {
            resultTextView.text = "No QR code detected"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}