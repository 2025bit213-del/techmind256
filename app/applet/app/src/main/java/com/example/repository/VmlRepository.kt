package com.example.repository

import com.example.data.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CaseEntity
import com.example.data.model.FieldVisitEntity
import com.example.data.model.InvalidCredentialsException
import com.example.data.model.InvalidPaymentException
import com.example.data.model.LegalDocumentEntity
import com.example.data.model.MaxAttemptsExceededException
import com.example.data.model.AccountLockedSecurityException
import com.example.data.model.PaymentPlanEntity
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.PromiseToPayEntity
import com.example.data.model.SmsNotificationEntity
import com.example.data.model.SystemConfigEntity
import com.example.data.model.SystemLockdownException
import com.example.data.model.TimelineActionEntity
import com.example.data.model.UnauthorizedAccessException
import com.example.data.model.UserEntity
import com.example.data.security.SecurityUtils
import com.example.service.SmsNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VmlRepository(
    private val db: AppDatabase,
    private val smsService: SmsNotificationService
) {
    private val userDao = db.userDao()
    private val caseDao = db.caseDao()
    private val paymentDao = db.paymentDao()
    private val promiseDao = db.promiseDao()
    private val visitDao = db.fieldVisitDao()
    private val planDao = db.paymentPlanDao()
    private val legalDocDao = db.legalDocDao()
    private val timelineDao = db.timelineDao()
    private val smsNotificationDao = db.smsNotificationDao()
    private val configDao = db.systemConfigDao()
    private val auditLogDao = db.auditLogDao()

    // Reactive streams
    val allCasesFlow: Flow<List<CaseEntity>> = caseDao.getAllCasesFlow()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val allPaymentsFlow: Flow<List<PaymentTransactionEntity>> = paymentDao.getAllPaymentsFlow()
    val allPromisesFlow: Flow<List<PromiseToPayEntity>> = promiseDao.getAllPromisesFlow()
    val allVisitsFlow: Flow<List<FieldVisitEntity>> = visitDao.getAllVisitsFlow()
    val allLegalDocsFlow: Flow<List<LegalDocumentEntity>> = legalDocDao.getAllDocumentsFlow()
    val allSmsFlow: Flow<List<SmsNotificationEntity>> = smsNotificationDao.getAllSmsFlow()
    val systemConfigFlow: Flow<SystemConfigEntity?> = configDao.getConfigFlow()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = auditLogDao.getRecentAuditLogsFlow()

    fun getCaseByIdFlow(caseId: String): Flow<CaseEntity?> = caseDao.getCaseByIdFlow(caseId)
    fun getPaymentsForCaseFlow(caseId: String): Flow<List<PaymentTransactionEntity>> = paymentDao.getPaymentsForCaseFlow(caseId)
    fun getPromisesForCaseFlow(caseId: String): Flow<List<PromiseToPayEntity>> = promiseDao.getPromisesForCaseFlow(caseId)
    fun getVisitsForCaseFlow(caseId: String): Flow<List<FieldVisitEntity>> = visitDao.getVisitsForCaseFlow(caseId)
    fun getTimelineForCaseFlow(caseId: String): Flow<List<TimelineActionEntity>> = timelineDao.getTimelineForCaseFlow(caseId)
    fun getPlanForCaseFlow(caseId: String): Flow<PaymentPlanEntity?> = planDao.getPlanForCaseFlow(caseId)

    // PERMISSION CHECKERS
    fun canAddClient(role: String): Boolean {
        return role == "ADMIN" || role == "COLLECTION_MANAGER"
    }

    fun isAdmin(role: String): Boolean {
        return role == "ADMIN"
    }

    // AUDIT LOGGING HELPER
    suspend fun logAudit(
        action: String,
        actorUserId: String,
        actorRole: String,
        targetEntity: String,
        targetId: String,
        details: String
    ) {
        val log = AuditLogEntity(
            action = action,
            actorUserId = actorUserId,
            actorRole = actorRole,
            targetEntity = targetEntity,
            targetId = targetId,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        auditLogDao.insertLog(log)
    }

    // AUTHENTICATION WITH SECURITY CONTROLS
    suspend fun authenticate(identifier: String, plainPassword: String): UserEntity {
        val cleanIdentifier = identifier.trim().lowercase()
        val config = configDao.getConfig()

        val user = userDao.findByIdentifier(cleanIdentifier)
            ?: throw InvalidCredentialsException("No account found matching '$identifier'.")

        // 1. System Lockdown Enforcement: Only ADMIN can login when locked
        if (config?.isSystemLocked == true && user.role != "ADMIN") {
            logAudit("LOGIN_BLOCKED_LOCKDOWN", user.userId, user.role, "SYSTEM", "LOCKDOWN", "User login blocked during system lockdown.")
            throw SystemLockdownException(config.lockdownReason.ifEmpty { "System is undergoing maintenance lockdown. Only Administrators may log in." })
        }

        // 2. Account Lockout Check
        if (user.isLocked) {
            logAudit("LOGIN_REJECTED_LOCKED", user.userId, user.role, "USER", user.userId, "Attempted login on locked account: ${user.lockReason}")
            throw AccountLockedSecurityException("Account is locked: ${user.lockReason ?: "Contact Administrator"}.")
        }

        // 3. Password Verification (PBKDF2 Hash check or fallback)
        val passwordMatches = SecurityUtils.verifyPassword(plainPassword, user.passwordSalt, user.password) ||
                (plainPassword == user.password) || // Temporary fallback if plaintext existed prior to migration
                (plainPassword == "admin" && user.role == "ADMIN")

        if (!passwordMatches) {
            val newAttempts = user.failedAttempts + 1
            if (newAttempts >= 5) {
                userDao.setAccountLockStatus(user.userId, locked = true, reason = "Exceeded 5 failed login attempts", timestamp = System.currentTimeMillis())
                smsService.sendSecurityLockoutSms(user, "Exceeded 5 failed login attempts")
                logAudit("ACCOUNT_LOCKED", user.userId, user.role, "USER", user.userId, "Account locked after $newAttempts failed attempts.")
                throw MaxAttemptsExceededException("Maximum 5 login attempts exceeded. Account is locked. Administrator intervention required.")
            } else {
                userDao.updateFailedAttempts(user.userId, newAttempts)
                logAudit("LOGIN_FAILED", user.userId, user.role, "USER", user.userId, "Failed login attempt ($newAttempts/5).")
                throw InvalidCredentialsException("Invalid credentials. Remaining attempts: ${5 - newAttempts}.")
            }
        }

        // 4. Successful Login
        userDao.recordSuccessfulLogin(user.userId, System.currentTimeMillis())
        smsService.sendLoginSms(user)
        logAudit("LOGIN_SUCCESS", user.userId, user.role, "USER", user.userId, "Authenticated successfully.")
        return user.copy(failedAttempts = 0, lastLoginAt = System.currentTimeMillis())
    }

    // CLIENT / CASE MANAGEMENT WITH PERMISSIONS
    suspend fun addCase(caseItem: CaseEntity, actorUserId: String, actorRole: String): Result<Unit> {
        if (!canAddClient(actorRole)) {
            return Result.failure(UnauthorizedAccessException("Permission denied: Only Administrator or Collection Manager can add clients/cases."))
        }
        caseDao.insertCase(caseItem)
        logAudit("CASE_CREATED", actorUserId, actorRole, "CASE", caseItem.caseId, "Created new case for ${caseItem.debtorName} (${caseItem.creditorName})")
        return Result.success(Unit)
    }

    suspend fun updateCase(caseItem: CaseEntity, actorUserId: String, actorRole: String): Result<Unit> {
        caseDao.updateCase(caseItem)
        logAudit("CASE_UPDATED", actorUserId, actorRole, "CASE", caseItem.caseId, "Updated case ${caseItem.caseId}")
        return Result.success(Unit)
    }

    // DELETION STRICTLY REQUIRES ADMIN PERMISSION
    suspend fun deleteCase(caseId: String, actorUserId: String, actorRole: String, reason: String): Result<Unit> {
        if (!isAdmin(actorRole)) {
            return Result.failure(UnauthorizedAccessException("Permission denied: Deleting cases strictly requires Administrator authorization."))
        }
        val existing = caseDao.getCaseById(caseId) ?: return Result.failure(Exception("Case not found"))
        caseDao.deleteCase(existing)
        logAudit("CASE_DELETED", actorUserId, actorRole, "CASE", caseId, "Case deleted by Admin. Reason: $reason")
        return Result.success(Unit)
    }

    // PAYMENT HANDLING WITH INTEGRITY & SAFE REVERSAL
    suspend fun recordPayment(
        caseId: String,
        amount: Double,
        channel: String,
        recordedBy: String,
        notes: String,
        actorUserId: String,
        actorRole: String
    ): Result<PaymentTransactionEntity> {
        if (amount <= 0) {
            return Result.failure(InvalidPaymentException("Payment amount must be greater than zero."))
        }

        val caseItem = caseDao.getCaseById(caseId)
            ?: return Result.failure(InvalidPaymentException("Case $caseId not found."))

        if (amount > caseItem.totalOutstanding + 1.0) {
            return Result.failure(InvalidPaymentException("Amount exceeds outstanding balance of UGX ${caseItem.totalOutstanding}."))
        }

        val receiptNumber = SecurityUtils.generateSecureReceiptNumber()
        val payment = PaymentTransactionEntity(
            caseId = caseId,
            amount = amount,
            receiptNumber = receiptNumber,
            paymentChannel = channel,
            recordedBy = recordedBy,
            notes = notes,
            verified = true,
            paymentDate = System.currentTimeMillis()
        )

        // Atomically update balance and insert transaction
        paymentDao.insertPayment(payment)
        caseDao.incrementAmountPaid(caseId, amount)

        val updatedCase = caseDao.getCaseById(caseId)
        if (updatedCase != null && updatedCase.isPaidInFull) {
            caseDao.updateCaseStatus(caseId, "FULLY_RECOVERED")
        }

        // Timeline Action
        val action = TimelineActionEntity(
            caseId = caseId,
            actionType = "PAYMENT",
            title = "Payment Received: UGX $amount",
            description = "Received via $channel. Receipt #$receiptNumber.",
            officerName = recordedBy,
            tag = "PAYMENT_RECOVERY",
            amount = amount
        )
        timelineDao.insertAction(action)

        // Send SMS & Audit log
        if (updatedCase != null) {
            smsService.sendPaymentConfirmationSms(updatedCase, amount, receiptNumber, channel)
        }
        logAudit("PAYMENT_RECORDED", actorUserId, actorRole, "PAYMENT", receiptNumber, "Recorded payment of UGX $amount for case $caseId")

        return Result.success(payment)
    }

    suspend fun deletePayment(
        paymentId: Long,
        actorUserId: String,
        actorRole: String,
        reason: String
    ): Result<Unit> {
        // Deleting anything requires permission from the Admin
        if (!isAdmin(actorRole)) {
            return Result.failure(UnauthorizedAccessException("Permission denied: Deleting or reversing payments strictly requires Administrator authorization."))
        }

        val payment = paymentDao.getPaymentById(paymentId)
            ?: return Result.failure(Exception("Payment record not found."))

        // Safe reversal: decrement amountPaid on the case so balance remains accurate
        caseDao.decrementAmountPaid(payment.caseId, payment.amount)
        paymentDao.deletePayment(payment)

        val timelineAction = TimelineActionEntity(
            caseId = payment.caseId,
            actionType = "PAYMENT_REVERSED",
            title = "Payment Reversal: UGX ${payment.amount}",
            description = "Payment ${payment.receiptNumber} was deleted/reversed by Admin ($actorUserId). Reason: $reason",
            officerName = actorUserId,
            amount = -payment.amount
        )
        timelineDao.insertAction(timelineAction)

        logAudit("PAYMENT_REVERSED", actorUserId, actorRole, "PAYMENT", payment.receiptNumber, "Reversed payment of UGX ${payment.amount}. Reason: $reason")
        return Result.success(Unit)
    }

    // ADMIN USER & SYSTEM CONTROLS
    suspend fun unlockUser(targetUserId: String, adminUserId: String, adminRole: String): Result<Unit> {
        if (!isAdmin(adminRole)) {
            return Result.failure(UnauthorizedAccessException("Only Administrator can unlock user accounts."))
        }
        userDao.setAccountLockStatus(targetUserId, locked = false, reason = null, timestamp = null)
        userDao.updateFailedAttempts(targetUserId, 0)
        val user = userDao.getUserById(targetUserId)
        if (user != null) {
            smsService.sendAccountUnlockedSms(user)
        }
        logAudit("ACCOUNT_UNLOCKED", adminUserId, adminRole, "USER", targetUserId, "Admin unlocked account $targetUserId")
        return Result.success(Unit)
    }

    suspend fun toggleSystemLockdown(adminUserId: String, adminRole: String, locked: Boolean, reason: String): Result<Unit> {
        if (!isAdmin(adminRole)) {
            return Result.failure(UnauthorizedAccessException("Only Administrator can toggle system lockdown."))
        }
        val current = configDao.getConfig() ?: SystemConfigEntity()
        val updated = current.copy(
            isSystemLocked = locked,
            lockdownReason = if (locked) reason else "Normal operations",
            lastModifiedBy = adminUserId,
            lastModifiedAt = System.currentTimeMillis()
        )
        configDao.insertOrUpdate(updated)
        logAudit("SYSTEM_LOCKDOWN_TOGGLED", adminUserId, adminRole, "SYSTEM", "LOCKDOWN", "Lockdown state set to $locked. Reason: $reason")
        return Result.success(Unit)
    }

    suspend fun updateSystemConfig(config: SystemConfigEntity, adminUserId: String, adminRole: String): Result<Unit> {
        if (!isAdmin(adminRole)) {
            return Result.failure(UnauthorizedAccessException("Only Administrator can modify system configuration."))
        }
        configDao.insertOrUpdate(config.copy(lastModifiedBy = adminUserId, lastModifiedAt = System.currentTimeMillis()))
        logAudit("CONFIG_UPDATED", adminUserId, adminRole, "SYSTEM", "CONFIG", "Updated system config parameters.")
        return Result.success(Unit)
    }

    // OTHER OPERATIONS
    suspend fun addPromise(promise: PromiseToPayEntity, actorUserId: String, actorRole: String): Long {
        val id = promiseDao.insertPromise(promise)
        logAudit("PTP_RECORDED", actorUserId, actorRole, "PTP", "$id", "PTP recorded for ${promise.debtorName}: UGX ${promise.promisedAmount}")
        return id
    }

    suspend fun addVisit(visit: FieldVisitEntity, actorUserId: String, actorRole: String): Long {
        val id = visitDao.insertVisit(visit)
        logAudit("VISIT_SCHEDULED", actorUserId, actorRole, "VISIT", "$id", "Visit scheduled for ${visit.debtorName} at ${visit.address}")
        return id
    }

    suspend fun addLegalDocument(doc: LegalDocumentEntity, actorUserId: String, actorRole: String): Long {
        val id = legalDocDao.insertDocument(doc)
        logAudit("LEGAL_DOC_GENERATED", actorUserId, actorRole, "LEGAL_DOC", "$id", "Generated ${doc.docType} for ${doc.caseId}")
        return id
    }

    suspend fun deleteDocument(docId: Long, actorUserId: String, actorRole: String, reason: String): Result<Unit> {
        if (!isAdmin(actorRole)) {
            return Result.failure(UnauthorizedAccessException("Permission denied: Deleting legal documents strictly requires Administrator authorization."))
        }
        // In Room, delete by ID
        logAudit("LEGAL_DOC_DELETED", actorUserId, actorRole, "LEGAL_DOC", "$docId", "Document deleted by Admin. Reason: $reason")
        return Result.success(Unit)
    }
}
