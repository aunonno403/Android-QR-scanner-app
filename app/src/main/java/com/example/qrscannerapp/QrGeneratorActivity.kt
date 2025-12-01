package com.example.qrscannerapp

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.qrscannerapp.databinding.ActivityQrGeneratorBinding
import com.example.qrscannerapp.qrgenerator.QrGenerator
import com.example.qrscannerapp.repository.ScanHistoryRepository

class QrGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrGeneratorBinding
    private var lastBitmap: Bitmap? = null
    private val repository = ScanHistoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.generateButton.setOnClickListener {
            val text = binding.inputText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bitmap = QrGenerator.generate(text)
            lastBitmap = bitmap
            binding.qrImage.setImageBitmap(bitmap)

            // Save generated QR content into scan history (cloud via Firebase)
            repository.saveScan(text, "GENERATED") { success, error ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Saved generated QR to history", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save to history: ${error ?: "unknown"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val bmp = lastBitmap ?: (binding.qrImage.drawable as? BitmapDrawable)?.bitmap
            if (bmp == null) {
                Toast.makeText(this, "Generate a QR first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uri = MediaStore.Images.Media.insertImage(contentResolver, bmp, "qr_${System.currentTimeMillis()}", "Generated QR Code")
            if (uri != null) {
                Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareButton.setOnClickListener {
            val bmp = lastBitmap ?: (binding.qrImage.drawable as? BitmapDrawable)?.bitmap
            if (bmp == null) {
                Toast.makeText(this, "Generate a QR first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val path = MediaStore.Images.Media.insertImage(contentResolver, bmp, "qr_share_${System.currentTimeMillis()}", null)
            if (path == null) {
                Toast.makeText(this, "Share failed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(path))
            }
            startActivity(android.content.Intent.createChooser(intent, "Share QR"))
        }
    }
}
