package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.VmlViewModel

@Composable
fun ClientPortalScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val allCases by viewModel.allCases.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val clientCases = remember(allCases, currentUser) {
        allCases.filter { it.clientName.contains("Centenary", ignoreCase = true) }
    }

    val totalDebt = clientCases.sumOf { it.totalAmount }
    val recovered = clientCases.sumOf { it.recoveredAmount }

    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("client_portal_screen")) {
        VmlHeader(
            title = "Creditor Institutional Portal",
            subtitle = currentUser?.organization ?: "Centenary Bank Uganda",
            badgeText = "Creditor Dashboard"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VmlStatCard(title = "Submitted Claims", value = "UGX " + String.format("%,.0f", totalDebt / 1000000.0) + "M", icon = Icons.Default.AccountBalance, modifier = Modifier.weight(1f))
            VmlStatCard(title = "Settled Recoveries", value = "UGX " + String.format("%,.0f", recovered / 1000000.0) + "M", icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Loan Recovery Portfolio (${clientCases.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(clientCases) { case ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(case.debtorName, fontWeight = FontWeight.Bold)
                            VmlStatusBadge(case.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Loan: ${case.loanNumber} • Balance: UGX " + String.format("%,.0f", case.balance), style = MaterialTheme.typography.bodySmall)
                        Text("Officer: ${case.assignedOfficerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
