package com.example.sms_spam_detection

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListViewFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val notificationsJson = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        ?.getString("notifications", null)
    private val data: List<MyNotification> = if (notificationsJson != null) {
        Gson().fromJson<List<MyNotification>?>(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type).asReversed()
    } else {
        listOf(MyNotification(
            message = "N/A",
            sender = "N/A",
            spamProb = 0.0F,
            isSpam = true,
            timestamp = System.currentTimeMillis()
        ))
    }
    private var itemsToShow: Int = intent.extras?.getInt("EXTRA_ITEMS_TO_SHOW") ?: 1

    override fun onCreate() {
    }

    override fun getCount(): Int = minOf(data.size, itemsToShow)

    // Other methods remain the same


    override fun onDataSetChanged() {
        // Update the data source if necessary
    }

    override fun onDestroy() {
    }

    override fun getViewAt(position: Int): RemoteViews {
        val currentNotification = data[position % data.size]
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.notificationSenderWidget, "Sender: ${currentNotification.sender}")
        views.setTextViewText(R.id.notificationTimestampWidget, TimeUtils.getTimeAgo(currentNotification.timestamp))
        views.setTextViewText(R.id.notificationMessageWidget, "Message: ${currentNotification.message}")
        views.setTextViewText(R.id.notificationSpamProbWidget, "Spam Probability: ${currentNotification.spamProb}")
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}

