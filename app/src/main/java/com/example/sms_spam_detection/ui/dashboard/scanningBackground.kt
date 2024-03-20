package com.example.sms_spam_detection.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sms_spam_detection.databinding.ScanningBackgroundBinding

class ScanningBackground: Fragment() {
    private var _binding: ScanningBackgroundBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScanningBackgroundBinding.inflate(inflater, container, false)
        return binding.root
    }

}
