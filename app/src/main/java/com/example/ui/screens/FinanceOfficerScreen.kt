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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.PrintUtils
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.VmlViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceOfficerScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allPayments by viewModel.allPayments.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val metrics by viewModel.portfolioMetrics.collectAsState()

    var showAddPaymentDialog by remember { mutableStateOf(false) }

    if (showAddPaymentDialog) {
        RecordPaymentDialog(
            caseId = allCases.firstOrNull()?.caseId ?: "REC-2026-001",
            viewModel = viewModel,
            onDismiss = { showAddPaymentDialog = false }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("finance_officer_screen")) {
        VmlHeader(
            title = "Finance & Reconciliation",
            subtitle = "Escrow Deposits, Client Settlements & Cash Collections",
            badgeText = "Finance Department",
            actions = {
                Button(onClick = { showAddPaymentDialog = true }) {
                    Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Deposit")
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VmlStatCard(
                title = "Total Collected",
                value = "UGX " + String.format("%,.0f", metrics.totalRecovered / 1000000.0) + "M",
                icon = Icons.Default.Paid,
                iconTint = Color(0xFF16A34A),
                modifier = Modifier.weight(1f)
            )
            VmlStatCard(
                title = "Transactions",
                value = allPayments.size.toString(),
                icon = Icons.Default.ReceiptLong,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Reconciliation Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(allPayments) { payment ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UGX " + String.format("%,.0f", payment.amount), fontWeight = FontWeight.Bold)
                            Text("Ref: ${payment.referenceNumber} • Channel: ${payment.paymentChannel}", style = MaterialTheme.typography.bodySmall)
                            Text("Recorded by: ${payment.recordedBy}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        VmlStatusBadge(payment.status)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val html = PrintUtils.generateReceiptHtml(
                                    receiptNo = payment.referenceNumber,
                                    debtorName = "Debtor Account",
                                    loanNo = payment.caseId,
                                    amount = payment.amount,
                                    channel = payment.paymentChannel,
                                    clientName = "VML Recover Client",
                                    date = dateFormat.format(Date(payment.paymentDate))
                                )
                                PrintUtils.printHtmlDocument(context, "Receipt_${payment.referenceNumber}", html)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = "Print")
                        }
                    }
                }
            }
        }
    }
}
