package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransactionEntity
import com.example.data.security.SecurityUtils
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
import com.example.ui.theme.VmlNavyDark
import com.example.ui.theme.VmlSurface
import com.example.ui.theme.VmlTeal
import com.example.ui.theme.VmlTextPrimary
import com.example.ui.theme.VmlTextSecondary
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaseDetailScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val selectedCase by viewModel.selectedCase.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allPromises by viewModel.allPromises.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteCaseDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<PaymentTransactionEntity?>(null) }

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    val caseItem = selectedCase

    Scaffold(
        topBar = {
            // ONLY the Logout button on top bar as requested
            VmlTopAppBar(
                title = caseItem?.debtorName ?: "Case Detail",
                subtitle = "Ref: ${caseItem?.caseId ?: ""} • ${caseItem?.creditorName ?: ""}",
                currentUser = currentUser,
                showBackButton = true,
                onBackClick = { viewModel.navigateTo(NavigationScreen.CLIENT_PORTAL) },
                onLogoutClick = { viewModel.logout() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (caseItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No case selected", color = VmlTextSecondary)
            }
            return@Scaffold
        }

        val casePayments = allPayments.filter { it.caseId == caseItem.caseId }
        val casePromises = allPromises.filter { it.caseId == caseItem.caseId }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Financial Status Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Outstanding Balance", color = VmlSurface.copy(alpha = 0.8f), fontSize = 13.sp)
                                Text(
                                    text = formatUgx(caseItem.totalOutstanding),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlSurface
                                    )
                                )
                            }
                            StatusBadge(text = caseItem.status, color = if (caseItem.isPaidInFull) VmlGreen else VmlAmber)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Principal Debt", color = VmlSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatUgx(caseItem.principalAmount), color = VmlSurface, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Interest & Penalties", color = VmlSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatUgx(caseItem.interestAmount + caseItem.penaltiesAmount), color = VmlSurface, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Amount Recovered", color = VmlSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatUgx(caseItem.amountPaid), color = VmlGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PRINT BUTTONS ON DOCUMENT
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val demandHtml = """
                                        <h2>FORMAL FINAL STATUTORY DEMAND NOTICE</h2>
                                        <p><strong>To:</strong> ${caseItem.debtorName} (${caseItem.debtorType})</p>
                                        <p><strong>National ID / Reg:</strong> ${caseItem.debtorNationalId}</p>
                                        <p><strong>Address:</strong> ${caseItem.debtorAddress}</p>
                                        <p><strong>Creditor Bank:</strong> ${caseItem.creditorName}</p>
                                        <p><strong>Loan Agreement Ref:</strong> ${caseItem.originalAgreementRef}</p>
                                        <hr/>
                                        <p>Take notice that you are indebted to <strong>${caseItem.creditorName}</strong> in the sum of <strong>UGX ${caseItem.totalOutstanding}</strong>.</p>
                                        <p>You are hereby demanded to settle this amount within SEVEN (7) DAYS of receipt of this notice, failing which legal proceedings will commence in the High Court Commercial Division.</p>
                                        <br/>
                                        <p>Signed: Counsel Brenda Nabirye / VML Recover Legal Chambers</p>
                                    """.trimIndent()
                                    printDocumentUsingAndroidPrint(context, "Demand_Notice_${caseItem.caseId}", demandHtml)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print Demand Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val statementHtml = """
                                        <h2>CERTIFIED DEBT RECOVERY STATEMENT</h2>
                                        <p><strong>Debtor:</strong> ${caseItem.debtorName} | <strong>Case:</strong> ${caseItem.caseId}</p>
                                        <p><strong>Bank:</strong> ${caseItem.creditorName} | <strong>A/C:</strong> ${caseItem.creditorAccountNumber}</p>
                                        <p><strong>Principal:</strong> UGX ${caseItem.principalAmount} | <strong>Interest:</strong> UGX ${caseItem.interestAmount}</p>
                                        <p><strong>Total Recovered:</strong> UGX ${caseItem.amountPaid}</p>
                                        <p><strong>Current Outstanding:</strong> UGX ${caseItem.totalOutstanding}</p>
                                        <hr/>
                                        <p>Payments History: ${casePayments.size} recorded payments.</p>
                                    """.trimIndent()
                                    printDocumentUsingAndroidPrint(context, "Statement_${caseItem.caseId}", statementHtml)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlSurface.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = VmlSurface, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print Statement", color = VmlSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Debtor Profile & Creditor Bank Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Debtor & Bank Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow("Debtor Type", caseItem.debtorType)
                        DetailRow("Creditor Bank", caseItem.creditorName)
                        DetailRow("Bank Account", caseItem.creditorAccountNumber)
                        DetailRow("Sector of Work", caseItem.sectorOfWork)
                        DetailRow("Loan Aging", caseItem.ageingCategory.replace("_", " "))
                        DetailRow("Legal Stage", caseItem.legalStage.replace("_", " "))
                        DetailRow("National ID", SecurityUtils.maskNationalId(caseItem.debtorNationalId))
                        DetailRow("Phone Number", caseItem.debtorPhone)
                        DetailRow("Physical Address", caseItem.debtorAddress)
                        DetailRow("Assigned Officer", caseItem.assignedOfficerName)
                    }
                }
            }

            // Primary Case Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payment", fontWeight = FontWeight.Bold)
                    }

                    // Delete Case Button (Strictly Requires Admin Authorization)
                    Button(
                        onClick = { showDeleteCaseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlCrimson),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Case (Admin)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Payment History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payments History (${casePayments.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                }
            }

            if (casePayments.isEmpty()) {
                item {
                    Text("No payment transactions recorded yet.", color = VmlTextSecondary, fontSize = 13.sp)
                }
            } else {
                items(casePayments, key = { it.id }) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VmlSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = formatUgx(payment.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VmlGreen
                                )
                                Text(
                                    text = "${payment.paymentChannel} • Receipt: ${payment.receiptNumber}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = VmlTextSecondary
                                )
                                Text(
                                    text = "By: ${payment.recordedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VmlNavy
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Print Receipt Button
                                IconButton(
                                    onClick = {
                                        val receiptHtml = """
                                            <h2>OFFICIAL PAYMENT RECEIPT</h2>
                                            <p><strong>Receipt No:</strong> ${payment.receiptNumber}</p>
                                            <p><strong>Date:</strong> ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(payment.paymentDate))}</p>
                                            <p><strong>Debtor:</strong> ${caseItem.debtorName}</p>
                                            <p><strong>Creditor Bank:</strong> ${caseItem.creditorName}</p>
                                            <p><strong>Amount Paid:</strong> UGX ${payment.amount}</p>
                                            <p><strong>Payment Channel:</strong> ${payment.paymentChannel}</p>
                                            <p><strong>Authorized By:</strong> ${payment.recordedBy}</p>
                                        """.trimIndent()
                                        printDocumentUsingAndroidPrint(context, "Receipt_${payment.receiptNumber}", receiptHtml)
                                    }
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = "Print Receipt", tint = VmlNavy)
                                }

                                // Delete/Reverse Payment (Requires Admin Permission)
                                IconButton(
                                    onClick = { paymentToDelete = payment }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete (Requires Admin)", tint = VmlCrimson)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        var paymentAmount by remember { mutableStateOf("1000000") }
        var paymentChannel by remember { mutableStateOf("MTN Mobile Money") }
        var notes by remember { mutableStateOf("Direct recovery payment") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Direct Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Amount (UGX)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentChannel,
                        onValueChange = { paymentChannel = it },
                        label = { Text("Channel (e.g. MTN Mobile Money, Bank Wire)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Reference") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmount.toDoubleOrNull() ?: 0.0
                        viewModel.recordPayment(caseItem.caseId, amt, paymentChannel, notes) { success, msg ->
                            if (success) {
                                showPaymentDialog = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VmlGreen)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Case Dialog (Strictly Requires Admin Permission)
    if (showDeleteCaseDialog) {
        AdminDeleteConfirmationDialog(
            itemType = "Case / Loan Record",
            itemId = "${caseItem.debtorName} (${caseItem.caseId})",
            currentUser = currentUser,
            onDismiss = { showDeleteCaseDialog = false },
            onConfirmDelete = { adminPasswordAttempt, reason ->
                viewModel.deleteCase(caseItem.caseId, adminPasswordAttempt, reason) { success, msg ->
                    if (success) {
                        showDeleteCaseDialog = false
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            }
        )
    }

    // Delete Payment Dialog (Strictly Requires Admin Permission)
    if (paymentToDelete != null) {
        val payment = paymentToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Payment Transaction",
            itemId = "Receipt ${payment.receiptNumber} (UGX ${payment.amount})",
            currentUser = currentUser,
            onDismiss = { paymentToDelete = null },
            onConfirmDelete = { adminPasswordAttempt, reason ->
                viewModel.deletePayment(payment.id, adminPasswordAttempt, reason) { success, msg ->
                    if (success) {
                        paymentToDelete = null
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = VmlTextSecondary, fontSize = 13.sp)
        Text(text = value, color = VmlTextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
