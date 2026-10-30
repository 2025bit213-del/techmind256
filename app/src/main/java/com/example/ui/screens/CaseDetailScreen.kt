package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaseDetailScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(NavigationScreen.CASES)
    }

    val context = LocalContext.current
    val case by viewModel.selectedCase.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allPromises by viewModel.allPromises.collectAsState()
    val allTimeline by viewModel.allTimeline.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val casePayments = remember(case, allPayments) {
        allPayments.filter { it.caseId == case?.caseId }
    }
    val casePromises = remember(case, allPromises) {
        allPromises.filter { it.caseId == case?.caseId }
    }
    val caseTimeline = remember(case, allTimeline) {
        allTimeline.filter { it.caseId == case?.caseId }
    }

    if (showPaymentDialog && case != null) {
        RecordPaymentDialog(
            caseId = case!!.caseId,
            viewModel = viewModel,
            onDismiss = { showPaymentDialog = false }
        )
    }

    val targetCase = case
    if (targetCase == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Case not found or not selected.")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("case_detail_screen")
    ) {
        VmlHeader(
            title = targetCase.debtorName,
            subtitle = "${targetCase.clientName} • #${targetCase.loanNumber}",
            badgeText = targetCase.status,
            actions = {
                IconButton(onClick = { viewModel.navigateTo(NavigationScreen.CASES) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        toastMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF0FDF4),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(text = msg, modifier = Modifier.padding(10.dp), color = Color(0xFF166534))
            }
        }

        // Financial Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Claim Financial Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Original Principal", style = MaterialTheme.typography.labelSmall)
                        Text("UGX " + String.format("%,.0f", targetCase.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Recovered", style = MaterialTheme.typography.labelSmall)
                        Text("UGX " + String.format("%,.0f", targetCase.recoveredAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                    Column {
                        Text("Balance Due", style = MaterialTheme.typography.labelSmall)
                        Text("UGX " + String.format("%,.0f", targetCase.balance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Risk Level: ${targetCase.riskLevel}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Legal Stage: ${targetCase.legalStage}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showPaymentDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Payment")
            }
            OutlinedButton(
                onClick = {
                    viewModel.sendPaymentReminderSms(targetCase.caseId) { _, msg ->
                        toastMessage = msg
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send SMS Alert")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Debtor Information Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Debtor & Collateral Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("National ID / Reg: ${targetCase.nationalId.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                Text("Contact Phone: ${targetCase.contactPhone.ifBlank { "+256 700 000 000" }}", style = MaterialTheme.typography.bodySmall)
                Text("Pledged Collateral: ${targetCase.collateral}", style = MaterialTheme.typography.bodySmall)
                Text("Assigned Officer: ${targetCase.assignedOfficerName}", style = MaterialTheme.typography.bodySmall)
                Text("Next Scheduled Action: ${targetCase.nextAction}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment History
        Text("Payment Transactions (${casePayments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (casePayments.isEmpty()) {
            Text("No payments recorded yet for this claim.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            casePayments.forEach { p ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("UGX " + String.format("%,.0f", p.amount), fontWeight = FontWeight.Bold)
                            Text("${p.paymentChannel} • Ref: ${p.referenceNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(
                            onClick = {
                                val html = PrintUtils.generateReceiptHtml(
                                    receiptNo = p.referenceNumber,
                                    debtorName = targetCase.debtorName,
                                    loanNo = targetCase.loanNumber,
                                    amount = p.amount,
                                    channel = p.paymentChannel,
                                    clientName = targetCase.clientName,
                                    date = dateFormat.format(Date(p.paymentDate))
                                )
                                PrintUtils.printHtmlDocument(context, "Receipt_${p.referenceNumber}", html)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = "Print Receipt")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Timeline
        Text("Recovery Timeline (${caseTimeline.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        caseTimeline.forEach { act ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(act.actionType, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("By: ${act.performedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(act.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
