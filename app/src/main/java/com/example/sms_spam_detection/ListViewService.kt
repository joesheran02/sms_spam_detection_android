package com.example.sms_spam_detection

import android.content.Intent
import android.widget.RemoteViewsService

class ListViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListViewFactory(applicationContext, intent)
    }
}