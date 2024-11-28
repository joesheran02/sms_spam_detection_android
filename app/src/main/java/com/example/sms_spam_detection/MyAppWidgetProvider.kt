package com.example.sms_spam_detection

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        appWidgetIds?.forEach { widgetId ->
            val notificationsJson = context?.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                ?.getString("notifications", null)
            val notificationsList: List<MyNotification> = if (notificationsJson != null) {
                Gson().fromJson(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type)
            } else {
                listOf(MyNotification(
                    message = "N/A",
                    sender = "N/A",
                    spamProb = 0.0F,
                    isSpam = true,
                    timestamp = System.currentTimeMillis()
                ))
            }

            val notification = notificationsList.last()

            // Update the widget layout
            val views = RemoteViews(context?.packageName, R.layout.widget_layout)

            views.setTextViewText(R.id.notificationSenderWidget, "Sender: ${notification.sender}")
            views.setTextViewText(R.id.notificationTimestampWidget, TimeUtils.getTimeAgo(notification.timestamp))
            views.setTextViewText(R.id.notificationMessageWidget, "Message: ${notification.message}")
            views.setTextViewText(R.id.notificationSpamProbWidget, "Spam Probability: ${notification.spamProb}")

            // Update the widget
            appWidgetManager?.updateAppWidget(widgetId, views)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        // Get the minimum height of the widget
        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        // Height of a single list item in dp (e.g., 48dp including padding)
        val itemHeightDp = 48

        // Convert dp to pixels
        val density = context.resources.displayMetrics.density
        val itemHeightPx = (itemHeightDp * density).toInt()

        // Calculate the number of items that can fit
        val itemsFitMinHeight = (minHeight / itemHeightPx) + 1

        val intent = Intent(context, ListViewService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("EXTRA_ITEMS_TO_SHOW", itemsFitMinHeight)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }

        val views = RemoteViews(context.packageName, R.layout.widget_list).apply {
            setRemoteAdapter(R.id.widgetListView, intent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.counter.UPDATE_NOTIFICATION") {
            val notificationsJson = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                    ?.getString("notifications", null)
                val notificationsList: List<MyNotification> = if (notificationsJson != null) {
                    Gson().fromJson(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type)
                } else {
                    emptyList()
                }
            val notification = notificationsList.last()

            // Update the widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widget = ComponentName(context, MyAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widget)
            for (appWidgetId in appWidgetIds) {
                // Update the widget layout
                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                views.setTextViewText(R.id.notificationSenderWidget, "Sender: ${notification.sender}")
                views.setTextViewText(R.id.notificationTimestampWidget, TimeUtils.getTimeAgo(notification.timestamp))
                views.setTextViewText(R.id.notificationMessageWidget, "Message: ${notification.message}")
                views.setTextViewText(R.id.notificationSpamProbWidget, "Spam Probability: ${notification.spamProb}")
                appWidgetManager.updateAppWidget(appWidgetId, views)

            }
        }

    }
}
