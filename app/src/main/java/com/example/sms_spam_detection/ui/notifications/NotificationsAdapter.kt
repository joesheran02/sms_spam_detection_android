package com.example.sms_spam_detection.ui.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.MyNotification
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.TimeUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsAdapter(private val notifications: List<MyNotification>) :

    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {


    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
        val notificationSender: TextView = itemView.findViewById(R.id.notificationSender)
        val notificationSpamProb: TextView = itemView.findViewById(R.id.notificationSpamProb)
        val notificationIsSpam: TextView = itemView.findViewById(R.id.notificationIsSpam)
        val notificationTimestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.notification_item,
            parent,
            false
        )
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications.asReversed()[position]
        holder.notificationSender.text = "Sender: ${notification.sender}"
        holder.notificationMessage.text = "Message: ${notification.message}"
        holder.notificationSpamProb.text = "Spam Probability: ${notification.spamProb}"
        holder.notificationIsSpam.text = "Is Spam: ${notification.isSpam}"
        holder.notificationTimestamp.text = TimeUtils.getTimeAgo(notification.timestamp)

        // Bind other UI elements if needed
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}
