package com.example.sms_spam_detection.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.MessageParcel
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.SuspiciousNumber
import com.example.sms_spam_detection.SuspiciousNumberParcel

class SuspiciousNumbersAdapter(private val suspiciousNumbers: List<SuspiciousNumber>, private val fragment: Fragment) :
    RecyclerView.Adapter<SuspiciousNumbersAdapter.ViewHolder>(), BaseAdapter {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phoneNumberTextView: TextView = view.findViewById(R.id.phoneNumberTextView)
        val ratingTextView: TextView = view.findViewById(R.id.ratingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suspicious_number_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suspiciousNumber = suspiciousNumbers[position]
        holder.phoneNumberTextView.text = suspiciousNumber.phoneNumber
        holder.ratingTextView.text = suspiciousNumber.rating.toString()
        holder.itemView.setOnClickListener {
            val messages = suspiciousNumber.spamMessages.sortedByDescending { it.rating }.mapIndexed{ index, message ->
                MessageParcel(message.message, index + 1, message.rating)
            }
            val action = SuspiciousFragmentDirections.suspiciousFragmentToInsights(
                SuspiciousNumberParcel(suspiciousNumber.phoneNumber, suspiciousNumber.rating, messages)
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return suspiciousNumbers.size
    }
}
