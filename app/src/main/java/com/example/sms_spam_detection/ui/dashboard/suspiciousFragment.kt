package com.example.sms_spam_detection.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.HistoryInsight
import com.example.sms_spam_detection.MessageStats
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.SuspiciousNumber
import com.example.sms_spam_detection.databinding.InsightsFragmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SuspiciousFragment: Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter

    private var _binding: InsightsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InsightsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.suspiciousNumbersRecyclerView)
        val dataType = arguments?.getString("data_type") ?: "Unknown"
        if (dataType == "suspicious_numbers") {
            setupRecyclerViewSuspicious()
        }
        else {
            setupRecyclerViewHistory(dataType)
        }
        val textView : TextView = binding.title
        textView.text = arguments?.getString("title") ?: "Insights"
    }

    private fun setupRecyclerViewSuspicious() {
        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
        val dashboardJson = sharedPreferences?.getString("dashboard", null)
        lateinit var dashboardStats: MessageStats

        if (dashboardJson != null) {
            val type = object : TypeToken<MessageStats>() {}.type
            dashboardStats = Gson().fromJson(dashboardJson, type)
            val suspiciousNumbers: List<SuspiciousNumber> = dashboardStats.suspiciousNumbers.sortedByDescending { it.rating }
            adapter = SuspiciousNumbersAdapter(suspiciousNumbers, this)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter as SuspiciousNumbersAdapter
        }
        else {
            return
        }
    }

    private fun setupRecyclerViewHistory(dataType : String) {
        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
        val dashboardJsonHistory = sharedPreferences?.getString("history_dashboard", null)
        lateinit var dashboardStatsHistory: MutableList<MessageStats>
        lateinit var insightHistory: List<HistoryInsight>

        if (dashboardJsonHistory != null) {
            val type = object : TypeToken<MutableList<MessageStats>>() {}.type
            dashboardStatsHistory = Gson().fromJson(dashboardJsonHistory, type)
            when (dataType) {
                "sms_scanned" -> {
                    val message = { it: Int -> "Scanned $it messages" }
                    insightHistory = dashboardStatsHistory.map { HistoryInsight(message(it.numberSmsMessages), it.lastTimeScan) }
                }
                "total_spam" -> {
                    val message = { it: Int -> "Found $it spam messages" }
                    insightHistory = dashboardStatsHistory.map { HistoryInsight(message(it.numberSpamMessages), it.lastTimeScan) }
                }
                "last_scan" -> {
                    insightHistory = dashboardStatsHistory.map { HistoryInsight("Good", it.lastTimeScan) }
                }
            }
            adapter = HistoryInsightsAdapter(insightHistory)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter as HistoryInsightsAdapter
        }
        else {
            return
        }
    }

}

