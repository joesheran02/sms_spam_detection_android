package com.example.sms_spam_detection.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.sms_spam_detection.R

class PermissionsSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var smsPermissionSwitch: SwitchPreferenceCompat

    // Define the ActivityResultLauncher for permission requests
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Update the switch based on permission result
            smsPermissionSwitch.isChecked = isGranted
        }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.permission_section, rootKey)

        smsPermissionSwitch = findPreference("sms_permission")!!

        // Update the switch based on SMS permission status
        updateSmsPermissionSwitch()

        // Add a listener to handle permission changes
        smsPermissionSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                // Request SMS permissions
                requestSmsPermission()
            } else {
                openAppSettings()
                updateSmsPermissionSwitch()
            }
            false // Prevent the switch from automatically changing state
        }
    }

    // Check if SMS permission is granted and update the switch accordingly
    private fun updateSmsPermissionSwitch() {
        smsPermissionSwitch.isChecked = isPermissionsGranted()
    }

    private fun isPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request the SMS permission using the ActivityResultLauncher
    private fun requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
    }

    private fun openAppSettings() {
        // Create an Intent to open the app's settings
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
