package com.example.data

import com.example.data.model.*
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SampleDataProvider {
    private val seedMutex = Mutex()

    private fun createHashedUser(
        userId: String,
        email: String,
        plainPassword: String,
        fullName: String,
        role: String,
        department: String,
        phone: String,
        organization: String = "VML Recover Ltd",
        isEmailVerified: Boolean = true,
        verificationCode: String? = null,
        isFirstLogin: Boolean = false
    ): UserEntity {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(plainPassword, salt)
        val maskedUser = SecurityUtils.maskEmail(email)
        return UserEntity(
            userId = userId,
            email = email,
            password = hash,
            fullName = fullName,
            role = role,
            department = department,
            phoneNumber = phone,
            organization = organization,
            lastLoginAt = System.currentTimeMillis() - 86400000L,
            maskedUsername = maskedUser,
            maskedPassword = "●●●●●●●●",
            isLocked = false,
            failedAttempts = 0,
            passwordSalt = salt,
            isEmailVerified = isEmailVerified,
            emailVerificationCode = verificationCode,
            isFirstLogin = isFirstLogin
        )
    }

    suspend fun populateInitialData(db: AppDatabase) {
        seedMutex.withLock {
            val existingConfig = db.systemConfigDao().getConfig()
            if (existingConfig != null) return

            val config = SystemConfigEntity(
                configId = "DEFAULT_CONFIG",
                interestRatePolicy = 18.5,
                autoEscalateDays = 90,
                maxLoginAttempts = 5,
                allowOfflineMode = true,
                maintenanceMode = false,
                emergencyAdminLocked = false,
                organizationName = "VML Recover Ltd",
                emailVerificationRequired = true
            )
            db.systemConfigDao().insertConfig(config)

            val users = listOf(
                createHashedUser(
                    userId = "USR-001",
                    email = "admin@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "Patrick Mukwaya",
                    role = "ADMIN",
                    department = "Executive Management",
                    phone = "+256 772 100 200",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-002",
                    email = "director@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "Hon. Moses Byaruhanga",
                    role = "DIRECTOR",
                    department = "Board of Directors",
                    phone = "+256 701 234 567",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-003",
                    email = "manager@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "Sarah Nalubega",
                    role = "COLLECTION_MANAGER",
                    department = "Operations & Strategy",
                    phone = "+256 752 345 678",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-004",
                    email = "officer@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "David Ochieng",
                    role = "COLLECTION_OFFICER",
                    department = "Field Recovery Team Alpha",
                    phone = "+256 782 456 789",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-005",
                    email = "legal@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "Counsel Ronald Kisakye",
                    role = "LEGAL_OFFICER",
                    department = "Litigation & Recoveries",
                    phone = "+256 776 567 890",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-006",
                    email = "finance@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "Grace Atuhaire",
                    role = "FINANCE_OFFICER",
                    department = "Accounts & Reconciliation",
                    phone = "+256 703 678 901",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                createHashedUser(
                    userId = "USR-007",
                    email = "client@centenary.co.ug",
                    plainPassword = "password",
                    fullName = "Centenary Rural Development Bank",
                    role = "CLIENT_PORTAL",
                    department = "Special Asset Management Unit",
                    phone = "+256 414 251 276",
                    organization = "Centenary Bank Uganda",
                    isEmailVerified = true,
                    isFirstLogin = false
                ),
                // Debtor user requiring first-time verification
                createHashedUser(
                    userId = "USR-008",
                    email = "debtor@vmlrecover.ug",
                    plainPassword = "password",
                    fullName = "John Bosco Mukasa",
                    role = "DEBTOR_PORTAL",
                    department = "Retail Debtor",
                    phone = "+256 782 999 000",
                    organization = "Individual Borrower",
                    isEmailVerified = false,
                    verificationCode = "482915",
                    isFirstLogin = true
                )
            )
            users.forEach { db.userDao().insertUser(it) }

            val cases = listOf(
                CaseEntity(
                    caseId = "REC-2026-001",
                    debtorName = "Godfrey Kasule Mukasa",
                    loanNumber = "LN-CENT-88321",
                    clientName = "Centenary Rural Development Bank",
                    totalAmount = 45000000.0,
                    recoveredAmount = 18500000.0,
                    balance = 26500000.0,
                    status = "ACTIVE",
                    ageingDays = 124,
                    assignedOfficerId = "USR-004",
                    assignedOfficerName = "David Ochieng",
                    nextAction = "Follow up Promise to Pay for UGX 5,000,000",
                    lastContactDate = System.currentTimeMillis() - 86400000L * 2,
                    collateral = "Land Title (Kyadondo Block 214 Plot 88, Kyanja)",
                    contactPhone = "+256 772 341 902",
                    nationalId = "CM84029104928A",
                    legalStage = "DEMAND_LETTER",
                    riskLevel = "HIGH"
                ),
                CaseEntity(
                    caseId = "REC-2026-002",
                    debtorName = "Hajat Rehema Nabukalu",
                    loanNumber = "LN-STB-44109",
                    clientName = "Stanbic Bank Uganda",
                    totalAmount = 28000000.0,
                    recoveredAmount = 22000000.0,
                    balance = 6000000.0,
                    status = "PENDING_PTP",
                    ageingDays = 45,
                    assignedOfficerId = "USR-004",
                    assignedOfficerName = "David Ochieng",
                    nextAction = "Verify bank transfer receipt of UGX 3,000,000",
                    lastContactDate = System.currentTimeMillis() - 86400000L * 1,
                    collateral = "Commercial Vehicle Logbook (Toyota Hiace UBF 219K)",
                    contactPhone = "+256 701 492 810",
                    nationalId = "CF91029482019B",
                    legalStage = "PRE_LEGAL",
                    riskLevel = "MEDIUM"
                ),
                CaseEntity(
                    caseId = "REC-2026-003",
                    debtorName = "Kampala Agro Enterprises Ltd",
                    loanNumber = "LN-DFCU-77120",
                    clientName = "DFCU Bank Uganda",
                    totalAmount = 120000000.0,
                    recoveredAmount = 35000000.0,
                    balance = 85000000.0,
                    status = "LEGAL_ESCALATION",
                    ageingDays = 210,
                    assignedOfficerId = "USR-005",
                    assignedOfficerName = "Counsel Ronald Kisakye",
                    nextAction = "File application for warrant of attachment in High Court Commercial Division",
                    lastContactDate = System.currentTimeMillis() - 86400000L * 5,
                    collateral = "Grain Processing Factory & Machinery, Kawempe",
                    contactPhone = "+256 752 901 823",
                    nationalId = "CO-REG-109482-KLA",
                    legalStage = "COURT_FILING",
                    riskLevel = "CRITICAL"
                ),
                CaseEntity(
                    caseId = "REC-2026-004",
                    debtorName = "Dr. Emmanuel Ssenyonjo",
                    loanNumber = "LN-EQT-39102",
                    clientName = "Equity Bank Uganda",
                    totalAmount = 15000000.0,
                    recoveredAmount = 15000000.0,
                    balance = 0.0,
                    status = "CLOSED",
                    ageingDays = 15,
                    assignedOfficerId = "USR-004",
                    assignedOfficerName = "David Ochieng",
                    nextAction = "Discharge certificate dispatched to client",
                    lastContactDate = System.currentTimeMillis() - 86400000L * 10,
                    collateral = "Medical Clinic Equipment, Mengo",
                    contactPhone = "+256 774 102 938",
                    nationalId = "CM78019283746C",
                    legalStage = "PRE_LEGAL",
                    riskLevel = "LOW"
                )
            )
            db.caseDao().insertCases(cases)

            val payments = listOf(
                PaymentTransactionEntity(
                    caseId = "REC-2026-001",
                    amount = 5000000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L * 4,
                    referenceNumber = "FT-20260408-CENT-991",
                    paymentChannel = "BANK_TRANSFER",
                    status = "RECONCILED",
                    recordedBy = "Grace Atuhaire",
                    verifiedAt = System.currentTimeMillis() - 86400000L * 3,
                    notes = "Instalment #3 received via CenteMobile Escrow"
                ),
                PaymentTransactionEntity(
                    caseId = "REC-2026-002",
                    amount = 7000000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L * 2,
                    referenceNumber = "MM-20260410-MTN-882",
                    paymentChannel = "MOBILE_MONEY",
                    status = "COMPLETED",
                    recordedBy = "David Ochieng",
                    verifiedAt = System.currentTimeMillis() - 86400000L * 1,
                    notes = "Direct debtor deposit into VML Recover Trust Account"
                )
            )
            db.paymentDao().insertPayments(payments)

            val promises = listOf(
                PromiseToPayEntity(
                    promiseId = "PTP-2026-001",
                    caseId = "REC-2026-001",
                    promiseAmount = 5000000.0,
                    promiseDate = System.currentTimeMillis() + 86400000L * 4,
                    status = "PENDING",
                    notes = "Agreed to pay upon sale of seasonal harvest",
                    recordedBy = "David Ochieng"
                )
            )
            db.promiseDao().insertPromises(promises)

            val visits = listOf(
                FieldVisitEntity(
                    visitId = "VIS-2026-001",
                    caseId = "REC-2026-001",
                    officerId = "USR-004",
                    officerName = "David Ochieng",
                    scheduledDate = System.currentTimeMillis() - 86400000L * 2,
                    completedDate = System.currentTimeMillis() - 86400000L * 2,
                    status = "COMPLETED",
                    outcome = "Debtor found on premises; negotiated restructuring plan",
                    locationAddress = "Kyanja Trading Centre, Plot 88, Kyadondo",
                    gpsCoordinates = "0.3891° N, 32.6102° E",
                    debtorFound = true,
                    notes = "Debtor requested 7 days extension; cooperative attitude"
                )
            )
            db.fieldVisitDao().insertVisits(visits)

            val docs = listOf(
                LegalDocumentEntity(
                    docId = "DOC-2026-001",
                    caseId = "REC-2026-003",
                    docType = "DEMAND_NOTICE",
                    docTitle = "Final Statutory Demand Notice Prior to High Court Action",
                    generatedDate = System.currentTimeMillis() - 86400000L * 6,
                    status = "SERVED",
                    advocateName = "Counsel Ronald Kisakye",
                    courtRef = "HCT-00-CC-CS-2026/0412"
                )
            )
            db.legalDocDao().insertDocs(docs)

            val initialAudit = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userId = "SYS-BOOT",
                userName = "VML Security Kernel",
                action = "SYSTEM_INITIALIZED",
                details = "VML Recover production schema initialized with cryptographic PBKDF2 salting and email verification policy.",
                severity = "INFO"
            )
            db.auditLogDao().insertLog(initialAudit)
        }
    }
}
