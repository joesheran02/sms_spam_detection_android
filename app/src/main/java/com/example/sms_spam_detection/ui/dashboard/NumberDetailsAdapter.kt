package com.example.sms_spam_detection.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.Message
import com.example.sms_spam_detection.MessageParcel
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.TimeUtils

class NumberDetailsAdapter(private val messages: List<MessageParcel>) :
    RecyclerView.Adapter<NumberDetailsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.message)
        val messageIndex: TextView = view.findViewById(R.id.messageIndex)
        val isSpam: TextView = view.findViewById(R.id.IsSpam)
        val spamProb: TextView = view.findViewById(R.id.SpamProb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.number_details_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val insightHistory = messages[position]
        holder.message.text = "Message: ${insightHistory.message}"
        holder.messageIndex.text = insightHistory.index.toString()
        holder.isSpam.text = "Is Spam: ${(insightHistory.rating >= 0.5).toString()}"
        holder.spamProb.text = "Spam Probability: ${insightHistory.rating.toString()}"
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}