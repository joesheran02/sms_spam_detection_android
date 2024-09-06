package com.example.sms_spam_detection.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sms_spam_detection.Message
import com.example.sms_spam_detection.R
import com.example.sms_spam_detection.databinding.InsightsFragmentBinding
import com.example.sms_spam_detection.databinding.NumberDetailsBinding

class SuspiciousNumberDetails: Fragment() {
    private var _binding: NumberDetailsBinding? = null
    private val binding get()  = _binding
    private lateinit var adapter: NumberDetailsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NumberDetailsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    private val args: SuspiciousNumberDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.numberDetailsRecyclerView)
        val suspiciousNumber = args.suspiciousNumberParcelable
        adapter = NumberDetailsAdapter(suspiciousNumber.spamMessages)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        textView = view.findViewById(R.id.number)
        textView.text = suspiciousNumber.phoneNumber

    }
}