package com.example.qrscannerapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscannerapp.adapter.ScanHistoryAdapter
import com.example.qrscannerapp.models.ScanHistory
import com.example.qrscannerapp.repository.ScanHistoryRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ScanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipScanned: Chip
    private lateinit var chipGenerated: Chip
    private lateinit var adapter: ScanHistoryAdapter
    private val repository = ScanHistoryRepository()
    private var allScans: List<ScanHistory> = emptyList()

    enum class FilterType {
        ALL, SCANNED, GENERATED
    }

    private var currentFilter: FilterType = FilterType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        chipAll = findViewById(R.id.chipAll)
        chipScanned = findViewById(R.id.chipScanned)
        chipGenerated = findViewById(R.id.chipGenerated)

        setupRecyclerView()
        setupFilterChips()
        loadScanHistory()
    }

    private fun setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            currentFilter = when (checkedIds[0]) {
                R.id.chipAll -> FilterType.ALL
                R.id.chipScanned -> FilterType.SCANNED
                R.id.chipGenerated -> FilterType.GENERATED
                else -> FilterType.ALL
            }

            applyFilter()
        }
    }

    private fun setupRecyclerView() {
        adapter = ScanHistoryAdapter(
            scans = mutableListOf(),
            onCopyClick = { scan -> copyToClipboard(scan.content) },
            onShareClick = { scan -> shareText(scan.content) },
            onDeleteClick = { scan -> showDeleteConfirmation(scan) },
            onItemClick = { scan -> handleScanClick(scan) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadScanHistory() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyTextView.visibility = View.GONE

        repository.getUserScans { scans, error ->
            progressBar.visibility = View.GONE

            if (error != null) {
                Toast.makeText(this, "Error loading history: $error", Toast.LENGTH_SHORT).show()
                emptyTextView.visibility = View.VISIBLE
                emptyTextView.text = "Error loading scan history"
                return@getUserScans
            }

            allScans = scans ?: emptyList()
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filteredScans = when (currentFilter) {
            FilterType.ALL -> allScans
            FilterType.SCANNED -> allScans.filter { it.type != "GENERATED" }
            FilterType.GENERATED -> allScans.filter { it.type == "GENERATED" }
        }

        if (filteredScans.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyTextView.text = when (currentFilter) {
                FilterType.ALL -> "No scan history yet.\nStart scanning QR codes!"
                FilterType.SCANNED -> "No scanned QR codes yet.\nScan a QR code to see it here!"
                FilterType.GENERATED -> "No generated QR codes yet.\nGenerate a QR code to see it here!"
            }
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateScans(filteredScans)
        }
    }

    private fun handleScanClick(scan: ScanHistory) {
        when (scan.type) {
            "URL" -> {
                val urlToLoad = if (scan.content.startsWith("http://") || scan.content.startsWith("https://")) {
                    scan.content
                } else {
                    "http://${scan.content}"
                }

                if (Patterns.WEB_URL.matcher(urlToLoad).matches()) {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("url", urlToLoad)
                    startActivity(intent)
                } else {
                    showContentDialog(scan.content)
                }
            }
            else -> {
                showContentDialog(scan.content)
            }
        }
    }

    private fun showContentDialog(content: String) {
        AlertDialog.Builder(this)
            .setTitle("Scanned Content")
            .setMessage(content)
            .setPositiveButton("Copy") { d, _ ->
                copyToClipboard(content)
                d.dismiss()
            }
            .setNeutralButton("Share") { d, _ ->
                shareText(content)
                d.dismiss()
            }
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .show()
    }

    private fun showDeleteConfirmation(scan: ScanHistory) {
        AlertDialog.Builder(this)
            .setTitle("Delete Scan")
            .setMessage("Are you sure you want to delete this scan?")
            .setPositiveButton("Delete") { d, _ ->
                deleteScan(scan)
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun deleteScan(scan: ScanHistory) {
        repository.deleteScan(scan.id) { success, error ->
            if (success) {
                Toast.makeText(this, "Scan deleted", Toast.LENGTH_SHORT).show()

                // Remove from allScans list and reapply filter
                allScans = allScans.filter { it.id != scan.id }
                applyFilter()
            } else {
                Toast.makeText(this, "Failed to delete: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("QR Content", text))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Content"))
    }
}

