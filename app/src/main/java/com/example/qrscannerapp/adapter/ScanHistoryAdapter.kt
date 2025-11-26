package com.example.qrscannerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscannerapp.R
import com.example.qrscannerapp.models.ScanHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter(
    private var scans: MutableList<ScanHistory>,
    private val onCopyClick: (ScanHistory) -> Unit,
    private val onShareClick: (ScanHistory) -> Unit,
    private val onDeleteClick: (ScanHistory) -> Unit,
    private val onItemClick: (ScanHistory) -> Unit
) : RecyclerView.Adapter<ScanHistoryAdapter.ScanViewHolder>() {

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView: TextView = itemView.findViewById(R.id.scanTypeTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.scanTimestampTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.scanContentTextView)
        val copyButton: Button = itemView.findViewById(R.id.copyButton)
        val shareButton: Button = itemView.findViewById(R.id.shareButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scan = scans[position]

        holder.typeTextView.text = scan.type
        holder.contentTextView.text = scan.content
        holder.timestampTextView.text = formatTimestamp(scan.timestamp)

        holder.copyButton.setOnClickListener { onCopyClick(scan) }
        holder.shareButton.setOnClickListener { onShareClick(scan) }
        holder.deleteButton.setOnClickListener { onDeleteClick(scan) }
        holder.itemView.setOnClickListener { onItemClick(scan) }
    }

    override fun getItemCount(): Int = scans.size

    fun updateScans(newScans: List<ScanHistory>) {
        scans.clear()
        scans.addAll(newScans)
        notifyDataSetChanged()
    }

    fun removeScan(scan: ScanHistory) {
        val position = scans.indexOf(scan)
        if (position != -1) {
            scans.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now" // Less than 1 minute
            diff < 3600000 -> "${diff / 60000}m ago" // Less than 1 hour
            diff < 86400000 -> "${diff / 3600000}h ago" // Less than 1 day
            diff < 604800000 -> "${diff / 86400000}d ago" // Less than 1 week
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
}

