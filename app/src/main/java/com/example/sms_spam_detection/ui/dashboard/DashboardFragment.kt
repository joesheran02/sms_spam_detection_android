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
import com.example.sms_spam_detection.MyNotification
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.databinding.FragmentDashboardBinding
import com.example.sms_spam_detection.SmsReceiver
import com.example.sms_spam_detection.TimeUtils
import com.fasterxml.jackson.databind.JsonSerializer.None
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.graphics.Color
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController


class DashboardFragment : Fragment() {


    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scanButton: AppCompatButton = view.findViewById(R.id.scan_button)
        val suspiciousTextView: TextView = view.findViewById(R.id.suspicious_numbers)

        scanButton.setOnClickListener {
            revealProgressBar()
            /*progressBar = view.findViewById(R.id.progressBar)

            // Start a coroutine to perform the background task
            GlobalScope.launch(Dispatchers.Default) {
                val messageStats = goThroughPhoneMessages()

                // Update the UI on the main thread
                requireActivity().runOnUiThread {
                    //startColorFillAnimation(view, scanButton.backgroundTintList?.defaultColor ?: 0)
                    updateStatistics(messageStats)
                    updateDashboardStats()
                    hideProgressBar()
                }
            }*/
            findNavController().navigate(R.id.action_Dashboard_to_scanning_Background
            )
        }

        suspiciousTextView.setOnClickListener {
            findNavController().navigate(R.id.action_Dashboard_to_SuspiciousFragment)
        }
    }

    private fun startColorFillAnimation(view: View, startColor: Int) {
        val endColor = "#44cc88" // specify the color you want to fill the screen with

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, Color.parseColor(endColor))
        colorAnimator.duration = 1000 // set the duration of the animation in milliseconds
        colorAnimator.interpolator = AccelerateDecelerateInterpolator()

        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            view.setBackgroundColor(animatedColor)
        }

        colorAnimator.start()
    }

    interface MessageProcessingListener {
        fun onMessageProcessed(currentIndex: Int, totalMessages: Int)
        fun onMessageProcessingComplete()
    }

    private fun hideProgressBar() {
        val progressBar: ProgressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE

        // Also change the text
        val textView: TextView = binding.textDashboard
        textView.text = "Completed Scan"
    }

    private fun revealProgressBar() {
        val progressBar: ProgressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE
    }

    private fun updateStatistics(messageStats: MessageStats) {

        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
        val updatedDashboardJson = Gson().toJson(messageStats)
        sharedPreferences?.edit()?.putString("dashboard", updatedDashboardJson)?.apply()
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
    
    private fun countSuspicious(addressMap: Map<String, Map<String, Int>>): List<SuspiciousNumber> {
        //if (addressMap.isEmpty()) {
            // Handle the case where there are no messages
            //return List<size: 0>
        //}
        return addressMap.map { (address, spamInfoMap) ->
            val totalMessages = spamInfoMap.getOrDefault("totalMessages", 1).toFloat() // Convert to float to ensure float division and avoid division by zero
            val spamCount = spamInfoMap.getOrDefault("spamCount", 0).toFloat() // Convert to float to ensure float division
            SuspiciousNumber(address, spamCount / totalMessages)
        }.filter{ (_, rating) ->
            rating > 0.80
        }
        /*return addressMap.count { (_, spamInfoMap) ->
            val totalMessages = spamInfoMap.getOrDefault("totalMessages", 0)
            val spamCount = spamInfoMap.getOrDefault("spamCount", 0)

            // Check if the proportion of spam messages is above 0.80
            totalMessages > 0 && spamCount.toFloat() / totalMessages.toFloat() > 0.80
        }*/
    }

    data class SuspiciousNumber(
        val phoneNumber: String,
        val rating: Float
    )

    data class MessageStats(
        val numberSmsMessages: Int,
        val numberSpamMessages: Int,
        val suspiciousNumbers: List<SuspiciousNumber>,
        val lastTimeScan: Long
    )


    private fun goThroughPhoneMessages(): MessageStats {
        // Implement the logic to go through all phone messages
        // For example, you can use contentResolver to get SMS messages
        val uri = Uri.parse("content://sms/inbox")
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)

        var numberSmsMessages = 0
        var numberSpamMessages = 0
        val addressMap: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()


        cursor?.use {
            val totalMessages = it.count // Get the total number of messages
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")

            // Check if the columns exist in the cursor
            if (addressIndex != -1 && bodyIndex != -1) {
                var currentIndex = 0
                while (it.moveToNext()) {


                    // Access SMS details using cursor
                    val address = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    val isSpam = processSms(body)


                    Log.d("address", address)
                    Log.d("body", body)

                    // Process each message as needed
                    // You can perform spam detection logic here
                    numberSmsMessages += 1
                    val spamInfoMap = addressMap.getOrPut(address) { mutableMapOf("spamCount" to 0, "totalMessages" to 0) }
                    if (isSpam) {
                        spamInfoMap["spamCount"] = spamInfoMap["spamCount"]!! + 1
                        numberSpamMessages += 1
                    }
                    spamInfoMap["totalMessages"] = spamInfoMap["totalMessages"]!! + 1

                    currentIndex++
                    val progress = (currentIndex.toFloat() / totalMessages.toFloat()) * 100
                    progressBar.progress = progress.toInt()
                }
            }
            else {
                // Handle the case where columns do not exist in the cursor
                Log.e("DashboardFragment", "Columns not found in the cursor")
            }
        }

        val suspiciousNumbers = countSuspicious(addressMap)

        return MessageStats(numberSmsMessages, numberSpamMessages, suspiciousNumbers, System.currentTimeMillis())
    }

    private fun processSms(body: String): Boolean {
        val additionalFeatures: List<Float> = SmsReceiver().createAdditionalFeatures(body)
        val cleanedText = SmsReceiver().cleanText(body)
        Log.d("Additional Features", additionalFeatures.toString())
        Log.d("Cleaned text", cleanedText)

        // Determine if the message is spam (replace this with your spam detection logic)
        val spamProb =
            context?.let { SmsReceiver().spamProbability(it, cleanedText, additionalFeatures) }
        return spamProb!! > 0.5
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