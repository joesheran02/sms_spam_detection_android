package com.example.sms_spam_detection.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.databinding.FragmentNotificationsBinding
import com.example.sms_spam_detection.MyNotification
import com.example.sms_spam_detection.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)


        val sharedPreferences = context?.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val notificationsJson = sharedPreferences?.getString("notifications", null)

        val notificationsList: List<MyNotification> = if (notificationsJson != null) {
            Gson().fromJson(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type)
        } else {
            emptyList()
        }

        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationsAdapter = NotificationsAdapter(notificationsList)

        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerView.adapter = notificationsAdapter


        Log.d("line 38", viewModel.notifications.value.toString())

        Log.d("FragmentLifecycle", "Current State: ${lifecycle.currentState}")
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            Log.d("FragmentLifecycle", "Current State: ${lifecycle.currentState}")
            Log.d("Update in onviewcreated", "Finished line 41")
            // Update the UI with the new list of notifications
            updateRecyclerView(notifications)
        }

        viewModel.updateNotifications(notificationsList)

        /*if (viewModel.notifications.value.isNullOrEmpty()) {
            val sharedPreferences = context?.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            val notificationsJson = sharedPreferences?.getString("notifications", null)

            val notificationsList: List<MyNotification> = if (notificationsJson != null) {
                Gson().fromJson(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type)
            } else {
                emptyList()
            }
            // Update the ViewModel with the loaded notifications
            viewModel.updateNotifications(notificationsList)
        }*/

    }


    private fun updateRecyclerView(notifications: List<MyNotification>?) {

        binding.bellIcon.visibility = View.VISIBLE
        binding.noNotificationsText.visibility = View.VISIBLE
        binding.notificationRecyclerView.visibility = View.GONE

        // Set up RecyclerView and NotificationsAdapter
        notifications?.let {

            notificationsAdapter = NotificationsAdapter(it)
            notificationRecyclerView.adapter = notificationsAdapter

            if (notifications.isNotEmpty()) {
                binding.bellIcon.visibility = View.GONE
                binding.noNotificationsText.visibility = View.GONE
                binding.notificationRecyclerView.visibility = View.VISIBLE
            }
            Log.d("Entered", "Line 85")
        }


        Log.d("Update Recycler View", "Finished line 50")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}