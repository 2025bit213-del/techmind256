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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransactionEntity
import com.example.ui.components.AdminDeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlTopAppBar
import com.example.ui.components.formatUgx
import com.example.ui.components.printDocumentUsingAndroidPrint
import com.example.ui.theme.VmlCardBorder
import com.example.ui.theme.VmlCrimson
import com.example.ui.theme.VmlGreen
import com.example.ui.theme.VmlNavy
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
fun FinanceOfficerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var paymentToDelete by remember { mutableStateOf<PaymentTransactionEntity?>(null) }

    val totalCollections = allPayments.sumOf { it.amount }

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            // ONLY Logout button on top bar
            VmlTopAppBar(
                title = "Finance Reconciliation & Treasury",
                subtitle = "Reconciled Collections: ${formatUgx(totalCollections)}",
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
            // Summary & Print Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Verified Direct Collections", color = VmlSurface.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(
                            text = formatUgx(totalCollections),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VmlSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${allPayments.size} Verified Direct Debit, Mobile Money & Bank Inflows",
                            color = VmlSurface.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // PRINT TREASURY RECONCILIATION REPORT
                        Button(
                            onClick = {
                                val reportHtml = """
                                    <h2>TREASURY COLLECTIONS & RECONCILIATION AUDIT</h2>
                                    <p><strong>Generated By:</strong> ${currentUser?.fullName} (${currentUser?.role})</p>
                                    <p><strong>Date:</strong> ${java.util.Date()}</p>
                                    <hr/>
                                    <p><strong>Total Transactions:</strong> ${allPayments.size}</p>
                                    <p><strong>Total Inflow Volume:</strong> UGX $totalCollections</p>
                                    <table border='1' cellpadding='6' cellspacing='0' style='width:100%; border-collapse:collapse; margin-top:12px;'>
                                        <tr><th>Receipt</th><th>Case ID</th><th>Amount</th><th>Channel</th><th>Recorded By</th></tr>
                                        ${allPayments.joinToString("") { p ->
                                            "<tr><td>${p.receiptNumber}</td><td>${p.caseId}</td><td>UGX ${p.amount}</td><td>${p.paymentChannel}</td><td>${p.recordedBy}</td></tr>"
                                        }}
                                    </table>
                                """.trimIndent()
                                printDocumentUsingAndroidPrint(context, "Treasury_Reconciliation_Report", reportHtml)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print Treasury Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Transaction Ledgers (${allPayments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = VmlTextPrimary
                )
            }

            items(allPayments, key = { it.id }) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                text = "Case: ${payment.caseId} • By: ${payment.recordedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VmlNavy
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(payment.paymentDate)),
                                style = MaterialTheme.typography.labelSmall,
                                color = VmlTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Print Receipt
                            IconButton(
                                onClick = {
                                    val receiptHtml = """
                                        <h2>TREASURY PAYMENT VOUCHER</h2>
                                        <p><strong>Receipt No:</strong> ${payment.receiptNumber}</p>
                                        <p><strong>Amount:</strong> UGX ${payment.amount}</p>
                                        <p><strong>Channel:</strong> ${payment.paymentChannel}</p>
                                        <p><strong>Case Reference:</strong> ${payment.caseId}</p>
                                        <p><strong>Officer:</strong> ${payment.recordedBy}</p>
                                        <p><strong>Notes:</strong> ${payment.notes}</p>
                                    """.trimIndent()
                                    printDocumentUsingAndroidPrint(context, "Voucher_${payment.receiptNumber}", receiptHtml)
                                }
                            ) {
                                Icon(Icons.Default.Print, contentDescription = "Print Receipt", tint = VmlNavy)
                            }

                            // Delete/Reverse Payment (Admin Authorization strictly required)
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

    if (paymentToDelete != null) {
        val payment = paymentToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Payment Record",
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
