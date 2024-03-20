package com.example.sms_spam_detection.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text*/

    private val _isSmsPermissionGranted = MutableLiveData<Boolean>()

    // Initialize the LiveData with the default value (e.g., false)
    init {
        _isSmsPermissionGranted.value = isSmsPermissionGranted()
        Log.d("Initialised Permissions", isSmsPermissionGranted().toString())
    }

    val isSmsPermissionGranted: LiveData<Boolean> = _isSmsPermissionGranted

    // Function to update the LiveData when permissions are granted
    fun updateSmsPermissionStatus(isGranted: Boolean) {
        _isSmsPermissionGranted.value = isGranted
    }

    // Function to check if SMS permissions are granted
     private fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            // Use the application context to avoid leaks
            getApplication(),
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}