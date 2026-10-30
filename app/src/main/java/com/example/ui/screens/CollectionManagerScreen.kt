package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CaseEntity
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.VmlViewModel

@Composable
fun CollectionManagerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val allCases by viewModel.allCases.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val metrics by viewModel.portfolioMetrics.collectAsState()

    var selectedCaseForReassign by remember { mutableStateOf<CaseEntity?>(null) }
    var newOfficerId by remember { mutableStateOf("USR-004") }
    var reassignReason by remember { mutableStateOf("Workload Balancing & Territory Realignment") }

    val officers = remember(allUsers) {
        allUsers.filter { it.role.equals("COLLECTION_OFFICER", ignoreCase = true) }
    }

    selectedCaseForReassign?.let { targetCase ->
        AlertDialog(
            onDismissRequest = { selectedCaseForReassign = null },
            title = { Text("Reassign Case: ${targetCase.debtorName}") },
            text = {
                Column {
                    Text("Select new collection recovery officer:")
                    Spacer(modifier = Modifier.height(8.dp))
                    officers.forEach { off ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newOfficerId == off.userId,
                                onClick = { newOfficerId = off.userId }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${off.fullName} (${off.department})")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val officerName = officers.find { it.userId == newOfficerId }?.fullName ?: "Officer"
                        viewModel.reassignCase(targetCase.caseId, newOfficerId, officerName, reassignReason) { success, _ ->
                            if (success) selectedCaseForReassign = null
                        }
                    }
                ) {
                    Text("Confirm Reassignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCaseForReassign = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("collection_manager_screen")
    ) {
        VmlHeader(
            title = "Manager Supervisory Hub",
            subtitle = "Officer Allocations, Performance Quotas & Recovery Rate Oversight",
            badgeText = "Collection Manager",
            badgeColor = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(
                title = "Portfolio Assigned",
                value = "UGX " + String.format("%,.0f", metrics.totalAssigned / 1000000.0) + "M",
                icon = Icons.Default.Assignment,
                modifier = Modifier.weight(1f)
            )
            VmlStatCard(
                title = "Total Officers",
                value = officers.size.toString(),
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Active Case Allocations (${allCases.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(allCases) { case ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(case.debtorName, fontWeight = FontWeight.Bold)
                            Text("UGX " + String.format("%,.0f", case.balance) + " • Assigned: ${case.assignedOfficerName}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { selectedCaseForReassign = case },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Reassign", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
