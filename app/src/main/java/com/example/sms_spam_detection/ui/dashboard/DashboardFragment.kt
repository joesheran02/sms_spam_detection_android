package com.example.sms_spam_detection.ui.dashboard

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sms_spam_detection.databinding.FragmentDashboardBinding
import com.fasterxml.jackson.databind.JsonSerializer.None
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.graphics.Color
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.sms_spam_detection.*


class DashboardFragment : Fragment() {


    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun checkExistsFile(name: String, key: String): Boolean {
        val sharedPreferences = context?.getSharedPreferences(name, Context.MODE_PRIVATE)
        val existingHistoryJson = sharedPreferences?.getString(key, null)
        return existingHistoryJson != null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scanButton: LottieAnimationView = view.findViewById(R.id.scan_button)
        val suspiciousTextView: TextView = view.findViewById(R.id.suspicious_numbers)

        scanButton?.setAnimation("scan_button.json")
        scanButton?.repeatCount = ValueAnimator.INFINITE
        scanButton?.playAnimation()

        val argsBundle = Bundle()

        scanButton.setOnClickListener {
            findNavController().navigate(R.id.action_Dashboard_to_scanning_Background)
            updateDashboardStats()
        }

        if (checkExistsFile("dashboard_prefs", "history_dashboard")) {
            val smsScannedTextView: TextView = view.findViewById(R.id.sms_scanned)
            val totalSpamTextView: TextView = view.findViewById(R.id.total_spam)
            val lastScanTextView: TextView = view.findViewById(R.id.last_scan)

            smsScannedTextView.setOnClickListener {
                argsBundle.putString("title", "History of SMS Scanned")
                argsBundle.putString("data_type", "sms_scanned")
                findNavController().navigate(R.id.action_Dashboard_to_SuspiciousFragment, argsBundle)
            }

            totalSpamTextView.setOnClickListener {
                argsBundle.putString("title", "History of Total Spam Messages")
                argsBundle.putString("data_type", "total_spam")
                findNavController().navigate(R.id.action_Dashboard_to_SuspiciousFragment, argsBundle)
            }

            lastScanTextView.setOnClickListener {
                argsBundle.putString("title", "History of Last Scan Date")
                argsBundle.putString("data_type", "last_scan")
                findNavController().navigate(R.id.action_Dashboard_to_SuspiciousFragment, argsBundle)
            }
        }
        else {
            Log.d("fail", "FAILED")
        }

        if (checkExistsFile("dashboard_prefs", "dashboard"))
            suspiciousTextView.setOnClickListener {
                argsBundle.putString("title", "Here is a list of suspicious numbers in descending order of suspicion")
                argsBundle.putString("data_type", "suspicious_numbers")
                findNavController().navigate(R.id.action_Dashboard_to_SuspiciousFragment, argsBundle)
            }
    }


    private fun updateDashboardStats() {
        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
        val dashboardJson = sharedPreferences?.getString("dashboard", null)
        lateinit var dashboardStats: MessageStats
        if (dashboardJson != null) {
            val type = object : TypeToken<MessageStats>() {}.type
            dashboardStats = Gson().fromJson(dashboardJson, type)
            val smsScanned: TextView = binding.smsScanned
            smsScanned.text = "Number of SMS Messages Scanned\n${dashboardStats.numberSmsMessages}"
            val totalSpam: TextView = binding.totalSpam
            totalSpam.text = "Number of Spam SMS Messages\n${dashboardStats.numberSpamMessages}"
            val suspiciousNumbers: TextView = binding.suspiciousNumbers
            suspiciousNumbers.text = "Suspicious Numbers\n${dashboardStats.suspiciousNumbers.size}"
            val lastScan: TextView = binding.lastScan
            lastScan.text = "Last Scan Date\n${TimeUtils.getTimeAgo(dashboardStats.lastTimeScan)}"
        }
        else {
            Log.e("Something went wrong with saving", "or freshly initialised")
            val smsScanned: TextView = binding.smsScanned
            smsScanned.text = "Number of SMS Messages Scanned\n-/-"
            val totalSpam: TextView = binding.totalSpam
            totalSpam.text = "Number of Spam SMS Messages\n-/-"
            val suspiciousNumbers: TextView = binding.suspiciousNumbers
            suspiciousNumbers.text = "Suspicious Numbers\n-/-"
            val lastScan: TextView = binding.lastScan
            lastScan.text = "Last Scan Date\n-/-"
            return
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        updateDashboardStats()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}