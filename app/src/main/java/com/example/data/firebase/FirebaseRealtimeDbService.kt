package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.model.CaseEntity
import com.example.data.model.PaymentTransactionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Service managing real-time data streaming with Firebase Realtime Database.
 * Connects the Android Studio architecture with cloud real-time updates.
 */
class FirebaseRealtimeDbService(
    private val context: Context,
    private val database: AppDatabase? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "FirebaseRTDB"

    private var rtdb: FirebaseDatabase? = null
    private var casesRef: DatabaseReference? = null
    private var paymentsRef: DatabaseReference? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("Firebase Realtime Database Initialized")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    init {
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:581332599780:android:35da385f77928d2beffdf1")
                        .setApiKey("AIzaSyCkcmJZQOvT1ROjxJijEqbn6wOv04ko734")
                        .setProjectId("vml-recovery-app")
                        .setStorageBucket("vml-recovery-app.firebasestorage.app")
                        .setDatabaseUrl("https://vml-recovery-app-default-rtdb.firebaseio.com")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                } catch (initEx: Exception) {
                    Log.w(TAG, "Explicit FirebaseOptions init failed: ${initEx.message}, trying default init", initEx)
                    FirebaseApp.initializeApp(context)
                }
            }

            val fbInstance = try {
                FirebaseDatabase.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "Default FirebaseDatabase init fallback: ${e.message}")
                null
            }

            if (fbInstance != null) {
                rtdb = fbInstance
                try {
                    fbInstance.setPersistenceEnabled(true)
                } catch (ignored: Exception) {
                    // Persistence can only be set once
                }

                casesRef = fbInstance.getReference("cases")
                paymentsRef = fbInstance.getReference("payments")

                // Listen to connection state
                val connectedRef = fbInstance.getReference(".info/connected")
                connectedRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val connected = snapshot.getValue(Boolean::class.java) ?: false
                        _isConnected.value = connected
                        _syncStatusMessage.value = if (connected) {
                            "Firebase Realtime Database: Online & Live"
                        } else {
                            "Firebase Realtime Database: Local Cached (Offline Mode)"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Connection listener cancelled: ${error.message}")
                    }
                })

                // Listen for remote real-time updates on cases
                casesRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Cases sync error: ${error.message}")
                    }
                })
            } else {
                _syncStatusMessage.value = "Firebase RTDB Ready (Awaiting Cloud Project Binding)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Realtime Database: ${e.message}", e)
            _syncStatusMessage.value = "RTDB Offline Persistence Active"
        }
    }

    /**
     * Broadcasts a new or updated debtor case to Firebase Realtime Database
     */
    fun pushCaseUpdate(case: CaseEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val safeCaseId = case.caseId.replace(Regex("[.#$\\[\\]/]"), "_")
                casesRef?.child(safeCaseId)?.setValue(
                    mapOf(
                        "caseId" to case.caseId,
                        "debtorName" to case.debtorName,
                        "nationalId" to case.nationalId,
                        "contactPhone" to case.contactPhone,
                        "clientName" to case.clientName,
                        "totalAmount" to case.totalAmount,
                        "recoveredAmount" to case.recoveredAmount,
                        "balance" to case.balance,
                        "status" to case.status,
                        "legalStage" to case.legalStage,
                        "assignedOfficerName" to case.assignedOfficerName,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push case update to Firebase: ${e.message}")
            }
        }
    }

    /**
     * Broadcasts a payment record to Firebase Realtime Database
     */
    fun pushPaymentRecord(payment: PaymentTransactionEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val safeRef = payment.referenceNumber.ifBlank { "PAY-${payment.paymentId}" }.replace(Regex("[.#$\\[\\]/]"), "_")
                paymentsRef?.child(safeRef)?.setValue(
                    mapOf(
                        "caseId" to payment.caseId,
                        "amount" to payment.amount,
                        "paymentDate" to payment.paymentDate,
                        "paymentChannel" to payment.paymentChannel,
                        "referenceNumber" to payment.referenceNumber,
                        "recordedBy" to payment.recordedBy,
                        "notes" to payment.notes,
                        "syncedAt" to System.currentTimeMillis()
                    )
                )
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push payment to Firebase: ${e.message}")
            }
        }
    }

    /**
     * Broadcasts a registered member metadata to Firebase Realtime Database
     * (Sensitive credentials such as passwords and salts are strictly excluded).
     */
    fun pushUserRegistration(user: com.example.data.model.UserEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val safeUserId = user.userId.replace(Regex("[.#$\\[\\]/]"), "_")
                val usersRef = rtdb?.getReference("members")
                usersRef?.child(safeUserId)?.setValue(
                    mapOf(
                        "userId" to user.userId,
                        "email" to user.email,
                        "fullName" to user.fullName,
                        "role" to user.role,
                        "department" to user.department,
                        "phoneNumber" to user.phoneNumber,
                        "organization" to user.organization,
                        "isEmailVerified" to user.isEmailVerified,
                        "isLocked" to user.isLocked,
                        "registeredAt" to System.currentTimeMillis()
                    )
                )
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push member registration to Firebase: ${e.message}")
            }
        }
    }

    /**
     * Clears demo nodes in Firebase Realtime Database for production initialization
     */
    fun purgeDemoDataFromFirebase() {
        scope.launch(Dispatchers.IO) {
            try {
                casesRef?.removeValue()
                paymentsRef?.removeValue()
                _lastSyncTimestamp.value = System.currentTimeMillis()
                _syncStatusMessage.value = "Firebase Cloud Data Cleared for Production"
            } catch (e: Exception) {
                Log.w(TAG, "Failed to purge Firebase nodes: ${e.message}")
            }
        }
    }
}
