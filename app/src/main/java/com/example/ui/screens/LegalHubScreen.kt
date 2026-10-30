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
import com.example.data.model.LegalDocumentEntity
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.viewmodel.VmlViewModel

@Composable
fun LegalHubScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val allDocs by viewModel.allLegalDocs.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val metrics by viewModel.portfolioMetrics.collectAsState()

    var showGenerateDocDialog by remember { mutableStateOf(false) }

    if (showGenerateDocDialog) {
        var caseId by remember { mutableStateOf(allCases.firstOrNull()?.caseId ?: "REC-2026-001") }
        var docTitle by remember { mutableStateOf("7-Day Statutory Notice Prior to Execution") }
        var docType by remember { mutableStateOf("STATUTORY_NOTICE") }

        Dialog(onDismissRequest = { showGenerateDocDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Issue Statutory Demand / Notice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = docTitle, onValueChange = { docTitle = it }, label = { Text("Document Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showGenerateDocDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.escalateLegalStage(caseId, "DEMAND_LETTER", docType, docTitle) { success, _ ->
                                if (success) showGenerateDocDialog = false
                            }
                        }) { Text("Issue Document") }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("legal_hub_screen")) {
        VmlHeader(
            title = "Legal Escalation & Litigation",
            subtitle = "Court Filings, Attachment Warrants & Statutory Notices",
            badgeText = "Legal Operations",
            actions = {
                Button(onClick = { showGenerateDocDialog = true }) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Notice")
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VmlStatCard(title = "Litigation Files", value = metrics.legalEscalationsCount.toString(), icon = Icons.Default.Balance, modifier = Modifier.weight(1f))
            VmlStatCard(title = "Issued Documents", value = allDocs.size.toString(), icon = Icons.Default.Article, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Statutory Instruments & Court Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(allDocs) { doc ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(doc.docTitle, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(doc.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Advocate: ${doc.advocateName} • Case: ${doc.caseId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
