package com.example.service

import com.example.data.AppDatabase
import com.example.data.model.SmsNotificationEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

class SmsNotificationService(private val database: AppDatabase) {
    private val _dispatchedSms = MutableSharedFlow<SmsNotificationEntity>(replay = 1)
    val dispatchedSms = _dispatchedSms.asSharedFlow()

    suspend fun dispatchSms(phone: String, recipientName: String, eventType: String, messageText: String) {
        val sms = SmsNotificationEntity(
            smsId = "SMS-" + UUID.randomUUID().toString().take(8).uppercase(),
            recipientPhone = phone,
            recipientName = recipientName,
            messageText = messageText,
            sentAt = System.currentTimeMillis(),
            status = "DELIVERED",
            eventType = eventType
        )
        database.smsNotificationDao().insertSms(sms)
        _dispatchedSms.emit(sms)
    }

    suspend fun dispatchEmailVerification(email: String, recipientName: String, code: String) {
        val message = "VML Recover Security Verification: Your one-time access verification code is $code. This code verifies your email address for authorized first-time access."
        dispatchSms(
            phone = email,
            recipientName = recipientName,
            eventType = "VERIFICATION_CODE",
            messageText = message
        )
    }
}
