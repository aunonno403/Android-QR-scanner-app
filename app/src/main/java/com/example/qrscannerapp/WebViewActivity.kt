package com.example.qrscannerapp

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Patterns
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.view.View
import android.webkit.WebResourceResponse

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var originalContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)
        val url = intent.getStringExtra("url")
        originalContent = url

        if (url.isNullOrBlank() || !isValidHttpUrl(url)) {
            // Not a valid URL, show dialog instead of trying to load
            showContentDialog(url ?: getString(R.string.no_qr_detected))
            return
        }

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                // Only handle main-frame errors. Subresource errors (images/scripts)
                // should not trigger the dialog.
                if (request.isForMainFrame) {
                    // Stop loading and hide the webview before showing dialog
                    try { view.stopLoading() } catch (_: Exception) { /* ignore */ }
                    view.visibility = View.GONE
                    showContentDialog(originalContent ?: getString(R.string.failed_to_scan))
                }
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                // Handle HTTP errors (e.g., 404) for the main frame only
                if (request.isForMainFrame) {
                    try { view.stopLoading() } catch (_: Exception) { /* ignore */ }
                    view.visibility = View.GONE
                    showContentDialog(originalContent ?: getString(R.string.failed_to_scan))
                }
            }
        }
        webView.loadUrl(url)
    }

    private fun isValidHttpUrl(value: String): Boolean {
        val lower = value.lowercase()
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) return false
        return Patterns.WEB_URL.matcher(value).matches()
    }

    private fun showContentDialog(content: String) {
        // Make sure the webview isn't visible behind the dialog
        if (this::webView.isInitialized) {
            try { webView.stopLoading() } catch (_: Exception) { /* ignore */ }
            webView.visibility = View.GONE
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.scanned_content_title))
            .setMessage(content)
            .setPositiveButton(getString(R.string.copy)) { d, _ ->
                copyToClipboard(content)
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
                d.dismiss()
                finish()
            }
            .setNeutralButton(getString(R.string.share)) { d, _ ->
                shareText(content)
                d.dismiss()
                finish()
            }
            .setNegativeButton(getString(R.string.close)) { d, _ ->
                d.dismiss()
                finish()
            }
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
}