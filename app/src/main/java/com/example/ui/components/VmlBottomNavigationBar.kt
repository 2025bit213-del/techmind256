package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.UserRole

data class BottomNavItem(
    val screen: NavigationScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun VmlBottomNavigationBar(
    currentScreen: NavigationScreen,
    userRole: UserRole,
    onNavigate: (NavigationScreen) -> Unit
) {
    val items = getNavItemsForRole(userRole)

    NavigationBar(
        modifier = Modifier.testTag("vml_bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
fun VmlNavigationRail(
    currentScreen: NavigationScreen,
    userRole: UserRole,
    onNavigate: (NavigationScreen) -> Unit
) {
    val items = getNavItemsForRole(userRole)

    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .testTag("vml_navigation_rail"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

private fun getNavItemsForRole(role: UserRole): List<BottomNavItem> {
    return when (role) {
        UserRole.ADMIN -> listOf(
            BottomNavItem(NavigationScreen.ADMIN_SYSTEM_CONTROL, "Admin", Icons.Default.AdminPanelSettings, "nav_admin"),
            BottomNavItem(NavigationScreen.DASHBOARD, "Overview", Icons.Default.Dashboard, "nav_overview"),
            BottomNavItem(NavigationScreen.CASES, "Cases", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.REPORTS, "Reports", Icons.Default.Analytics, "nav_reports")
        )
        UserRole.DIRECTOR -> listOf(
            BottomNavItem(NavigationScreen.DASHBOARD, "Executive", Icons.Default.Dashboard, "nav_executive"),
            BottomNavItem(NavigationScreen.ADMIN_ANALYTICS, "Analytics", Icons.Default.BarChart, "nav_analytics"),
            BottomNavItem(NavigationScreen.CASES, "Portfolio", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.REPORTS, "Reports", Icons.Default.Assessment, "nav_reports")
        )
        UserRole.COLLECTION_MANAGER -> listOf(
            BottomNavItem(NavigationScreen.MANAGER_ALLOCATION, "Allocations", Icons.Default.AssignmentInd, "nav_allocation"),
            BottomNavItem(NavigationScreen.DASHBOARD, "Overview", Icons.Default.Dashboard, "nav_overview"),
            BottomNavItem(NavigationScreen.CASES, "Cases", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.PROMISES, "PTP Tracker", Icons.Default.Handshake, "nav_promises"),
            BottomNavItem(NavigationScreen.REPORTS, "Performance", Icons.Default.Analytics, "nav_reports")
        )
        UserRole.COLLECTION_OFFICER -> listOf(
            BottomNavItem(NavigationScreen.CASES, "My Cases", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.PROMISES, "Promises", Icons.Default.Handshake, "nav_promises"),
            BottomNavItem(NavigationScreen.FIELD_VISITS, "Field Visits", Icons.Default.PinDrop, "nav_visits"),
            BottomNavItem(NavigationScreen.DASHBOARD, "Overview", Icons.Default.Dashboard, "nav_overview")
        )
        UserRole.LEGAL_OFFICER -> listOf(
            BottomNavItem(NavigationScreen.LEGAL_HUB, "Legal Hub", Icons.Default.Gavel, "nav_legal"),
            BottomNavItem(NavigationScreen.CASES, "Litigation", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.DASHBOARD, "Overview", Icons.Default.Dashboard, "nav_overview"),
            BottomNavItem(NavigationScreen.REPORTS, "Notices", Icons.Default.Description, "nav_reports")
        )
        UserRole.FINANCE_OFFICER -> listOf(
            BottomNavItem(NavigationScreen.FINANCE_RECONCILIATION, "Reconcile", Icons.Default.AccountBalance, "nav_finance"),
            BottomNavItem(NavigationScreen.CASES, "Receivables", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.DASHBOARD, "Overview", Icons.Default.Dashboard, "nav_overview"),
            BottomNavItem(NavigationScreen.REPORTS, "Audit", Icons.Default.Receipt, "nav_reports")
        )
        UserRole.CLIENT_PORTAL -> listOf(
            BottomNavItem(NavigationScreen.CLIENT_PORTAL, "Portfolio", Icons.Default.Business, "nav_client"),
            BottomNavItem(NavigationScreen.CASES, "Debtors", Icons.Default.FolderShared, "nav_cases"),
            BottomNavItem(NavigationScreen.REPORTS, "Reports", Icons.Default.Assessment, "nav_reports")
        )
        UserRole.DEBTOR_PORTAL -> listOf(
            BottomNavItem(NavigationScreen.DEBTOR_PORTAL, "My Loan", Icons.Default.Person, "nav_debtor"),
            BottomNavItem(NavigationScreen.PROMISES, "Pay Plan", Icons.Default.Schedule, "nav_plan")
        )
    }
}
