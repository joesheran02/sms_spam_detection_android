package com.example.sms_spam_detection.ui.notifications

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sms_spam_detection.MyNotification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text*/


    private val _notifications = MutableLiveData<List<MyNotification>>()

    init {
        // Load notifications from persistent storage on ViewModel creation
        _notifications.value = loadNotifications(application.applicationContext)
        Log.d("notifications value line 27", _notifications.value.toString())
    }



    // Method to update the notifications list
    fun updateNotifications(newNotifications: List<MyNotification>) {
        _notifications.value = newNotifications
        saveNotifications(getApplication(), newNotifications)
        Log.d("Saving", "Are you sure")
    }

    val notifications: LiveData<List<MyNotification>> get() = _notifications

    private fun loadNotifications(context: Context): List<MyNotification> {
        val sharedPreferences = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
        val notificationJson = sharedPreferences.getString("notifications", null)
        return if (notificationJson != null) {
            val type = object : TypeToken<List<MyNotification>>() {}.type
            Gson().fromJson(notificationJson, type)
        }
        else {
            emptyList()
        }
    }

    private fun saveNotifications(context: Context, notifications: List<MyNotification>) {
        val sharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val updatedNotificationsJson = Gson().toJson(notifications)
        sharedPreferences.edit().putString("notifications", updatedNotificationsJson).apply()
        Log.d("SharedPreferences", "Updated Notifications: ${sharedPreferences.getString("notifications", null)}")
    }
}