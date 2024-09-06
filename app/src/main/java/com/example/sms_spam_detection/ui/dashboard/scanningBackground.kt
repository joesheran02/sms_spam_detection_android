package com.example.sms_spam_detection.ui.dashboard

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.sms_spam_detection.*
import com.example.sms_spam_detection.databinding.ScanningBackgroundBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanningBackground: Fragment() {
    private var _binding: ScanningBackgroundBinding? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var percentageTextView: TextView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressBar)
        textView = view.findViewById(R.id.loadingText)
        percentageTextView = view.findViewById(R.id.percentage)

        startScanningTask()
    }

    private fun startScanningTask() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {

            val messageStats = goThroughPhoneMessages()
            // Update the UI on the main thread
//            requireActivity().runOnUiThread {
////                startColorFillAnimation(view, scanButton.backgroundTintList?.defaultColor ?: 0)
//                updateStatistics(messageStats)
//            }
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    updateStatistics(messageStats)
                }
                view?.postDelayed({
                    if (isAdded) { // Ensure fragment is still attached
                        findNavController().navigate(R.id.scanning_Background_to_action_Dashboard)
                    }
                }, 1000)
            }

            // Now that the scanning task is done, navigate back to DashboardFragment
//            requireActivity().runOnUiThread {
//                view?.postDelayed({
//                    findNavController().navigate(R.id.scanning_Background_to_action_Dashboard)
//                }, 1000)
//            }
        }
    }

    private fun startColorFillAnimation(view: View, startColor: Int) {
        val endColor = "#44cc88" // specify the color you want to fill the screen with

        val colorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), startColor, Color.parseColor(endColor))
        colorAnimator.duration = 1000 // set the duration of the animation in milliseconds
        colorAnimator.interpolator = AccelerateDecelerateInterpolator()

        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            view.setBackgroundColor(animatedColor)
        }

        colorAnimator.start()
    }

    private fun updateStatistics(messageStats: MessageStats) {
        updateCurrentStatistics(messageStats)
        updateHistoryStatistics(messageStats)
    }

    private fun updateHistoryStatistics(messageStats: MessageStats) {
        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)

        // Retrieve the existing history data
        val existingHistoryJson = sharedPreferences?.getString("history_dashboard", null)

        // Initialize a list to hold the history
        val historyList: MutableList<MessageStats> = if (existingHistoryJson != null) {
            // Deserialize the existing history JSON into a list of MessageStats
            val type = object : TypeToken<MutableList<MessageStats>>() {}.type
            Gson().fromJson(existingHistoryJson, type)
        } else {
            mutableListOf()
        }

        // Add the new MessageStats to the history
        historyList.add(messageStats)

        // Serialize the updated history list back to JSON
        val updatedHistoryJson = Gson().toJson(historyList)

        // Save the updated history to SharedPreferences
        sharedPreferences?.edit()?.putString("history_dashboard", updatedHistoryJson)?.apply()
    }


    private fun updateCurrentStatistics(messageStats: MessageStats) {
        val sharedPreferences = context?.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
        val updatedDashboardJson = Gson().toJson(messageStats)
        sharedPreferences?.edit()?.putString("dashboard", updatedDashboardJson)?.apply()
    }


    private fun getSuspiciousNumbers(addressMap: MutableMap<String, SpamInfo>): List<SuspiciousNumber> {
        //if (addressMap.isEmpty()) {
        // Handle the case where there are no messages
        //return List<size: 0>
        //}
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val suspiciousNumberSensitivity = sharedPreferences.getFloat("suspicious_number_sensitivity", 0.50f)

        return addressMap.map { (address, spamInfoMap) ->
            val totalMessages = spamInfoMap.totalMessages.toFloat() // Convert to float to ensure float division and avoid division by zero
            val spamCount = spamInfoMap.spamCount.toFloat() // Convert to float to ensure float division
            SuspiciousNumber(address, spamCount / totalMessages, spamInfoMap.spamMessages)
        }.filter{ (_, rating) ->
            rating >= suspiciousNumberSensitivity
        }
        /*return addressMap.count { (_, spamInfoMap) ->
            val totalMessages = spamInfoMap.getOrDefault("totalMessages", 0)
            val spamCount = spamInfoMap.getOrDefault("spamCount", 0)

            // Check if the proportion of spam messages is above 0.80
            totalMessages > 0 && spamCount.toFloat() / totalMessages.toFloat() > 0.80
        }*/
    }


    private fun goThroughPhoneMessages(): MessageStats {
        // Implement the logic to go through all phone messages
        // For example, you can use contentResolver to get SMS messages
        val uri = Uri.parse("content://sms/inbox")
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)

        var numberSmsMessages = 0
        var numberSpamMessages = 0
        val addressMap: MutableMap<String, SpamInfo> = mutableMapOf()


        cursor?.use {
            val totalMessages = it.count // Get the total number of messages
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")

            // Check if the columns exist in the cursor
            if (addressIndex != -1 && bodyIndex != -1) {
                var currentIndex = 0
                while (it.moveToNext()) {

                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val spamDetectionSensitivity = sharedPreferences.getFloat("spam_detection_sensitivity", 0.50f)

                    // Access SMS details using cursor
                    val address = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    val prob = getSpamProb(body)
                    val isSpam = prob >= spamDetectionSensitivity

                    Log.d("address", address)
                    Log.d("body", body)

                    // Process each message as needed
                    // You can perform spam detection logic here
                    numberSmsMessages += 1
                    val spamInfoMap = addressMap.getOrPut(address) { SpamInfo(0, 0)  }
                    if (isSpam) {
                        spamInfoMap.spamCount += 1
                        spamInfoMap.spamMessages.add(Message(body, prob))
                        numberSpamMessages += 1
                    }
                    spamInfoMap.totalMessages += 1

                    currentIndex++
                    val progress = (currentIndex.toFloat() / totalMessages.toFloat()) * 100
                    progressBar.progress = progress.toInt()

                    // Update the TextView on the main thread
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            textView.text = "Loading from $address"
                            percentageTextView.text = "${progress.toInt()}%"
                        }
                    }
                }
            }
            else {
                // Handle the case where columns do not exist in the cursor
                Log.e("DashboardFragment", "Columns not found in the cursor")
            }
        }

        val suspiciousNumbers = getSuspiciousNumbers(addressMap)

        return MessageStats(numberSmsMessages, numberSpamMessages, suspiciousNumbers, System.currentTimeMillis())
    }

    private fun getSpamProb(body: String): Float {
        val additionalFeatures: List<Float> = SmsReceiver().createAdditionalFeatures(body)
        val cleanedText = SmsReceiver().cleanText(body)
        Log.d("Additional Features", additionalFeatures.toString())
        Log.d("Cleaned text", cleanedText)

        // Determine if the message is spam (replace this with your spam detection logic)
        val spamProb =
            context?.let { SmsReceiver().spamProbability(it, cleanedText, additionalFeatures) }
        return spamProb!!
    }

}
