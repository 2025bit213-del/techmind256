package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :identifier OR userId = :identifier LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE users SET isLocked = 1, lockReason = :reason, lockedAt = :timestamp WHERE userId = :userId")
    suspend fun lockUser(userId: String, reason: String, timestamp: Long)

    @Query("UPDATE users SET isLocked = 0, failedAttempts = 0, lockReason = NULL, lockedAt = NULL WHERE userId = :userId")
    suspend fun unlockUser(userId: String)

    @Query("UPDATE users SET isLocked = 0, failedAttempts = 0, lockReason = NULL, lockedAt = NULL")
    suspend fun unlockAllUsers()

    @Query("UPDATE users SET failedAttempts = :attempts WHERE userId = :userId")
    suspend fun updateFailedAttempts(userId: String, attempts: Int)

    @Query("UPDATE users SET lastLoginAt = :timestamp, failedAttempts = 0 WHERE userId = :userId")
    suspend fun recordSuccessfulLogin(userId: String, timestamp: Long)

    @Query("UPDATE users SET password = :passwordHash, passwordSalt = :passwordSalt WHERE userId = :userId")
    suspend fun updatePassword(userId: String, passwordHash: String, passwordSalt: String)

    @Query("UPDATE users SET isEmailVerified = 1, isFirstLogin = 0, emailVerificationCode = NULL WHERE userId = :userId")
    suspend fun markEmailVerified(userId: String)

    @Query("UPDATE users SET emailVerificationCode = :newCode WHERE userId = :userId")
    suspend fun updateVerificationCode(userId: String, newCode: String)

    @Query("DELETE FROM users WHERE role != 'ADMIN'")
    suspend fun clearNonAdminUsers()
}

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY ageingDays DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE caseId = :caseId LIMIT 1")
    fun getCaseByIdFlow(caseId: String): Flow<CaseEntity?>

    @Query("SELECT * FROM cases WHERE caseId = :caseId LIMIT 1")
    suspend fun getCaseById(caseId: String): CaseEntity?

    @Query("SELECT * FROM cases WHERE assignedOfficerId = :officerId")
    fun getCasesForOfficer(officerId: String): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE clientName = :clientName")
    fun getCasesForClient(clientName: String): Flow<List<CaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: CaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<CaseEntity>)

    @Update
    suspend fun updateCase(caseEntity: CaseEntity)

    @Query("DELETE FROM cases WHERE caseId = :caseId")
    suspend fun deleteCaseById(caseId: String)

    @Query("UPDATE cases SET recoveredAmount = :recovered, balance = :balance, status = :status WHERE caseId = :caseId")
    suspend fun updateBalanceAndStatus(caseId: String, recovered: Double, balance: Double, status: String)

    @Query("UPDATE cases SET assignedOfficerId = :officerId, assignedOfficerName = :officerName WHERE caseId = :caseId")
    suspend fun reassignCase(caseId: String, officerId: String, officerName: String)

    @Query("UPDATE cases SET legalStage = :stage WHERE caseId = :caseId")
    suspend fun updateLegalStage(caseId: String, stage: String)

    @Query("DELETE FROM cases")
    suspend fun clearAllCases()
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_transactions ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE caseId = :caseId ORDER BY paymentDate DESC")
    fun getPaymentsForCase(caseId: String): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentTransactionEntity>)

    @Query("DELETE FROM payment_transactions WHERE paymentId = :paymentId")
    suspend fun deletePaymentById(paymentId: Long)

    @Query("UPDATE payment_transactions SET status = :status, verifiedAt = :verifiedAt WHERE paymentId = :paymentId")
    suspend fun updatePaymentStatus(paymentId: Long, status: String, verifiedAt: Long?)

    @Query("DELETE FROM payment_transactions")
    suspend fun clearAllPayments()
}

@Dao
interface PromiseDao {
    @Query("SELECT * FROM promises_to_pay ORDER BY promiseDate ASC")
    fun getAllPromises(): Flow<List<PromiseToPayEntity>>

    @Query("SELECT * FROM promises_to_pay WHERE caseId = :caseId ORDER BY promiseDate DESC")
    fun getPromisesForCase(caseId: String): Flow<List<PromiseToPayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromise(promise: PromiseToPayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromises(promises: List<PromiseToPayEntity>)

    @Query("UPDATE promises_to_pay SET status = :status WHERE promiseId = :promiseId")
    suspend fun updatePromiseStatus(promiseId: String, status: String)

    @Query("DELETE FROM promises_to_pay")
    suspend fun clearAllPromises()
}

@Dao
interface FieldVisitDao {
    @Query("SELECT * FROM field_visits ORDER BY scheduledDate DESC")
    fun getAllVisits(): Flow<List<FieldVisitEntity>>

    @Query("SELECT * FROM field_visits WHERE caseId = :caseId ORDER BY scheduledDate DESC")
    fun getVisitsForCase(caseId: String): Flow<List<FieldVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: FieldVisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<FieldVisitEntity>)

    @Query("UPDATE field_visits SET status = :status, completedDate = :completedDate, outcome = :outcome, debtorFound = :found, notes = :notes WHERE visitId = :visitId")
    suspend fun completeVisit(visitId: String, status: String, completedDate: Long, outcome: String, found: Boolean, notes: String)

    @Query("DELETE FROM field_visits")
    suspend fun clearAllVisits()
}

@Dao
interface LegalDocDao {
    @Query("SELECT * FROM legal_documents ORDER BY generatedDate DESC")
    fun getAllLegalDocs(): Flow<List<LegalDocumentEntity>>

    @Query("SELECT * FROM legal_documents WHERE caseId = :caseId ORDER BY generatedDate DESC")
    fun getDocsForCase(caseId: String): Flow<List<LegalDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: LegalDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocs(docs: List<LegalDocumentEntity>)

    @Query("DELETE FROM legal_documents")
    suspend fun clearAllDocs()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_configs WHERE configId = :configId LIMIT 1")
    fun getConfigFlow(configId: String = "DEFAULT_CONFIG"): Flow<SystemConfigEntity?>

    @Query("SELECT * FROM system_configs WHERE configId = :configId LIMIT 1")
    suspend fun getConfig(configId: String = "DEFAULT_CONFIG"): SystemConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SystemConfigEntity)
}

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_actions ORDER BY timestamp DESC")
    fun getAllTimeline(): Flow<List<TimelineActionEntity>>

    @Query("SELECT * FROM timeline_actions WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getTimelineForCase(caseId: String): Flow<List<TimelineActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: TimelineActionEntity)

    @Query("DELETE FROM timeline_actions")
    suspend fun clearAllTimeline()
}

@Dao
interface SmsNotificationDao {
    @Query("SELECT * FROM sms_notifications ORDER BY sentAt DESC")
    fun getAllSms(): Flow<List<SmsNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSms(sms: SmsNotificationEntity)

    @Query("DELETE FROM sms_notifications")
    suspend fun clearAll()
}

@Dao
interface PaymentPlanDao {
    @Query("SELECT * FROM payment_plans WHERE caseId = :caseId LIMIT 1")
    fun getPlanForCase(caseId: String): Flow<PaymentPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PaymentPlanEntity)
}
