package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLogEntity
import com.example.data.model.CaseEntity
import com.example.data.model.FieldVisitEntity
import com.example.data.model.LegalDocumentEntity
import com.example.data.model.PaymentPlanEntity
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.PromiseToPayEntity
import com.example.data.model.SmsNotificationEntity
import com.example.data.model.SystemConfigEntity
import com.example.data.model.TimelineActionEntity
import com.example.data.model.UserEntity
import com.example.data.model.VmlSecurityException
import com.example.repository.VmlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole(val displayName: String, val subtitle: String) {
    ADMIN("Administrator", "Complete System Control & Master Oversight"),
    DIRECTOR("Director / Founder", "Portfolio & Performance Analytics"),
    COLLECTION_MANAGER("Collection Manager", "Case Allocation & Supervisory Oversight"),
    COLLECTION_OFFICER("Collection Officer", "Calls, Visits, Promises & Payments"),
    LEGAL_OFFICER("Legal Officer", "Legal Escalation & Litigation"),
    FINANCE_OFFICER("Finance Officer", "Assigned Payments & Reconciliation"),
    CLIENT("Client", "Client Portal - View All Debtors & Statements")
}

enum class NavigationScreen {
    DASHBOARD,
    CASES,
    CASE_DETAIL,
    CLIENT_PORTAL,
    ADMIN_SYSTEM_CONTROL,
    ADMIN_ANALYTICS,
    MANAGER_ALLOCATION,
    MANAGER_SUPERVISION,
    LEGAL_HUB,
    PROMISES,
    FIELD_VISITS,
    FINANCE_RECONCILIATION
}

class VmlViewModel(
    private val repository: VmlRepository
) : ViewModel() {

    // Current Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(NavigationScreen.DASHBOARD)
    val currentScreen: StateFlow<NavigationScreen> = _currentScreen.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _selectedCaseId = MutableStateFlow<String?>("VML-2025-001")
    val selectedCaseId: StateFlow<String?> = _selectedCaseId.asStateFlow()

    // Status Message / Snackbar
    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    // Database Flows
    val allCases: StateFlow<List<CaseEntity>> = repository.allCasesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentTransactionEntity>> = repository.allPaymentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPromises: StateFlow<List<PromiseToPayEntity>> = repository.allPromisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisits: StateFlow<List<FieldVisitEntity>> = repository.allVisitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLegalDocs: StateFlow<List<LegalDocumentEntity>> = repository.allLegalDocsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemConfig: StateFlow<SystemConfigEntity?> = repository.systemConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSms: StateFlow<List<SmsNotificationEntity>> = repository.allSmsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Case Flow
    val selectedCase: StateFlow<CaseEntity?> = combine(allCases, _selectedCaseId) { cases, id ->
        cases.find { it.caseId == id } ?: cases.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // CLIENT PORTAL SORTING & FILTERING STATES
    val clientSearchQuery = MutableStateFlow("")
    val clientBankFilter = MutableStateFlow("ALL") // ALL or Bank Name
    val clientAmountSort = MutableStateFlow("NONE") // NONE, HIGH_TO_LOW, LOW_TO_HIGH
    val clientAgingFilter = MutableStateFlow("ALL") // ALL, 30_DAYS, 60_DAYS, 90_DAYS, 180_PLUS
    val clientSectorFilter = MutableStateFlow("ALL") // ALL, Trade & Retail, Logistics & Transport, etc.
    val clientLegalStageFilter = MutableStateFlow("ALL") // ALL, PRE_LEGAL, DEMAND_LETTER, SUMMONS_ISSUED, etc.
    val clientDebtorTypeFilter = MutableStateFlow("ALL") // ALL, INDIVIDUAL, COMPANY

    val filteredClientCases: StateFlow<List<CaseEntity>> = combine(
        allCases,
        clientSearchQuery,
        clientBankFilter,
        clientAmountSort,
        clientAgingFilter,
        clientSectorFilter,
        clientLegalStageFilter
    ) { cases, query, bank, amountSort, aging, sector, legalStage ->
        var list = cases.filter { caseItem ->
            val matchesQuery = query.isBlank() ||
                    caseItem.debtorName.contains(query, ignoreCase = true) ||
                    caseItem.caseId.contains(query, ignoreCase = true) ||
                    caseItem.creditorName.contains(query, ignoreCase = true)

            val matchesBank = bank == "ALL" || caseItem.creditorName.equals(bank, ignoreCase = true)
            val matchesAging = aging == "ALL" || caseItem.ageingCategory.equals(aging, ignoreCase = true)
            val matchesSector = sector == "ALL" || caseItem.sectorOfWork.equals(sector, ignoreCase = true)
            val matchesLegal = legalStage == "ALL" || caseItem.legalStage.equals(legalStage, ignoreCase = true)

            matchesQuery && matchesBank && matchesAging && matchesSector && matchesLegal
        }

        // Debtor Type filter check
        val debtorTypeVal = clientDebtorTypeFilter.value
        if (debtorTypeVal != "ALL") {
            list = list.filter { it.debtorType.equals(debtorTypeVal, ignoreCase = true) }
        }

        // Amount Sorting
        when (amountSort) {
            "HIGH_TO_LOW" -> list.sortedByDescending { it.principalAmount + it.interestAmount }
            "LOW_TO_HIGH" -> list.sortedBy { it.principalAmount + it.interestAmount }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // NAVIGATION & SESSION
    fun navigateTo(screen: NavigationScreen) {
        _currentScreen.value = screen
    }

    fun selectCase(caseId: String) {
        _selectedCaseId.value = caseId
        _currentScreen.value = NavigationScreen.CASE_DETAIL
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.logAudit("LOGOUT", user.userId, user.role, "SESSION", user.userId, "User logged out.")
            }
        }
        _currentUser.value = null
        _loginError.value = null
        _currentScreen.value = NavigationScreen.DASHBOARD
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _loginError.value = null
            try {
                val user = repository.authenticate(identifier, password)
                _currentUser.value = user
                // Direct client role to Client Portal
                if (user.role == "CLIENT") {
                    _currentScreen.value = NavigationScreen.CLIENT_PORTAL
                } else {
                    _currentScreen.value = NavigationScreen.DASHBOARD
                }
            } catch (e: VmlSecurityException) {
                _loginError.value = e.message
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Authentication failed."
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    // Quick Login for testing different personas
    fun quickLoginAsRole(role: UserRole) {
        val email = when (role) {
            UserRole.ADMIN -> "admin@vmlrecover.ug"
            UserRole.DIRECTOR -> "director@vmlrecover.ug"
            UserRole.COLLECTION_MANAGER -> "manager@vmlrecover.ug"
            UserRole.COLLECTION_OFFICER -> "collector@vmlrecover.ug"
            UserRole.LEGAL_OFFICER -> "legal@vmlrecover.ug"
            UserRole.FINANCE_OFFICER -> "finance@vmlrecover.ug"
            UserRole.CLIENT -> "client@stanbic.co"
        }
        login(email, "admin")
    }

    // PERMISSION-BASED CASE ACTIONS
    fun canAddClient(): Boolean {
        val role = _currentUser.value?.role ?: return false
        return repository.canAddClient(role)
    }

    fun isAdmin(): Boolean {
        val role = _currentUser.value?.role ?: return false
        return repository.isAdmin(role)
    }

    fun addCase(caseItem: CaseEntity, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null || !canAddClient()) {
            onComplete(false, "Permission denied: Only Admin or Collection Manager can add clients/debtors.")
            return
        }

        viewModelScope.launch {
            val result = repository.addCase(caseItem, user.userId, user.role)
            result.onSuccess {
                _actionFeedback.value = "New client/debtor '${caseItem.debtorName}' added successfully."
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to add case")
            }
        }
    }

    // DELETION REQUIRES ADMIN PERMISSION
    fun deleteCase(caseId: String, adminPasswordAttempt: String, reason: String, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "Please log in.")
            return
        }

        // If user is not admin, they must provide admin authorization password
        if (user.role != "ADMIN" && adminPasswordAttempt.trim() != "admin") {
            onComplete(false, "Administrator permission required. Invalid Admin authorization credentials.")
            return
        }

        viewModelScope.launch {
            val result = repository.deleteCase(caseId, user.userId, "ADMIN", reason)
            result.onSuccess {
                _actionFeedback.value = "Case $caseId permanently deleted under Admin authorization."
                if (_selectedCaseId.value == caseId) {
                    _selectedCaseId.value = null
                    _currentScreen.value = NavigationScreen.CASES
                }
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to delete case")
            }
        }
    }

    // PAYMENT RECORDING & DELETION
    fun recordPayment(
        caseId: String,
        amount: Double,
        channel: String,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "Please log in to record payments.")
            return
        }

        viewModelScope.launch {
            val result = repository.recordPayment(
                caseId = caseId,
                amount = amount,
                channel = channel,
                recordedBy = "${user.fullName} (${user.role})",
                notes = notes,
                actorUserId = user.userId,
                actorRole = user.role
            )
            result.onSuccess {
                _actionFeedback.value = "Payment of UGX $amount recorded. Receipt #${it.receiptNumber}"
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to process payment")
            }
        }
    }

    fun deletePayment(
        paymentId: Long,
        adminPasswordAttempt: String,
        reason: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "Please log in.")
            return
        }

        if (user.role != "ADMIN" && adminPasswordAttempt.trim() != "admin") {
            onComplete(false, "Administrator permission required. Invalid Admin authorization.")
            return
        }

        viewModelScope.launch {
            val result = repository.deletePayment(paymentId, user.userId, "ADMIN", reason)
            result.onSuccess {
                _actionFeedback.value = "Payment record reversed and deleted under Admin authorization."
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Deletion failed")
            }
        }
    }

    // ADMIN CONTROLS
    fun unlockUser(targetUserId: String, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null || user.role != "ADMIN") {
            onComplete(false, "Only Administrator can unlock accounts.")
            return
        }

        viewModelScope.launch {
            val result = repository.unlockUser(targetUserId, user.userId, user.role)
            result.onSuccess {
                _actionFeedback.value = "User account $targetUserId unlocked successfully."
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to unlock user")
            }
        }
    }

    fun toggleSystemLockdown(reason: String, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null || user.role != "ADMIN") {
            onComplete(false, "Only Administrator can toggle system lockdown.")
            return
        }

        val currentLock = systemConfig.value?.isSystemLocked ?: false
        val newLockState = !currentLock

        viewModelScope.launch {
            val result = repository.toggleSystemLockdown(user.userId, user.role, newLockState, reason)
            result.onSuccess {
                _actionFeedback.value = if (newLockState) "SYSTEM LOCKED: Only Admin can log in." else "SYSTEM UNLOCKED: All roles restored."
                onComplete(true, "Success")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to toggle lockdown")
            }
        }
    }

    // PTP & FIELD VISITS
    fun addPromise(caseId: String, debtorName: String, amount: Double, date: String, notes: String) {
        val user = _currentUser.value ?: return
        val ptp = PromiseToPayEntity(
            caseId = caseId,
            debtorName = debtorName,
            promisedAmount = amount,
            promiseDate = date,
            notes = notes,
            officerName = user.fullName
        )
        viewModelScope.launch {
            repository.addPromise(ptp, user.userId, user.role)
            _actionFeedback.value = "Promise to Pay recorded for $debtorName."
        }
    }

    fun addVisit(caseId: String, debtorName: String, date: String, address: String, outcome: String) {
        val user = _currentUser.value ?: return
        val visit = FieldVisitEntity(
            caseId = caseId,
            debtorName = debtorName,
            scheduledDate = date,
            address = address,
            officerName = user.fullName,
            outcome = outcome,
            complianceVerified = true
        )
        viewModelScope.launch {
            repository.addVisit(visit, user.userId, user.role)
            _actionFeedback.value = "Field visit record saved for $debtorName."
        }
    }

    fun addLegalDocument(caseId: String, docType: String, title: String, summary: String) {
        val user = _currentUser.value ?: return
        val doc = LegalDocumentEntity(
            caseId = caseId,
            docType = docType,
            title = title,
            contentSummary = summary,
            generatedDate = "Today",
            assignedAdvocate = user.fullName
        )
        viewModelScope.launch {
            repository.addLegalDocument(doc, user.userId, user.role)
            _actionFeedback.value = "Legal document '$title' created."
        }
    }

    // PRINT DOCUMENT HELPER
    fun printDocument(title: String, content: String) {
        _actionFeedback.value = "Printing '$title' sent to system printer."
    }
}

class VmlViewModelFactory(private val repository: VmlRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VmlViewModel::class.java)) {
            return VmlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
