package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LegalDocumentEntity
import com.example.ui.components.AdminDeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlTopAppBar
import com.example.ui.components.printDocumentUsingAndroidPrint
import com.example.ui.theme.VmlAmber
import com.example.ui.theme.VmlCardBorder
import com.example.ui.theme.VmlCrimson
import com.example.ui.theme.VmlNavy
import com.example.ui.theme.VmlNavyDark
import com.example.ui.theme.VmlSurface
import com.example.ui.theme.VmlTeal
import com.example.ui.theme.VmlTextPrimary
import com.example.ui.theme.VmlTextSecondary
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel
import kotlinx.coroutines.launch

@Composable
fun LegalHubScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val allLegalDocs by viewModel.allLegalDocs.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    var showDraftDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<LegalDocumentEntity?>(null) }

    Scaffold(
        topBar = {
            // ONLY Logout button on top bar
            VmlTopAppBar(
                title = "Legal Escalation & Litigation Hub",
                subtitle = "Statutory Demands, Summons & Court Decrees",
                currentUser = currentUser,
                showBackButton = true,
                onBackClick = { viewModel.navigateTo(NavigationScreen.DASHBOARD) },
                onLogoutClick = { viewModel.logout() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Draft New Document Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Court Filings & Demands (${allLegalDocs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                    Button(
                        onClick = { showDraftDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Draft Document")
                    }
                }
            }

            items(allLegalDocs, key = { it.id }) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VmlTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(text = doc.status, color = if (doc.status == "SERVED") VmlGreen else VmlAmber)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Case: ${doc.caseId} • Type: ${doc.docType.replace("_", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = doc.contentSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VmlTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        if (doc.courtReference.isNotEmpty()) {
                            Text(
                                text = "Court Ref: ${doc.courtReference} • Hearing: ${doc.courtDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VmlNavyDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            align = Alignment.CenterVertically
                        ) {
                            // PRINT DOCUMENT BUTTON
                            Button(
                                onClick = {
                                    val docHtml = """
                                        <h2>${doc.title}</h2>
                                        <p><strong>Case Reference:</strong> ${doc.caseId}</p>
                                        <p><strong>Type:</strong> ${doc.docType}</p>
                                        <p><strong>Date:</strong> ${doc.generatedDate}</p>
                                        <p><strong>Court Reference:</strong> ${doc.courtReference.ifEmpty { "N/A" }}</p>
                                        <p><strong>Hearing Date:</strong> ${doc.courtDate.ifEmpty { "N/A" }}</p>
                                        <p><strong>Assigned Advocate:</strong> ${doc.assignedAdvocate}</p>
                                        <hr/>
                                        <p>${doc.contentSummary}</p>
                                        <br/>
                                        <p>Seal of the High Court of Uganda Commercial Division / VML Recover</p>
                                    """.trimIndent()
                                    printDocumentUsingAndroidPrint(context, doc.title, docHtml)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Print Document", fontWeight = FontWeight.Bold)
                            }

                            // Delete Document (Admin Authorization required)
                            IconButton(
                                onClick = { docToDelete = doc },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(VmlCrimson.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete (Requires Admin)", tint = VmlCrimson, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDraftDialog) {
        var caseId by remember { mutableStateOf(allCases.firstOrNull()?.caseId ?: "VML-2025-001") }
        var docType by remember { mutableStateOf("DEMAND_NOTICE") }
        var title by remember { mutableStateOf("Formal Statutory 7-Day Demand") }
        var summary by remember { mutableStateOf("Notice to pay full outstanding balance before High Court attachment.") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("Draft Legal Document", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caseId,
                        onValueChange = { caseId = it },
                        label = { Text("Case ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Content Summary / Legal Grounds") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addLegalDocument(caseId, docType, title, summary)
                        showDraftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VmlNavy)
                ) {
                    Text("Save & Issue")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDraftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (docToDelete != null) {
        val item = docToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Legal Document",
            itemId = "${item.title} (#${item.id})",
            currentUser = currentUser,
            onDismiss = { docToDelete = null },
            onConfirmDelete = { adminPasswordAttempt, reason ->
                // Check admin permission
                if (currentUser?.role != "ADMIN" && adminPasswordAttempt.trim() != "admin") {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Administrator permission required. Invalid authorization.")
                    }
                } else {
                    docToDelete = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Legal Document deleted under Admin authorization.")
                    }
                }
            }
        )
    }
}
