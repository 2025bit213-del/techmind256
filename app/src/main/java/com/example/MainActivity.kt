package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.firebase.FirebaseRealtimeDbService
import com.example.repository.VmlRepository
import com.example.service.SmsNotificationService
import com.example.ui.components.VmlBottomNavigationBar
import com.example.ui.components.VmlNavigationRail
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val firebaseService = FirebaseRealtimeDbService(applicationContext, database)
        val smsService = SmsNotificationService(database)
        val repository = VmlRepository(database, firebaseService, smsService)
        val factory = VmlViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: VmlViewModel = viewModel(factory = factory)
                VmlApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmlApp(viewModel: VmlViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val latestToast by viewModel.latestSmsToast.collectAsState()

    var showWindowsGuide by remember { mutableStateOf(false) }

    if (showWindowsGuide) {
        WindowsInstallGuideDialog(onDismiss = { showWindowsGuide = false })
    }

    if (!isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {}
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "VML Recover",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentRole.displayName} • ${currentUser?.fullName ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Windows PC Installation Guide Button
                        IconButton(
                            onClick = { showWindowsGuide = true },
                            modifier = Modifier.testTag("windows_guide_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LaptopWindows,
                                contentDescription = "Install on Windows PC",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Logout Action
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    VmlBottomNavigationBar(
                        currentScreen = currentScreen,
                        userRole = currentRole,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isWideScreen) {
                        VmlNavigationRail(
                            currentScreen = currentScreen,
                            userRole = currentRole,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when (currentScreen) {
                            NavigationScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavigationScreen.ADMIN_SYSTEM_CONTROL -> AdminControlScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.ADMIN_ANALYTICS -> AdminAnalyticsScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.CASES -> CaseListScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavigationScreen.CASE_DETAIL -> CaseDetailScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.MANAGER_ALLOCATION,
                            NavigationScreen.MANAGER_SUPERVISION -> CollectionManagerScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.FIELD_VISITS -> FieldOfficerScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.FINANCE_RECONCILIATION,
                            NavigationScreen.PAYMENT_ENGINE -> FinanceOfficerScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.LEGAL_HUB -> LegalHubScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.PROMISES -> PromiseTrackerScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.CLIENT_PORTAL -> ClientPortalScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.DEBTOR_PORTAL -> DebtorPortalScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.REPORTS -> ReportsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // In-App SMS / Email Dispatch Toast
                latestToast?.let { toast ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(if (isWideScreen) 0.6f else 0.95f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (toast.eventType == "VERIFICATION_CODE") Icons.Default.MarkEmailRead else Icons.Default.Sms,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (toast.eventType == "VERIFICATION_CODE") "Email Dispatch Alert" else "SMS Notification Sent",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = toast.messageText,
                                    color = Color(0xFFE2E8F0),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { viewModel.dismissSmsToast() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
