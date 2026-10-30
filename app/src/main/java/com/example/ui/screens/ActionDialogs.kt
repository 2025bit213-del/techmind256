package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CaseEntity
import com.example.viewmodel.VmlViewModel
import java.util.UUID

@Composable
fun NewCaseDialog(
    viewModel: VmlViewModel,
    onDismiss: () -> Unit
) {
    var debtorName by remember { mutableStateOf("") }
    var loanNumber by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("Centenary Rural Development Bank") }
    var totalAmount by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var collateral by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("new_case_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Register Recovery Case",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = debtorName,
                    onValueChange = { debtorName = it },
                    label = { Text("Debtor Full Name") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("case_debtor_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = loanNumber,
                    onValueChange = { loanNumber = it },
                    label = { Text("Loan / Account Number") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("case_loan_no_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Outstanding Debt (UGX)") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("case_amount_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = { Text("National ID (NIN) / Company Reg") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = collateral,
                    onValueChange = { collateral = it },
                    label = { Text("Pledged Collateral / Security") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = totalAmount.toDoubleOrNull()
                            if (debtorName.isBlank() || loanNumber.isBlank() || parsedAmount == null || parsedAmount <= 0) {
                                errorMsg = "Please enter valid debtor name, loan number, and debt amount."
                                return@Button
                            }
                            val newCase = CaseEntity(
                                caseId = "REC-" + System.currentTimeMillis().toString().takeLast(6),
                                debtorName = debtorName.trim(),
                                loanNumber = loanNumber.trim(),
                                clientName = clientName,
                                totalAmount = parsedAmount,
                                recoveredAmount = 0.0,
                                balance = parsedAmount,
                                status = "ACTIVE",
                                ageingDays = 30,
                                assignedOfficerId = "USR-004",
                                assignedOfficerName = "David Ochieng",
                                nextAction = "Initiate demand call & send statutory notice",
                                lastContactDate = System.currentTimeMillis(),
                                collateral = collateral.ifBlank { "Unsecured" },
                                contactPhone = phone,
                                nationalId = nationalId,
                                legalStage = "PRE_LEGAL",
                                riskLevel = "MEDIUM"
                            )
                            viewModel.registerNewCase(newCase) { success, _ ->
                                if (success) onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("submit_case_button")
                    ) {
                        Text("Register Case")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    caseId: String,
    viewModel: VmlViewModel,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("BANK_TRANSFER") }
    var refNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("record_payment_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = "Record Debt Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (UGX)") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("payment_amount_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = refNumber,
                    onValueChange = { refNumber = it },
                    label = { Text("Bank Slip / Mobile Money Ref") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Narration") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMsg = "Please enter a valid amount."
                                return@Button
                            }
                            viewModel.recordPayment(caseId, amount, channel, refNumber, notes) { success, _ ->
                                if (success) onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("confirm_payment_button")
                    ) {
                        Text("Record & Issue Receipt")
                    }
                }
            }
        }
    }
}
