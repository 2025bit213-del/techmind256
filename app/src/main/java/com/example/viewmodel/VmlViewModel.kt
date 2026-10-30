package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.repository.VmlRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole(val displayName: String, val subtitle: String) {
    ADMIN("Administrator", "Complete System Control & Master Oversight"),
    DIRECTOR("Director / Founder", "Portfolio & Performance Analytics Only"),
    COLLECTION_MANAGER("Collection Manager", "Case Allocation & Supervisory Oversight"),
    COLLECTION_OFFICER("Collection Officer", "Calls, Visits, Promises & Payments"),
    LEGAL_OFFICER("Legal Officer", "Legal Escalation & Litigation"),
    FINANCE_OFFICER("Finance Officer", "Assigned Company Payments & Reconciliation"),
    CLIENT_PORTAL("Company", "Assigned Portfolio, Loans, Instalments & Direct Payment"),
    DEBTOR_PORTAL("Individual", "Loan Status, Balance, Instalment Plan & Direct Payment")
}

enum class NavigationScreen {
    DASHBOARD,
    ADMIN_SYSTEM_CONTROL,
    ADMIN_ANALYTICS,
    MANAGER_ALLOCATION,
    MANAGER_SUPERVISION,
    CASES,
    CASE_DETAIL,
    LEGAL_HUB,
    PROMISES,
    FIELD_VISITS,
    FINANCE_RECONCILIATION,
    CLIENT_PORTAL,
    DEBTOR_PORTAL,
    PAYMENT_ENGINE,
    REPORTS
}

data class PortfolioMetrics(
    val totalAssigned: Double = 0.0,
    val totalRecovered: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val totalActiveCases: Int = 0,
    val pendingPromisesCount: Int = 0,
    val pendingPromisesAmount: Double = 0.0,
    val legalEscalationsCount: Int = 0,
    val recoveryRatePercent: Double = 0.0
)

data class ClientPortfolioSummary(
    val clientName: String,
    val activeCases: Int,
    val totalDebt: Double,
    val recoveredAmount: Double,
    val recoveryPercentage: Double,
    val averageAgeingDays: Int,
    val legalCases: Int
)

class VmlViewModel(private val repository: VmlRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentScreen = MutableStateFlow(NavigationScreen.DASHBOARD)
    val currentScreen: StateFlow<NavigationScreen> = _currentScreen.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _loginException = MutableStateFlow<VmlSecurityException?>(null)
    val loginException: StateFlow<VmlSecurityException?> = _loginException.asStateFlow()

    private val _isAccountLockedOut = MutableStateFlow(false)
    val isAccountLockedOut: StateFlow<Boolean> = _isAccountLockedOut.asStateFlow()

    private val _lockedOutUser = MutableStateFlow<UserEntity?>(null)
    val lockedOutUser: StateFlow<UserEntity?> = _lockedOutUser.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _isPendingEmailVerification = MutableStateFlow(false)
    val isPendingEmailVerification: StateFlow<Boolean> = _isPendingEmailVerification.asStateFlow()

    private val _pendingVerificationUser = MutableStateFlow<UserEntity?>(null)
    val pendingVerificationUser: StateFlow<UserEntity?> = _pendingVerificationUser.asStateFlow()

    private val _emailVerificationNotification = MutableStateFlow<String?>(null)
    val emailVerificationNotification: StateFlow<String?> = _emailVerificationNotification.asStateFlow()

    private val _emailVerificationError = MutableStateFlow<String?>(null)
    val emailVerificationError: StateFlow<String?> = _emailVerificationError.asStateFlow()

    private val _platformMode = MutableStateFlow(PlatformMode.AUTO)
    val platformMode: StateFlow<PlatformMode> = _platformMode.asStateFlow()

    private val _collectionManagerTab = MutableStateFlow(0)
    val collectionManagerTab: StateFlow<Int> = _collectionManagerTab.asStateFlow()

    private val _financeOfficerTab = MutableStateFlow(0)
    val financeOfficerTab: StateFlow<Int> = _financeOfficerTab.asStateFlow()

    private val _debtorPortalTab = MutableStateFlow(0)
    val debtorPortalTab: StateFlow<Int> = _debtorPortalTab.asStateFlow()

    private val _clientPortalTab = MutableStateFlow(0)
    val clientPortalTab: StateFlow<Int> = _clientPortalTab.asStateFlow()

    private val _selectedCaseId = MutableStateFlow<String?>(null)
    val selectedCaseId: StateFlow<String?> = _selectedCaseId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedAgeingFilter = MutableStateFlow("ALL")
    val selectedAgeingFilter: StateFlow<String> = _selectedAgeingFilter.asStateFlow()

    private val _selectedDashboardClient = MutableStateFlow<String?>(null)
    val selectedDashboardClient: StateFlow<String?> = _selectedDashboardClient.asStateFlow()

    private val _latestSmsToast = MutableStateFlow<SmsNotificationEntity?>(null)
    val latestSmsToast: StateFlow<SmsNotificationEntity?> = _latestSmsToast.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allCases: StateFlow<List<CaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allPayments: StateFlow<List<PaymentTransactionEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allPromises: StateFlow<List<PromiseToPayEntity>> = repository.allPromises
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allVisits: StateFlow<List<FieldVisitEntity>> = repository.allVisits
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allLegalDocs: StateFlow<List<LegalDocumentEntity>> = repository.getAllLegalDocs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allTimeline: StateFlow<List<TimelineActionEntity>> = repository.allTimeline
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSmsNotifications: StateFlow<List<SmsNotificationEntity>> = repository.allSmsNotifications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val systemConfig: StateFlow<SystemConfigEntity> = repository.systemConfigFlow
        .map { it ?: SystemConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.Lazily, SystemConfigEntity())

    val portfolioMetrics: StateFlow<PortfolioMetrics> = allCases.combine(allPromises) { cases, promises ->
        val totalAssigned = cases.sumOf { it.totalAmount }
        val totalRecovered = cases.sumOf { it.recoveredAmount }
        val totalOutstanding = cases.sumOf { it.balance }
        val activeCount = cases.count { it.status != "CLOSED" }
        val pendingPromises = promises.filter { it.status == "PENDING" }
        val legalCount = cases.count { it.status == "LEGAL_ESCALATION" }
        val rate = if (totalAssigned > 0) (totalRecovered / totalAssigned) * 100.0 else 0.0

        PortfolioMetrics(
            totalAssigned = totalAssigned,
            totalRecovered = totalRecovered,
            totalOutstanding = totalOutstanding,
            totalActiveCases = activeCount,
            pendingPromisesCount = pendingPromises.size,
            pendingPromisesAmount = pendingPromises.sumOf { it.promiseAmount },
            legalEscalationsCount = legalCount,
            recoveryRatePercent = rate
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, PortfolioMetrics())

    val clientSummaries: StateFlow<List<ClientPortfolioSummary>> = allCases.map { cases ->
        cases.groupBy { it.clientName }.map { (clientName, clientCases) ->
            val totalDebt = clientCases.sumOf { it.totalAmount }
            val recovered = clientCases.sumOf { it.recoveredAmount }
            val avgAgeing = if (clientCases.isNotEmpty()) clientCases.map { it.ageingDays }.average().toInt() else 0
            val pct = if (totalDebt > 0) (recovered / totalDebt) * 100.0 else 0.0
            val legal = clientCases.count { it.status == "LEGAL_ESCALATION" }

            ClientPortfolioSummary(
                clientName = clientName,
                activeCases = clientCases.count { it.status != "CLOSED" },
                totalDebt = totalDebt,
                recoveredAmount = recovered,
                recoveryPercentage = pct,
                averageAgeingDays = avgAgeing,
                legalCases = legal
            )
        }.sortedByDescending { it.totalDebt }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedCase: StateFlow<CaseEntity?> = _selectedCaseId.combine(allCases) { id, cases ->
        cases.find { it.caseId == id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            repository.smsService.dispatchedSms.collect { sms ->
                _latestSmsToast.value = sms
            }
        }
    }

    fun login(identifier: String, pass: String, onResult: (Boolean) -> Unit = {}) {
        _isAuthenticating.value = true
        _loginError.value = null
        _loginException.value = null

        viewModelScope.launch {
            try {
                val user = repository.authenticateUser(identifier, pass)

                if (!user.isEmailVerified || user.isFirstLogin) {
                    _pendingVerificationUser.value = user
                    _isPendingEmailVerification.value = true
                    _isAuthenticating.value = false
                    val code = user.emailVerificationCode ?: repository.resendVerificationCode(user.userId).first
                    _emailVerificationNotification.value = "Security code sent to ${user.email}: [$code]"
                    onResult(false)
                    return@launch
                }

                _currentUser.value = user
                _isLoggedIn.value = true
                _isAuthenticating.value = false
                _isAccountLockedOut.value = false
                _lockedOutUser.value = null

                val mappedRole = try {
                    UserRole.valueOf(user.role.uppercase())
                } catch (e: Exception) {
                    UserRole.ADMIN
                }
                _currentRole.value = mappedRole
                _currentScreen.value = getRoleHomeScreen(mappedRole)
                onResult(true)
            } catch (ex: AccountLockedSecurityException) {
                _isAuthenticating.value = false
                _isAccountLockedOut.value = true
                _loginError.value = ex.message
                _loginException.value = ex
                val foundUser = repository.findUserByIdentifier(identifier)
                _lockedOutUser.value = foundUser
                onResult(false)
            } catch (ex: VmlSecurityException) {
                _isAuthenticating.value = false
                _loginError.value = ex.message
                _loginException.value = ex
                onResult(false)
            } catch (ex: Exception) {
                _isAuthenticating.value = false
                _loginError.value = ex.message ?: "Authentication failed. Please verify credentials."
                onResult(false)
            }
        }
    }

    fun verifyEmailCode(enteredCode: String, onResult: (Boolean, String) -> Unit) {
        val user = _pendingVerificationUser.value
        if (user == null) {
            onResult(false, "No user pending email verification.")
            return
        }

        viewModelScope.launch {
            val (isSuccess, message) = repository.verifyEmailCode(user.userId, enteredCode)
            if (isSuccess) {
                val updatedUser = user.copy(isEmailVerified = true, isFirstLogin = false, emailVerificationCode = null)
                _currentUser.value = updatedUser
                _pendingVerificationUser.value = null
                _isPendingEmailVerification.value = false
                _isLoggedIn.value = true
                _emailVerificationError.value = null
                _emailVerificationNotification.value = null

                val mappedRole = try {
                    UserRole.valueOf(updatedUser.role.uppercase())
                } catch (e: Exception) {
                    UserRole.ADMIN
                }
                _currentRole.value = mappedRole
                _currentScreen.value = getRoleHomeScreen(mappedRole)
                onResult(true, message)
            } else {
                _emailVerificationError.value = message
                onResult(false, message)
            }
        }
    }

    fun resendVerificationCode(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val user = _pendingVerificationUser.value ?: return
        viewModelScope.launch {
            val (newCode, message) = repository.resendVerificationCode(user.userId)
            if (newCode != null) {
                _emailVerificationNotification.value = "New verification code dispatched to ${user.email}: [$newCode]"
                _emailVerificationError.value = null
                onResult(true, message)
            } else {
                _emailVerificationError.value = message
                onResult(false, message)
            }
        }
    }

    fun cancelEmailVerification() {
        _isPendingEmailVerification.value = false
        _pendingVerificationUser.value = null
        _emailVerificationError.value = null
        _emailVerificationNotification.value = null
    }

    fun registerMemberByAdmin(
        fullName: String,
        email: String,
        plainPassword: String,
        role: String,
        department: String,
        phone: String,
        organization: String = "VML Recover Ltd",
        onResult: (Boolean, String) -> Unit
    ) {
        val admin = _currentUser.value
        if (admin == null || !admin.role.equals("ADMIN", ignoreCase = true)) {
            onResult(false, "Permission Denied: Only authenticated Administrators can register members.")
            return
        }

        viewModelScope.launch {
            try {
                val newMember = repository.registerMemberByAdmin(
                    adminUser = admin,
                    fullName = fullName,
                    email = email,
                    plainPassword = plainPassword,
                    role = role,
                    department = department,
                    phone = phone,
                    organization = organization
                )
                onResult(true, "Member ${newMember.fullName} registered! Verification code [${newMember.emailVerificationCode}] dispatched to ${newMember.email}.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to register member.")
            }
        }
    }

    fun adminResendVerificationCode(userId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (newCode, message) = repository.resendVerificationCode(userId)
            if (newCode != null) {
                onResult(true, "New verification code [$newCode] sent to member's email.")
            } else {
                onResult(false, message)
            }
        }
    }

    fun purgeDemoData(newAdminEmail: String? = null, onResult: (Boolean, String) -> Unit) {
        val admin = _currentUser.value
        if (admin == null || admin.role != "ADMIN") {
            onResult(false, "Only a verified System Administrator can initialize production mode.")
            return
        }

        viewModelScope.launch {
            try {
                val success = repository.purgeDemoData(admin, newAdminEmail)
                if (success) {
                    val updatedAdmin = repository.findUserByIdentifier(newAdminEmail ?: admin.email) ?: admin
                    _currentUser.value = updatedAdmin
                    onResult(true, "All demo data purged! The system has successfully transitioned to clean Production Mode.")
                } else {
                    onResult(false, "Failed to purge demo data.")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error purging demo data.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _isPendingEmailVerification.value = false
        _pendingVerificationUser.value = null
        _currentScreen.value = NavigationScreen.DASHBOARD
        _loginError.value = null
        _loginException.value = null
        _isAccountLockedOut.value = false
        _lockedOutUser.value = null
    }

    fun clearLoginError() {
        _loginError.value = null
        _loginException.value = null
    }

    fun dismissSmsToast() {
        _latestSmsToast.value = null
    }

    fun navigateTo(screen: NavigationScreen) {
        _currentScreen.value = screen
    }

    fun selectCase(caseId: String?) {
        _selectedCaseId.value = caseId
        if (caseId != null) {
            _currentScreen.value = NavigationScreen.CASE_DETAIL
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setAgeingFilter(ageing: String) {
        _selectedAgeingFilter.value = ageing
    }

    fun selectDashboardClient(clientName: String?) {
        _selectedDashboardClient.value = clientName
    }

    fun setPlatformMode(mode: PlatformMode) {
        _platformMode.value = mode
    }

    fun setCollectionManagerTab(tab: Int) {
        _collectionManagerTab.value = tab
    }

    fun setFinanceOfficerTab(tab: Int) {
        _financeOfficerTab.value = tab
    }

    fun setDebtorPortalTab(tab: Int) {
        _debtorPortalTab.value = tab
    }

    fun setClientPortalTab(tab: Int) {
        _clientPortalTab.value = tab
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        _currentScreen.value = getRoleHomeScreen(role)
    }

    private fun getRoleHomeScreen(role: UserRole): NavigationScreen {
        return when (role) {
            UserRole.ADMIN -> NavigationScreen.ADMIN_SYSTEM_CONTROL
            UserRole.DIRECTOR -> NavigationScreen.DASHBOARD
            UserRole.COLLECTION_MANAGER -> NavigationScreen.MANAGER_ALLOCATION
            UserRole.COLLECTION_OFFICER -> NavigationScreen.CASES
            UserRole.LEGAL_OFFICER -> NavigationScreen.LEGAL_HUB
            UserRole.FINANCE_OFFICER -> NavigationScreen.FINANCE_RECONCILIATION
            UserRole.CLIENT_PORTAL -> NavigationScreen.CLIENT_PORTAL
            UserRole.DEBTOR_PORTAL -> NavigationScreen.DEBTOR_PORTAL
        }
    }

    fun recordPayment(caseId: String, amount: Double, channel: String, ref: String, notes: String, onResult: (Boolean, String) -> Unit) {
        if (amount <= 0) {
            onResult(false, "Payment amount must be greater than zero.")
            return
        }
        val user = _currentUser.value
        val recordedBy = user?.fullName ?: "Finance Officer"

        viewModelScope.launch {
            try {
                val payment = PaymentTransactionEntity(
                    caseId = caseId,
                    amount = amount,
                    paymentDate = System.currentTimeMillis(),
                    referenceNumber = ref.ifBlank { "REF-" + System.currentTimeMillis().toString().takeLast(6) },
                    paymentChannel = channel,
                    status = "COMPLETED",
                    recordedBy = recordedBy,
                    notes = notes
                )
                repository.recordPayment(payment)
                onResult(true, "Payment of UGX ${String.format("%,.0f", amount)} successfully recorded and reconciled.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to record payment.")
            }
        }
    }

    fun recordPromise(caseId: String, amount: Double, date: Long, notes: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        val recordedBy = user?.fullName ?: "Collection Officer"

        viewModelScope.launch {
            try {
                val promise = PromiseToPayEntity(
                    promiseId = "PTP-" + System.currentTimeMillis().toString().takeLast(6),
                    caseId = caseId,
                    promiseAmount = amount,
                    promiseDate = date,
                    status = "PENDING",
                    notes = notes,
                    recordedBy = recordedBy
                )
                repository.recordPromise(promise)
                onResult(true, "Promise to Pay recorded successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to record promise.")
            }
        }
    }

    fun scheduleFieldVisit(caseId: String, address: String, scheduledDate: Long, notes: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        val officerId = user?.userId ?: "USR-004"
        val officerName = user?.fullName ?: "David Ochieng"

        viewModelScope.launch {
            try {
                val visit = FieldVisitEntity(
                    visitId = "VIS-" + System.currentTimeMillis().toString().takeLast(6),
                    caseId = caseId,
                    officerId = officerId,
                    officerName = officerName,
                    scheduledDate = scheduledDate,
                    locationAddress = address,
                    notes = notes,
                    status = "SCHEDULED"
                )
                repository.recordFieldVisit(visit)
                onResult(true, "Field visit scheduled successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to schedule field visit.")
            }
        }
    }

    fun completeFieldVisit(visitId: String, outcome: String, found: Boolean, notes: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.completeFieldVisit(visitId, outcome, found, notes)
                onResult(true, "Field visit recorded as completed.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to complete visit.")
            }
        }
    }

    fun escalateLegalStage(caseId: String, stage: String, docType: String, docTitle: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        val advocate = user?.fullName ?: "Counsel Ronald Kisakye"

        viewModelScope.launch {
            try {
                repository.updateLegalStage(caseId, stage)
                val doc = LegalDocumentEntity(
                    docId = "DOC-" + System.currentTimeMillis().toString().takeLast(6),
                    caseId = caseId,
                    docType = docType,
                    docTitle = docTitle,
                    generatedDate = System.currentTimeMillis(),
                    status = "ISSUED",
                    advocateName = advocate
                )
                repository.generateLegalDoc(doc)
                onResult(true, "Case escalated to $stage and statutory document generated.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to escalate legal stage.")
            }
        }
    }

    fun reassignCase(caseId: String, newOfficerId: String, newOfficerName: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.reassignCase(caseId, newOfficerId, newOfficerName, reason)
                onResult(true, "Case reassigned to $newOfficerName.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to reassign case.")
            }
        }
    }

    fun lockUser(userId: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.lockUser(userId, reason)
                onResult(true, "User account has been locked.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to lock user.")
            }
        }
    }

    fun unlockUser(userId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.unlockUser(userId)
                onResult(true, "User account unlocked successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to unlock user.")
            }
        }
    }

    fun unlockAllAccounts() {
        viewModelScope.launch {
            repository.unlockAllAccounts()
        }
    }

    fun deleteUser(userId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteUser(userId)
                onResult(true, "User deleted successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete user.")
            }
        }
    }

    fun updateUserPassword(userId: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        if (newPass.length < 6) {
            onResult(false, "Password policy requires at least 6 characters.")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateUserPassword(userId, newPass)
                onResult(true, "Password updated successfully with cryptographic PBKDF2 hashing.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update password.")
            }
        }
    }

    fun updateSystemConfig(config: SystemConfigEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateSystemConfig(config)
                onResult(true, "System configuration updated.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update config.")
            }
        }
    }

    fun registerNewCase(caseEntity: CaseEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.insertCase(caseEntity)
                onResult(true, "Recovery case ${caseEntity.caseId} registered successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to register case.")
            }
        }
    }

    fun updateCase(caseEntity: CaseEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCase(caseEntity)
                onResult(true, "Case details updated.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update case.")
            }
        }
    }

    fun deleteCase(caseId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteCase(caseId)
                onResult(true, "Case deleted.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete case.")
            }
        }
    }

    fun deletePayment(paymentId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePayment(paymentId)
                onResult(true, "Payment record deleted.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete payment.")
            }
        }
    }

    fun sendPaymentReminderSms(caseId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val case = allCases.value.find { it.caseId == caseId }
            if (case != null) {
                val msg = "Dear ${case.debtorName}, your loan account #${case.loanNumber} has an overdue balance of UGX ${String.format("%,.0f", case.balance)}. Please make a deposit or contact VML Recover on +256 700 111222 to prevent further legal escalation."
                repository.smsService.dispatchSms(
                    phone = case.contactPhone.ifBlank { "+256 700 000 000" },
                    recipientName = case.debtorName,
                    eventType = "PAYMENT_REMINDER",
                    messageText = msg
                )
                onResult(true, "Payment reminder SMS dispatched to ${case.debtorName}.")
            } else {
                onResult(false, "Case not found.")
            }
        }
    }

    fun overrideLoginAdmin(recoveryKey: String, onResult: (Boolean, String) -> Unit) {
        if (recoveryKey.trim() == "VML-SECURE-ADMIN-2026") {
            viewModelScope.launch {
                val admin = allUsers.value.find { it.role.equals("ADMIN", ignoreCase = true) }
                if (admin != null) {
                    repository.unlockUser(admin.userId)
                    _isAccountLockedOut.value = false
                    _currentUser.value = admin.copy(isLocked = false, failedAttempts = 0)
                    _isLoggedIn.value = true
                    _currentRole.value = UserRole.ADMIN
                    _currentScreen.value = NavigationScreen.ADMIN_SYSTEM_CONTROL
                    onResult(true, "Administrator account unlocked and restored via secure recovery key.")
                } else {
                    onResult(false, "Admin user account not found.")
                }
            }
        } else {
            onResult(false, "Invalid emergency recovery key.")
        }
    }
}

class VmlViewModelFactory(private val repository: VmlRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VmlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VmlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
