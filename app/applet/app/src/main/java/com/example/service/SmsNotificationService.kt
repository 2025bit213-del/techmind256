package com.example.service

import android.content.Context
import com.example.data.dao.SmsNotificationDao
import com.example.data.model.CaseEntity
import com.example.data.model.SmsNotificationEntity
import com.example.data.model.UserEntity
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsNotificationService(
    private val context: Context,
    private val smsNotificationDao: SmsNotificationDao
) {
    private val _liveSmsEvents = MutableSharedFlow<SmsNotificationEntity>(replay = 0, extraBufferCapacity = 10)
    val liveSmsEvents: SharedFlow<SmsNotificationEntity> = _liveSmsEvents.asSharedFlow()

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    suspend fun dispatchSms(
        recipientPhone: String,
        recipientName: String,
        eventType: String,
        messageContent: String,
        referenceId: String? = null
    ): SmsNotificationEntity {
        val sanitizedPhone = recipientPhone.trim()
        val maskedPhone = SecurityUtils.maskPhoneNumber(sanitizedPhone)

        val sms = SmsNotificationEntity(
            recipientPhone = maskedPhone,
            recipientName = recipientName,
            eventType = eventType,
            messageContent = messageContent,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED",
            referenceId = referenceId
        )

        try {
            smsNotificationDao.insertSms(sms)
            _liveSmsEvents.tryEmit(sms)
        } catch (e: Exception) {
            // Log fallback
        }
        return sms
    }

    suspend fun sendLoginSms(user: UserEntity): SmsNotificationEntity {
        val message = "VML Alert: Security login detected for ${user.fullName} (${user.role}) on ${formatTimestamp(System.currentTimeMillis())}. If not you, contact Admin immediately."
        return dispatchSms(user.phoneNumber, user.fullName, "LOGIN", message, user.userId)
    }

    suspend fun sendPaymentConfirmationSms(
        caseItem: CaseEntity,
        amount: Double,
        receiptNo: String,
        channel: String
    ): SmsNotificationEntity {
        val message = "VML Receipt: Payment of UGX ${formatCurrency(amount)} received via $channel. Ref: $receiptNo for Loan ${caseItem.caseId}. Remaining Balance: UGX ${formatCurrency(caseItem.totalOutstanding)}."
        return dispatchSms(caseItem.debtorPhone, caseItem.debtorName, "PAYMENT_CONFIRMATION", message, receiptNo)
    }

    suspend fun sendPaymentReminderSms(
        caseItem: CaseEntity,
        promisedOrDueAmount: Double,
        scheduledDate: String
    ): SmsNotificationEntity {
        val message = "VML Reminder: Dear ${caseItem.debtorName}, your scheduled repayment of UGX ${formatCurrency(promisedOrDueAmount)} is due on $scheduledDate. Settle promptly to avoid legal escalation."
        return dispatchSms(caseItem.debtorPhone, caseItem.debtorName, "REMINDER", message, caseItem.caseId)
    }

    suspend fun sendSecurityLockoutSms(user: UserEntity, reason: String): SmsNotificationEntity {
        val message = "VML Security: Account ${user.email} has been LOCKED due to multiple failed login attempts ($reason). Admin intervention required."
        return dispatchSms(user.phoneNumber, user.fullName, "SECURITY_LOCKOUT", message, user.userId)
    }

    suspend fun sendAccountUnlockedSms(user: UserEntity): SmsNotificationEntity {
        val message = "VML Security: Your account ${user.email} has been UNLOCKED by the Administrator. You can now log in safely."
        return dispatchSms(user.phoneNumber, user.fullName, "ACCOUNT_UNLOCKED", message, user.userId)
    }

    suspend fun sendCustomSms(
        phone: String,
        name: String,
        type: String,
        content: String,
        ref: String? = null
    ): SmsNotificationEntity {
        return dispatchSms(phone, name, type, content, ref)
    }
}
