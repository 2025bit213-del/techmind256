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
import androidx.compose.ui.window.Dialog
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.viewmodel.VmlViewModel

@Composable
fun FieldOfficerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val allVisits by viewModel.allVisits.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    var showScheduleVisitDialog by remember { mutableStateOf(false) }

    if (showScheduleVisitDialog) {
        var caseId by remember { mutableStateOf(allCases.firstOrNull()?.caseId ?: "REC-2026-001") }
        var address by remember { mutableStateOf("Plot 88 Kyanja, Kampala") }
        var notes by remember { mutableStateOf("Physical verification of trading premises") }

        Dialog(onDismissRequest = { showScheduleVisitDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Schedule Field Visit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Physical Location Address") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Visit Objectives & Instructions") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showScheduleVisitDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.scheduleFieldVisit(caseId, address, System.currentTimeMillis() + 86400000L, notes) { success, _ ->
                                if (success) showScheduleVisitDialog = false
                            }
                        }) { Text("Confirm Schedule") }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("field_officer_screen")) {
        VmlHeader(
            title = "Field Operations Hub",
            subtitle = "Debtor Premises Visits, Asset Inspections & Geo Check-ins",
            badgeText = "Field Operations",
            actions = {
                Button(onClick = { showScheduleVisitDialog = true }) {
                    Icon(imageVector = Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule Visit")
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VmlStatCard(title = "Scheduled Visits", value = allVisits.size.toString(), icon = Icons.Default.DirectionsWalk, modifier = Modifier.weight(1f))
            VmlStatCard(title = "Debtor Found Rate", value = "85%", icon = Icons.Default.Check, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Field Visit Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(allVisits) { visit ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(visit.locationAddress, fontWeight = FontWeight.Bold)
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(visit.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Officer: ${visit.officerName} • GPS: ${visit.gpsCoordinates}", style = MaterialTheme.typography.bodySmall)
                        Text(visit.outcome.ifBlank { visit.notes }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
