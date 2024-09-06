package com.example.sms_spam_detection.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.HistoryInsight
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.TimeUtils




class HistoryInsightsAdapter(private val historyInsights: List<HistoryInsight>) :
    RecyclerView.Adapter<HistoryInsightsAdapter.ViewHolder>(), BaseAdapter {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val insight: TextView = view.findViewById(R.id.phoneNumberTextView)
        val lastTimeScanned: TextView = view.findViewById(R.id.ratingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suspicious_number_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val insightHistory = historyInsights[position]
        holder.insight.text = insightHistory.insight
        holder.lastTimeScanned.text = TimeUtils.getTimeAgo(insightHistory.lastTimeScanned)
    }

    override fun getItemCount(): Int {
        return historyInsights.size
    }
}