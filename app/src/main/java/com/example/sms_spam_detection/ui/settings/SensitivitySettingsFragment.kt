package com.example.sms_spam_detection.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.sms_spam_detection.R

class SensitivitySettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sensitivity_section, rootKey)
    }
}