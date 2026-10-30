package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val password: String,
    val fullName: String,
    val role: String,
    val department: String = "General",
    val phoneNumber: String = "",
    val organization: String = "VML Recover Ltd",
    val lastLoginAt: Long = 0L,
    val maskedUsername: String = "",
    val maskedPassword: String = "●●●●●●●●",
    val isLocked: Boolean = false,
    val failedAttempts: Int = 0,
    val lockReason: String? = null,
    val lockedAt: Long? = null,
    val passwordSalt: String = "",
    val isEmailVerified: Boolean = false,
    val emailVerificationCode: String? = null,
    val isFirstLogin: Boolean = true
)

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val caseId: String,
    val debtorName: String,
    val loanNumber: String,
    val clientName: String,
    val totalAmount: Double,
    val recoveredAmount: Double,
    val balance: Double,
    val status: String,
    val ageingDays: Int,
    val assignedOfficerId: String,
    val assignedOfficerName: String,
    val nextAction: String,
    val lastContactDate: Long,
    val collateral: String = "None",
    val contactPhone: String = "",
    val nationalId: String = "",
    val legalStage: String = "PRE_LEGAL",
    val riskLevel: String = "MEDIUM"
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true) val paymentId: Long = 0,
    val caseId: String,
    val amount: Double,
    val paymentDate: Long,
    val referenceNumber: String,
    val paymentChannel: String,
    val status: String = "COMPLETED",
    val recordedBy: String,
    val verifiedAt: Long? = null,
    val notes: String = "",
    val receiptUrl: String? = null
)

@Entity(tableName = "promises_to_pay")
data class PromiseToPayEntity(
    @PrimaryKey val promiseId: String,
    val caseId: String,
    val promiseAmount: Double,
    val promiseDate: Long,
    val status: String,
    val notes: String = "",
    val recordedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "field_visits")
data class FieldVisitEntity(
    @PrimaryKey val visitId: String,
    val caseId: String,
    val officerId: String,
    val officerName: String,
    val scheduledDate: Long,
    val completedDate: Long? = null,
    val status: String,
    val outcome: String = "",
    val locationAddress: String = "",
    val gpsCoordinates: String = "",
    val debtorFound: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "legal_documents")
data class LegalDocumentEntity(
    @PrimaryKey val docId: String,
    val caseId: String,
    val docType: String,
    val docTitle: String,
    val generatedDate: Long,
    val status: String = "ISSUED",
    val fileUrl: String = "",
    val advocateName: String = "Kisakye & Co. Advocates",
    val courtRef: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val userName: String,
    val action: String,
    val details: String,
    val ipAddress: String = "127.0.0.1",
    val severity: String = "INFO"
)

@Entity(tableName = "system_configs")
data class SystemConfigEntity(
    @PrimaryKey val configId: String = "DEFAULT_CONFIG",
    val interestRatePolicy: Double = 18.5,
    val autoEscalateDays: Int = 90,
    val maxLoginAttempts: Int = 5,
    val allowOfflineMode: Boolean = true,
    val maintenanceMode: Boolean = false,
    val emergencyAdminLocked: Boolean = false,
    val organizationName: String = "VML Recover Ltd",
    val emailVerificationRequired: Boolean = true
)

@Entity(tableName = "timeline_actions")
data class TimelineActionEntity(
    @PrimaryKey(autoGenerate = true) val actionId: Long = 0,
    val caseId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val description: String,
    val performedBy: String
)

@Entity(tableName = "sms_notifications")
data class SmsNotificationEntity(
    @PrimaryKey val smsId: String,
    val recipientPhone: String,
    val recipientName: String,
    val messageText: String,
    val sentAt: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED",
    val eventType: String = "PAYMENT_REMINDER"
)

@Entity(tableName = "payment_plans")
data class PaymentPlanEntity(
    @PrimaryKey val planId: String,
    val caseId: String,
    val totalPlanAmount: Double,
    val monthlyInstalment: Double,
    val numberOfInstalments: Int,
    val startDate: Long,
    val status: String = "ACTIVE"
)

enum class PlatformMode(val label: String, val shortLabel: String) {
    ANDROID("Android (Material 3)", "Android"),
    IOS("iOS (Cupertino Adaptive)", "iOS"),
    AUTO("Adaptive (Universal / Windows)", "Universal")
}
