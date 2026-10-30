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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.VmlViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PromiseTrackerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val allPromises by viewModel.allPromises.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val metrics by viewModel.portfolioMetrics.collectAsState()

    var showAddPromiseDialog by remember { mutableStateOf(false) }

    if (showAddPromiseDialog) {
        var caseId by remember { mutableStateOf(allCases.firstOrNull()?.caseId ?: "REC-2026-001") }
        var amountText by remember { mutableStateOf("5000000") }
        var notes by remember { mutableStateOf("Debtor agreed to deposit via mobile money") }

        Dialog(onDismissRequest = { showAddPromiseDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Record Promise to Pay (PTP)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Promise Amount (UGX)") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Terms & Settlement Commitment") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddPromiseDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 1000000.0
                            viewModel.recordPromise(caseId, amount, System.currentTimeMillis() + 86400000L * 5, notes) { success, _ ->
                                if (success) showAddPromiseDialog = false
                            }
                        }) { Text("Record Promise") }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("promise_tracker_screen")) {
        VmlHeader(
            title = "Promise-to-Pay (PTP) Tracker",
            subtitle = "Active Debtor Commitments & Maturity Schedules",
            badgeText = "PTP Pipeline",
            actions = {
                Button(onClick = { showAddPromiseDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New PTP")
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VmlStatCard(
                title = "Pipeline Total",
                value = "UGX " + String.format("%,.0f", metrics.pendingPromisesAmount / 1000000.0) + "M",
                icon = Icons.Default.Handshake,
                iconTint = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )
            VmlStatCard(
                title = "Total PTPs",
                value = allPromises.size.toString(),
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Commitment Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(allPromises) { ptp ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UGX " + String.format("%,.0f", ptp.promiseAmount), fontWeight = FontWeight.Bold)
                            Text("Maturity: ${dateFormat.format(Date(ptp.promiseDate))} • Case: ${ptp.caseId}", style = MaterialTheme.typography.bodySmall)
                            Text(ptp.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        VmlStatusBadge(ptp.status)
                    }
                }
            }
        }
    }
}
