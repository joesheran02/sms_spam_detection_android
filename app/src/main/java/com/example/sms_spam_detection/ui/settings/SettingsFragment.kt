package com.example.sms_spam_detection.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.example.sms_spam_detection.R


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<PreferenceScreen>("manage_permissions")?.setOnPreferenceClickListener {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    androidx.transition.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.transition.R.anim.abc_shrink_fade_out_from_bottom,
                    androidx.transition.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.transition.R.anim.abc_shrink_fade_out_from_bottom,
                )
                .replace(android.R.id.content, PermissionsSettingsFragment())
                .addToBackStack(null)
                .commit()
            true
        }

        findPreference<PreferenceScreen>("manage_sensitivity")?.setOnPreferenceClickListener {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    androidx.transition.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.transition.R.anim.abc_shrink_fade_out_from_bottom,
                    androidx.transition.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.transition.R.anim.abc_shrink_fade_out_from_bottom,
                )
                .replace(android.R.id.content, SensitivitySettingsFragment())
                .addToBackStack(null)
                .commit()
            true
        }
    }
}