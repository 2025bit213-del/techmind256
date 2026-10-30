package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.repository.VmlRepository
import com.example.service.SmsNotificationService
import com.example.ui.screens.AdminControlScreen
import com.example.ui.screens.CaseDetailScreen
import com.example.ui.screens.CaseListScreen
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceOfficerScreen
import com.example.ui.screens.LegalHubScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PromiseTrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel
import com.example.viewmodel.VmlViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: VmlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val smsService = SmsNotificationService(applicationContext, database.smsNotificationDao())
        val repository = VmlRepository(database, smsService)
        val factory = VmlViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VmlViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VmlMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VmlMainApp(viewModel: VmlViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        // Handle Back navigation cleanly
        if (currentScreen != NavigationScreen.DASHBOARD && currentScreen != NavigationScreen.CLIENT_PORTAL) {
            BackHandler {
                if (currentUser?.role == "CLIENT") {
                    viewModel.navigateTo(NavigationScreen.CLIENT_PORTAL)
                } else {
                    viewModel.navigateTo(NavigationScreen.DASHBOARD)
                }
            }
        }

        when (currentScreen) {
            NavigationScreen.CLIENT_PORTAL -> {
                ClientPortalScreen(viewModel = viewModel)
            }
            NavigationScreen.DASHBOARD -> {
                if (currentUser?.role == "CLIENT") {
                    ClientPortalScreen(viewModel = viewModel)
                } else {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            NavigationScreen.CASES -> {
                CaseListScreen(viewModel = viewModel)
            }
            NavigationScreen.CASE_DETAIL -> {
                CaseDetailScreen(viewModel = viewModel)
            }
            NavigationScreen.LEGAL_HUB -> {
                LegalHubScreen(viewModel = viewModel)
            }
            NavigationScreen.PROMISES -> {
                PromiseTrackerScreen(viewModel = viewModel)
            }
            NavigationScreen.FINANCE_RECONCILIATION -> {
                FinanceOfficerScreen(viewModel = viewModel)
            }
            NavigationScreen.ADMIN_SYSTEM_CONTROL -> {
                AdminControlScreen(viewModel = viewModel)
            }
            else -> {
                if (currentUser?.role == "CLIENT") {
                    ClientPortalScreen(viewModel = viewModel)
                } else {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
