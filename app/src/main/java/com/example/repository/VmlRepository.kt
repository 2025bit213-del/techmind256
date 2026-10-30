package com.example.repository

import com.example.data.AppDatabase
import com.example.data.SampleDataProvider
import com.example.data.firebase.FirebaseRealtimeDbService
import com.example.data.model.*
import com.example.data.security.SecurityUtils
import com.example.service.EmailVerificationService
import com.example.service.SmsNotificationService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VmlRepository(
    private val database: AppDatabase,
    private val firebaseService: FirebaseRealtimeDbService,
    val smsService: SmsNotificationService
) {
    val emailService: EmailVerificationService = EmailVerificationService(database)
    val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()
    val allCases: Flow<List<CaseEntity>> = database.caseDao().getAllCases()
    val allPayments: Flow<List<PaymentTransactionEntity>> = database.paymentDao().getAllPayments()
    val allPromises: Flow<List<PromiseToPayEntity>> = database.promiseDao().getAllPromises()
    val allVisits: Flow<List<FieldVisitEntity>> = database.fieldVisitDao().getAllVisits()
    val allAuditLogs: Flow<List<AuditLogEntity>> = database.auditLogDao().getAllAuditLogs()
    val allTimeline: Flow<List<TimelineActionEntity>> = database.timelineDao().getAllTimeline()
    val allSmsNotifications: Flow<List<SmsNotificationEntity>> = database.smsNotificationDao().getAllSms()
    val systemConfigFlow: Flow<SystemConfigEntity?> = database.systemConfigDao().getConfigFlow()

    fun getAllLegalDocs(): Flow<List<LegalDocumentEntity>> = database.legalDocDao().getAllLegalDocs()

    suspend fun ensureSeedData() {
        SampleDataProvider.populateInitialData(database)
    }

    suspend fun findUserByIdentifier(identifier: String): UserEntity? {
        val clean = SecurityUtils.sanitizeInput(identifier)
        return database.userDao().findUserByIdentifier(clean)
    }

    suspend fun authenticateUser(identifier: String, plainPassword: String): UserEntity {
        val user = findUserByIdentifier(identifier)
            ?: throw InvalidCredentialsException("No account registered with email or ID: $identifier. Member accounts must be registered by an Administrator.")

        if (user.isLocked) {
            val reason = user.lockReason ?: "Account locked by Administrator for security compliance."
            throw AccountLockedSecurityException("SECURITY LOCKOUT: Account for ${user.fullName} is LOCKED ($reason). Please contact your administrator.")
        }

        val isValid = SecurityUtils.verifyPassword(plainPassword, user.password, user.passwordSalt)
        if (!isValid) {
            val newAttempts = user.failedAttempts + 1
            database.userDao().updateFailedAttempts(user.userId, newAttempts)
            val config = database.systemConfigDao().getConfig()
            val maxAttempts = config?.maxLoginAttempts ?: 5
            if (newAttempts >= maxAttempts) {
                database.userDao().lockUser(
                    userId = user.userId,
                    reason = "Exceeded $maxAttempts consecutive failed authentication attempts.",
                    timestamp = System.currentTimeMillis()
                )
                database.auditLogDao().insertLog(
                    AuditLogEntity(
                        userId = user.userId,
                        userName = user.fullName,
                        action = "ACCOUNT_LOCKED",
                        details = "Account locked out automatically after $newAttempts failed login attempts.",
                        severity = "SECURITY_ALERT"
                    )
                )
                throw MaxAttemptsExceededException("Account has been LOCKED due to $newAttempts failed attempts. Contact administrator.")
            }
            throw InvalidCredentialsException("Invalid password. Remaining attempts before security lockout: ${maxAttempts - newAttempts}.")
        }

        database.userDao().recordSuccessfulLogin(user.userId, System.currentTimeMillis())
        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = user.userId,
                userName = user.fullName,
                action = "USER_LOGIN",
                details = "User authenticated successfully (PBKDF2-SHA256 verified).",
                severity = "INFO"
            )
        )
        return user
    }

    suspend fun verifyEmailCode(userId: String, code: String): Pair<Boolean, String> {
        val user = database.userDao().getUserById(userId) ?: return Pair(false, "User not found.")

        when (val result = emailService.verify(user.email, code)) {
            is EmailVerificationService.VerifyResult.Success -> {
                database.userDao().markEmailVerified(userId)
                database.auditLogDao().insertLog(
                    AuditLogEntity(
                        userId = user.userId,
                        userName = user.fullName,
                        action = "EMAIL_VERIFIED",
                        details = "First-time email verification completed successfully for ${user.email} (SHA-256 verified).",
                        severity = "INFO"
                    )
                )
                return Pair(true, "Email verified successfully! Welcome to VML Recover.")
            }
            is EmailVerificationService.VerifyResult.InvalidCode -> {
                return Pair(false, "Invalid verification code. Attempts remaining: ${result.attemptsRemaining}/${EmailVerificationService.MAX_ATTEMPTS}.")
            }
            is EmailVerificationService.VerifyResult.Expired -> {
                return Pair(false, "Verification code has expired (valid for 10 minutes). Please click Resend Code.")
            }
            is EmailVerificationService.VerifyResult.TooManyAttempts -> {
                return Pair(false, "Too many failed attempts. Code has been invalidated for security. Please request a new code.")
            }
            is EmailVerificationService.VerifyResult.NotFound -> {
                val expected = user.emailVerificationCode
                if (expected != null && SecurityUtils.verifyCodeConstantTime(code, expected)) {
                    database.userDao().markEmailVerified(userId)
                    return Pair(true, "Email verified successfully!")
                }
                return Pair(false, "No active verification code found for this account. Please click Resend Code.")
            }
        }
    }

    suspend fun resendVerificationCode(userId: String): Pair<String?, String> {
        val user = database.userDao().getUserById(userId) ?: return Pair(null, "User not found.")
        when (val sendResult = emailService.sendCode(user.email, user.fullName)) {
            is EmailVerificationService.SendCodeResult.Success -> {
                smsService.dispatchEmailVerification(user.email, user.fullName, sendResult.code)
                database.auditLogDao().insertLog(
                    AuditLogEntity(
                        userId = user.userId,
                        userName = user.fullName,
                        action = "VERIFICATION_CODE_RESENT",
                        details = "New verification security token dispatched to ${user.email} (expires in 10 mins).",
                        severity = "INFO"
                    )
                )
                return Pair(sendResult.code, sendResult.message)
            }
            is EmailVerificationService.SendCodeResult.RateLimited -> {
                return Pair(null, "Please wait ${sendResult.waitSeconds} seconds before requesting another code.")
            }
            is EmailVerificationService.SendCodeResult.InvalidEmail -> {
                return Pair(null, sendResult.message)
            }
            is EmailVerificationService.SendCodeResult.Error -> {
                return Pair(null, sendResult.message)
            }
        }
    }

    suspend fun registerMemberByAdmin(
        adminUser: UserEntity,
        fullName: String,
        email: String,
        plainPassword: String,
        role: String,
        department: String,
        phone: String,
        organization: String = "VML Recover Ltd"
    ): UserEntity {
        if (!adminUser.role.equals("ADMIN", ignoreCase = true)) {
            throw UnauthorizedAccessException("Permission Denied: Only System Administrators can register new members to VML Recover.")
        }

        val cleanEmail = SecurityUtils.sanitizeInput(email.trim().lowercase())
        val existing = database.userDao().getUserByEmail(cleanEmail)
        if (existing != null) {
            throw IllegalArgumentException("A member is already registered with email: $cleanEmail")
        }

        if (plainPassword.length < 6) {
            throw IllegalArgumentException("Temporary password must be at least 6 characters.")
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(plainPassword, salt)
        val sendResult = emailService.sendCode(cleanEmail, fullName)
        val verificationCode = if (sendResult is EmailVerificationService.SendCodeResult.Success) {
            sendResult.code
        } else {
            SecurityUtils.generateEmailVerificationCode()
        }
        val newUserId = "USR-" + UUID.randomUUID().toString().take(6).uppercase()

        val newUser = UserEntity(
            userId = newUserId,
            email = cleanEmail,
            password = hash,
            fullName = SecurityUtils.sanitizeInput(fullName),
            role = role,
            department = department,
            phoneNumber = phone,
            organization = organization,
            lastLoginAt = 0L,
            maskedUsername = SecurityUtils.maskEmail(cleanEmail),
            maskedPassword = "●●●●●●●●",
            isLocked = false,
            failedAttempts = 0,
            passwordSalt = salt,
            isEmailVerified = false,
            emailVerificationCode = verificationCode,
            isFirstLogin = true
        )

        database.userDao().insertUser(newUser)
        smsService.dispatchEmailVerification(cleanEmail, newUser.fullName, verificationCode)

        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = adminUser.userId,
                userName = adminUser.fullName,
                action = "MEMBER_REGISTERED",
                details = "Admin registered member ${newUser.fullName} (${newUser.email}, Role: $role). Verification code issued.",
                severity = "INFO"
            )
        )

        firebaseService.pushUserRegistration(newUser)

        return newUser
    }

    suspend fun updateUserPassword(userId: String, newPass: String) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPass, salt)
        database.userDao().updatePassword(userId, hash, salt)
        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = userId,
                userName = "System",
                action = "PASSWORD_RESET",
                details = "User password updated with cryptographic PBKDF2 salting.",
                severity = "WARNING"
            )
        )
    }

    suspend fun lockUser(userId: String, reason: String) {
        database.userDao().lockUser(userId, reason, System.currentTimeMillis())
        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = userId,
                userName = "Admin",
                action = "ADMIN_LOCK_USER",
                details = "User account locked: $reason",
                severity = "WARNING"
            )
        )
    }

    suspend fun unlockUser(userId: String) {
        database.userDao().unlockUser(userId)
        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = userId,
                userName = "Admin",
                action = "ADMIN_UNLOCK_USER",
                details = "User account unlocked and failed attempts reset.",
                severity = "INFO"
            )
        )
    }

    suspend fun unlockAllAccounts() {
        database.userDao().unlockAllUsers()
    }

    suspend fun deleteUser(userId: String) {
        database.userDao().deleteUserById(userId)
    }

    suspend fun recordPayment(payment: PaymentTransactionEntity): Long {
        val id = database.paymentDao().insertPayment(payment)
        val case = database.caseDao().getCaseById(payment.caseId)
        if (case != null) {
            val newRecovered = case.recoveredAmount + payment.amount
            val newBalance = (case.totalAmount - newRecovered).coerceAtLeast(0.0)
            val newStatus = if (newBalance <= 0) "CLOSED" else case.status
            database.caseDao().updateBalanceAndStatus(case.caseId, newRecovered, newBalance, newStatus)
            database.timelineDao().insertAction(
                TimelineActionEntity(
                    caseId = case.caseId,
                    actionType = "PAYMENT",
                    description = "Payment of UGX ${String.format("%,.0f", payment.amount)} recorded via ${payment.paymentChannel} (Ref: ${payment.referenceNumber})",
                    performedBy = payment.recordedBy
                )
            )
            firebaseService.pushPaymentRecord(payment.copy(paymentId = id))
            firebaseService.pushCaseUpdate(case.copy(recoveredAmount = newRecovered, balance = newBalance, status = newStatus))
        }
        return id
    }

    suspend fun recordPromise(promise: PromiseToPayEntity) {
        database.promiseDao().insertPromise(promise)
        database.timelineDao().insertAction(
            TimelineActionEntity(
                caseId = promise.caseId,
                actionType = "PROMISE",
                description = "Promise to Pay UGX ${String.format("%,.0f", promise.promiseAmount)} recorded: ${promise.notes}",
                performedBy = promise.recordedBy
            )
        )
    }

    suspend fun updatePromiseStatus(promiseId: String, status: String) {
        database.promiseDao().updatePromiseStatus(promiseId, status)
    }

    suspend fun recordFieldVisit(visit: FieldVisitEntity) {
        database.fieldVisitDao().insertVisit(visit)
        database.timelineDao().insertAction(
            TimelineActionEntity(
                caseId = visit.caseId,
                actionType = "VISIT",
                description = "Field visit scheduled to ${visit.locationAddress} by ${visit.officerName}",
                performedBy = visit.officerName
            )
        )
    }

    suspend fun completeFieldVisit(visitId: String, outcome: String, found: Boolean, notes: String) {
        database.fieldVisitDao().completeVisit(
            visitId = visitId,
            status = "COMPLETED",
            completedDate = System.currentTimeMillis(),
            outcome = outcome,
            found = found,
            notes = notes
        )
    }

    suspend fun generateLegalDoc(doc: LegalDocumentEntity) {
        database.legalDocDao().insertDoc(doc)
        database.timelineDao().insertAction(
            TimelineActionEntity(
                caseId = doc.caseId,
                actionType = "LEGAL",
                description = "Legal document generated: ${doc.docTitle} (${doc.docType})",
                performedBy = doc.advocateName
            )
        )
    }

    suspend fun updateLegalStage(caseId: String, stage: String) {
        database.caseDao().updateLegalStage(caseId, stage)
    }

    suspend fun reassignCase(caseId: String, newOfficerId: String, newOfficerName: String, reason: String) {
        database.caseDao().reassignCase(caseId, newOfficerId, newOfficerName)
        database.timelineDao().insertAction(
            TimelineActionEntity(
                caseId = caseId,
                actionType = "REASSIGN",
                description = "Case reassigned to $newOfficerName. Reason: $reason",
                performedBy = "Operations Management"
            )
        )
    }

    suspend fun updateSystemConfig(config: SystemConfigEntity) {
        database.systemConfigDao().insertConfig(config)
    }

    suspend fun insertCase(caseEntity: CaseEntity) {
        database.caseDao().insertCase(caseEntity)
        firebaseService.pushCaseUpdate(caseEntity)
    }

    suspend fun updateCase(caseEntity: CaseEntity) {
        database.caseDao().updateCase(caseEntity)
        firebaseService.pushCaseUpdate(caseEntity)
    }

    suspend fun deleteCase(caseId: String) {
        database.caseDao().deleteCaseById(caseId)
    }

    suspend fun deletePayment(paymentId: Long) {
        database.paymentDao().deletePaymentById(paymentId)
    }

    suspend fun purgeDemoData(adminUser: UserEntity, newAdminEmail: String? = null): Boolean {
        database.caseDao().clearAllCases()
        database.paymentDao().clearAllPayments()
        database.promiseDao().clearAllPromises()
        database.fieldVisitDao().clearAllVisits()
        database.legalDocDao().clearAllDocs()
        database.timelineDao().clearAllTimeline()
        database.smsNotificationDao().clearAll()

        database.userDao().clearNonAdminUsers()

        if (!newAdminEmail.isNullOrBlank()) {
            val cleanEmail = newAdminEmail.trim().lowercase()
            val updatedAdmin = adminUser.copy(
                email = cleanEmail,
                maskedUsername = SecurityUtils.maskEmail(cleanEmail)
            )
            database.userDao().updateUser(updatedAdmin)
        }

        firebaseService.purgeDemoDataFromFirebase()

        database.auditLogDao().insertLog(
            AuditLogEntity(
                userId = adminUser.userId,
                userName = adminUser.fullName,
                action = "SYSTEM_INITIALIZED_PRODUCTION",
                details = "All sample cases, demo payments, and mock accounts purged by Administrator. System transitioned to clean Production Mode.",
                severity = "CRITICAL"
            )
        )
        return true
    }
}
