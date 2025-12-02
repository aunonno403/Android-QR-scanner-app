package com.example.qrscannerapp

import android.annotation.SuppressLint
import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import android.widget.Toast
import com.example.qrscannerapp.repository.ScanHistoryRepository

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

    // Firebase repository for scan history
    private val scanHistoryRepository = ScanHistoryRepository()

    // Debounce mechanism to prevent duplicate scans
    private var lastSavedQrCode: String? = null
    private var lastSavedTimestamp: Long = 0
    private val SCAN_DEBOUNCE_INTERVAL = 5000L // 5 seconds - adjust as needed
    private val RESCAN_CONFIRMATION_THRESHOLD = 10000L // 10 seconds - show popup after this
    private var rescanDialogShown: Boolean = false
    private var firstScanTimestamp: Long = 0

    private lateinit var offlineBanner: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppMode.load(this) // load persisted mode before UI decisions
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "QR Scanner"

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.resultTextView)
        scannedImageView = findViewById(R.id.scannedImageView)
        flashButton = findViewById(R.id.flashButton)
        offlineBanner = findViewById(R.id.offlineBanner)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        // Flash button click listener
        flashButton.setOnClickListener {
            toggleFlash()
        }

        // History button click listener
        val historyButton = findViewById<ImageButton>(R.id.historyButton)
        historyButton.setOnClickListener {
            val intent = Intent(this, ScanHistoryActivity::class.java)
            startActivity(intent)
        }

        // Profile button click listener
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val galleryButton = findViewById<ImageButton>(R.id.imageButton2)
        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        // QR generator button click listener (null-safe)
        (findViewById<View>(R.id.qrGeneratorButton) as? ImageButton)?.setOnClickListener {
            startActivity(Intent(this, QrGeneratorActivity::class.java))
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

        promptForModeIfNeeded()
        applyModeUi()
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

        // If we detect a different QR code, reset the debounce tracking
        // This allows the same code to be saved again after scanning a different code
        if (rawValue != lastSavedQrCode) {
            Log.d("MainActivity", "New QR code detected, resetting debounce: $rawValue")
            lastSavedQrCode = null
            lastSavedTimestamp = 0
            rescanDialogShown = false
            firstScanTimestamp = System.currentTimeMillis()
        }

        // Require explicit http/https scheme for WebView; if missing but looks like domain, we can prepend.
        val urlCandidate = buildHttpUrlIfNeeded(rawValue)
        val isHttpUrl = urlCandidate != null && isValidHttpUrl(urlCandidate)
        resultTextView.text = rawValue

        // Determine scan type and save to Firebase
        val scanType = determineScanType(rawValue, isHttpUrl)
        saveScanToFirebase(rawValue, scanType)

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

    private fun determineScanType(value: String, isHttpUrl: Boolean): String {
        return when {
            isHttpUrl -> "URL"
            Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "EMAIL"
            Patterns.PHONE.matcher(value).matches() -> "PHONE"
            else -> "TEXT"
        }
    }

    private fun saveScanToFirebase(content: String, type: String) {
        if (AppMode.isOffline) {
            Log.d("MainActivity", "Offline mode: scan not saved to Firebase")
            return
        }
        val currentTime = System.currentTimeMillis()

        // Check if this is a duplicate scan within the debounce interval
        if (content == lastSavedQrCode && (currentTime - lastSavedTimestamp) < SCAN_DEBOUNCE_INTERVAL) {
            Log.d("MainActivity", "Duplicate scan detected, skipping save: $content")

            // Check if user has been scanning the same code for more than 10 seconds
            if (!rescanDialogShown && (currentTime - firstScanTimestamp) > RESCAN_CONFIRMATION_THRESHOLD) {
                rescanDialogShown = true
                showRescanConfirmationDialog(content, type)
            }
            return
        }

        // Update tracking variables
        lastSavedQrCode = content
        lastSavedTimestamp = currentTime

        // Set first scan timestamp if this is a new code
        if (firstScanTimestamp == 0L) {
            firstScanTimestamp = currentTime
        }

        scanHistoryRepository.saveScan(content, type) { success, errorMessage ->
            if (!success) {
                Log.e("MainActivity", "Failed to save scan to Firebase: $errorMessage")
                // Optionally show error to user
                // Toast.makeText(this, "Failed to save: $errorMessage", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("MainActivity", "Scan saved successfully to Firebase: $content")
            }
        }
    }

    private fun showRescanConfirmationDialog(content: String, type: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Continue Scanning?")
                .setMessage("You've been scanning the same QR code for a while. Do you want to scan it again?")
                .setPositiveButton("Yes, Scan Again") { dialog, _ ->
                    // Reset tracking to allow immediate rescan
                    lastSavedQrCode = null
                    lastSavedTimestamp = 0
                    firstScanTimestamp = System.currentTimeMillis()
                    rescanDialogShown = false

                    // Save the scan immediately
                    saveScanToFirebase(content, type)

                    Toast.makeText(this, "QR code scanned again", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("No, Keep Current") { dialog, _ ->
                    // Reset the dialog shown flag after a delay to allow showing it again later
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        rescanDialogShown = false
                    }, RESCAN_CONFIRMATION_THRESHOLD)

                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
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
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
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

    private fun promptForModeIfNeeded() {
        // Only show dialog if first launch or user explicitly wants to change; here we show if neither offline nor authenticated online
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val isLoggedIn = auth.currentUser != null
        if (!AppMode.isOffline && isLoggedIn) return // already online and logged in
        if (AppMode.isOffline) return // offline mode previously chosen
        AlertDialog.Builder(this)
            .setTitle("Select Mode")
            .setMessage("Choose how you want to use the app:")
            .setPositiveButton("Online Mode") { d, _ ->
                AppMode.isOffline = false
                AppMode.save(this)
                // Require login if not authenticated
                if (auth.currentUser == null) {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    applyModeUi()
                }
                d.dismiss()
            }
            .setNegativeButton("Offline Mode") { d, _ ->
                AppMode.isOffline = true
                AppMode.save(this)
                applyModeUi()
                Toast.makeText(this, "Offline mode enabled", Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun applyModeUi() {
        val historyButton = findViewById<ImageButton>(R.id.historyButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        if (AppMode.isOffline) {
            historyButton.visibility = View.GONE
            profileButton.visibility = View.GONE
            offlineBanner.visibility = View.VISIBLE
        } else {
            offlineBanner.visibility = View.GONE
            // Show only if user is logged in
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val loggedIn = auth.currentUser != null
            historyButton.visibility = if (loggedIn) View.VISIBLE else View.GONE
            profileButton.visibility = if (loggedIn) View.VISIBLE else View.GONE
            if (!loggedIn) {
                resultTextView.text = "Login required for online features. Please sign in." // fallback
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_mode -> {
                showChangeModeDialog()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showChangeModeDialog() {
        val currentModeText = if (AppMode.isOffline) "Offline" else "Online"
        AlertDialog.Builder(this)
            .setTitle("Change Mode")
            .setMessage("Current mode: $currentModeText\n\nSelect a new mode:")
            .setPositiveButton("Online Mode") { d, _ ->
                if (AppMode.isOffline) {
                    // Switching from offline to online
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        // Need to login
                        AppMode.isOffline = false
                        AppMode.save(this)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        // Already logged in
                        AppMode.isOffline = false
                        AppMode.save(this)
                        applyModeUi()
                        Toast.makeText(this, "Switched to Online mode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Already in Online mode", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("Offline Mode") { d, _ ->
                if (!AppMode.isOffline) {
                    // Switching from online to offline
                    AppMode.isOffline = true
                    AppMode.save(this)
                    applyModeUi()
                    Toast.makeText(this, "Switched to Offline mode", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Already in Offline mode", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNeutralButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About QR Scanner")
            .setMessage("QR Scanner App\nVersion 1.0\n\nFeatures:\n• Scan QR codes\n• Generate QR codes\n• Offline mode support\n• Cloud sync (Online mode)\n• Scan history\n• User profiles")
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .show()
    }
}