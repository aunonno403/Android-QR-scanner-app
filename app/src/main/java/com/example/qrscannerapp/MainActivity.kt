package com.example.qrscannerapp

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.util.Size
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var scannedImageView: ImageView
    private var isShowingScannedImage = false

    private var camera: Camera? = null
    private var isFlashOn = false
    private lateinit var flashButton: ImageButton

    // Keep last scanned raw value for dialog actions
    private var lastScannedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.resultTextView)
        scannedImageView = findViewById(R.id.scannedImageView)
        flashButton = findViewById(R.id.flashButton)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        // Flash button click listener
        flashButton.setOnClickListener {
            toggleFlash()
        }


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
                    val inputStream = contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val image = InputImage.fromFilePath(this, uri)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            var found = false
                            for (barcode in barcodes) {
                                handleBarcode(barcode)
                                found = true
                            }
                            if (!found) {
                                resultTextView.text = getString(R.string.no_qr_detected)
                            }
                            // Ensure UI update is on main thread and after scan
                            scannedImageView.post {
                                val width = scannedImageView.width
                                val height = scannedImageView.height
                                val scaledBitmap = if (width > 0 && height > 0) {
                                    originalBitmap.scale(width, height)
                                } else originalBitmap
                                scannedImageView.setImageBitmap(scaledBitmap)
                                scannedImageView.visibility = View.VISIBLE
                                previewView.visibility = View.GONE
                                isShowingScannedImage = true
                            }
                        }
                        .addOnFailureListener { resultTextView.text = getString(R.string.failed_to_scan) }
                }
            }
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) startCamera() else resultTextView.text = getString(R.string.camera_permission_denied)
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingScannedImage) {
                    scannedImageView.visibility = View.GONE
                    previewView.visibility = View.VISIBLE
                    isShowingScannedImage = false
                } else finish()
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
                .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy -> processImageProxy(imageProxy) }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(this))

    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // Image processing logic here
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes -> barcodes.forEach { handleBarcode(it) } }
                .addOnFailureListener { resultTextView.text = getString(R.string.failed_to_scan) }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        // Prefer structured URL field, fallback to displayValue
        val structuredUrl = barcode.url?.url
        val displayValue = barcode.displayValue
        val rawValue = structuredUrl ?: displayValue
        lastScannedValue = rawValue

        if (rawValue == null) {
            resultTextView.text = getString(R.string.no_qr_detected)
            resultTextView.setOnClickListener(null)
            return
        }

        // Require explicit http/https scheme for WebView; if missing but looks like domain, we can prepend.
        val urlCandidate = buildHttpUrlIfNeeded(rawValue)
        val isHttpUrl = urlCandidate != null && isValidHttpUrl(urlCandidate)
        resultTextView.text = rawValue

        if (isHttpUrl) {
            // URL case: clicking opens WebViewActivity
            resultTextView.setOnClickListener {
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    putExtra("url", urlCandidate)
                }
                startActivity(intent)
            }
        } else {
            // Non-URL case: show dialog with options
            resultTextView.setOnClickListener { showNonUrlDialog(rawValue) }
        }
    }

    private fun buildHttpUrlIfNeeded(value: String): String? {
        val trimmed = value.trim()
        val lower = trimmed.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")) return trimmed
        // If matches WEB_URL but missing scheme, prepend http://
        return if (Patterns.WEB_URL.matcher(trimmed).matches()) "http://$trimmed" else null
    }

    private fun isValidHttpUrl(value: String): Boolean {
        val lower = value.lowercase()
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) return false
        return Patterns.WEB_URL.matcher(value).matches()
    }

    private fun showNonUrlDialog(content: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.scanned_content_title))
            .setMessage(content)
            .setPositiveButton(getString(R.string.copy)) { d, _ ->
                copyToClipboard(content)
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNeutralButton(getString(R.string.share)) { d, _ ->
                shareText(content)
                d.dismiss()
            }
            .setNegativeButton(getString(R.string.close)) { d, _ -> d.dismiss() }
            .show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.qr_content_label), text))
    }

    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qr_content)))
    }

    private fun toggleFlash() {
        camera?.let { cam ->
            val info = cam.cameraInfo
            val control = cam.cameraControl
            if (info.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                control.enableTorch(isFlashOn)

                // Update button appearance based on flash state
                if (isFlashOn) {
                    flashButton.setImageResource(android.R.drawable.ic_menu_day)
                    flashButton.alpha = 1.0f
                } else {
                    flashButton.setImageResource(android.R.drawable.ic_menu_day)
                    flashButton.alpha = 0.5f
                }
            } else {
                resultTextView.text = getString(R.string.flash_not_available)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}