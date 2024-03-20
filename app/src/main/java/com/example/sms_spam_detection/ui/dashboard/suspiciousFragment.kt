package com.example.sms_spam_detection.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.databinding.FragmentDashboardBinding
import com.example.sms_spam_detection.databinding.SuspiciousFragmentBinding

class SuspiciousFragment: Fragment() {


    private var _binding: SuspiciousFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SuspiciousFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}

