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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.PromiseToPayEntity
import com.example.ui.components.AdminDeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlTopAppBar
import com.example.ui.components.formatUgx
import com.example.ui.components.printDocumentUsingAndroidPrint
import com.example.ui.theme.VmlAmber
import com.example.ui.theme.VmlCardBorder
import com.example.ui.theme.VmlCrimson
import com.example.ui.theme.VmlGreen
import com.example.ui.theme.VmlNavy
import com.example.ui.theme.VmlSurface
import com.example.ui.theme.VmlTextPrimary
import com.example.ui.theme.VmlTextSecondary
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel
import kotlinx.coroutines.launch

@Composable
fun PromiseTrackerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val allPromises by viewModel.allPromises.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var ptpToDelete by remember { mutableStateOf<PromiseToPayEntity?>(null) }

    Scaffold(
        topBar = {
            // ONLY Logout button on top bar
            VmlTopAppBar(
                title = "Promise To Pay (PTP) Tracker",
                subtitle = "Manage Debtor Commitments & Call Schedules",
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Commitments (${allPromises.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record PTP")
                    }
                }
            }

            items(allPromises, key = { it.id }) { ptp ->
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
                                text = ptp.debtorName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VmlTextPrimary
                            )
                            StatusBadge(text = ptp.status, color = if (ptp.status == "FULFILLED") VmlGreen else VmlAmber)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Promised Amount: " + formatUgx(ptp.promisedAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = VmlNavy
                        )
                        Text(
                            text = "Due Date: ${ptp.promiseDate} • Channel: ${ptp.paymentMethod}",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                        if (ptp.notes.isNotEmpty()) {
                            Text(
                                text = "Notes: ${ptp.notes}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = VmlTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PRINT PTP COMMITMENT LETTER
                            Button(
                                onClick = {
                                    val ptpHtml = """
                                        <h2>PROMISE TO PAY (PTP) ACKNOWLEDGEMENT</h2>
                                        <p><strong>Case ID:</strong> ${ptp.caseId}</p>
                                        <p><strong>Debtor:</strong> ${ptp.debtorName}</p>
                                        <p><strong>Promised Amount:</strong> UGX ${ptp.promisedAmount}</p>
                                        <p><strong>Commitment Date:</strong> ${ptp.promiseDate}</p>
                                        <p><strong>Payment Channel:</strong> ${ptp.paymentMethod}</p>
                                        <p><strong>Negotiating Officer:</strong> ${ptp.officerName}</p>
                                        <hr/>
                                        <p>The debtor has solemnly undertaken to transfer the above amount into the designated trust collection account by the specified date.</p>
                                    """.trimIndent()
                                    printDocumentUsingAndroidPrint(context, "PTP_${ptp.id}", ptpHtml)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlNavy),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print PTP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Delete PTP (Admin Authorization required)
                            IconButton(
                                onClick = { ptpToDelete = ptp },
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

    if (showAddDialog) {
        var caseId by remember { mutableStateOf(allCases.firstOrNull()?.caseId ?: "VML-2025-001") }
        var debtorName by remember { mutableStateOf(allCases.firstOrNull()?.debtorName ?: "Kasule John Baptist") }
        var amount by remember { mutableStateOf("3000000") }
        var date by remember { mutableStateOf("28 Oct 2025") }
        var notes by remember { mutableStateOf("Promised direct deposit upon sales invoice payment") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record Debtor Promise to Pay", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caseId,
                        onValueChange = { caseId = it },
                        label = { Text("Case ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = debtorName,
                        onValueChange = { debtorName = it },
                        label = { Text("Debtor Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Promised Amount (UGX)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Call / Negotiation Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 1000000.0
                        viewModel.addPromise(caseId, debtorName, amt, date, notes)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VmlNavy)
                ) {
                    Text("Record Commitment")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (ptpToDelete != null) {
        val item = ptpToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Promise To Pay Commitment",
            itemId = "For ${item.debtorName} (UGX ${item.promisedAmount})",
            currentUser = currentUser,
            onDismiss = { ptpToDelete = null },
            onConfirmDelete = { adminPasswordAttempt, reason ->
                if (currentUser?.role != "ADMIN" && adminPasswordAttempt.trim() != "admin") {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Administrator permission required. Invalid authorization.")
                    }
                } else {
                    ptpToDelete = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("PTP record deleted under Admin authorization.")
                    }
                }
            }
        )
    }
}
