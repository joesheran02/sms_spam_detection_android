package com.example.sms_spam_detection

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SuspiciousNumberParcel(
    val phoneNumber: String,
    val rating: Float,
    val spamMessages: List<MessageParcel>
) : Parcelable


@Parcelize
data class MessageParcel(
    val message: String,
    val index: Int,
    val rating: Float
) : Parcelable