package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.viewmodel.VmlViewModel

@Composable
fun DebtorPortalScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    val debtorCase = allCases.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("debtor_portal_screen")
    ) {
        VmlHeader(
            title = "Debtor Self-Service Portal",
            subtitle = "Welcome, ${currentUser?.fullName ?: "Borrower"}",
            badgeText = "Verified Debtor"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(
                title = "Current Balance Due",
                value = "UGX " + String.format("%,.0f", debtorCase?.balance ?: 26500000.0),
                icon = Icons.Default.Receipt,
                iconTint = Color(0xFFDC2626),
                modifier = Modifier.weight(1f)
            )
            VmlStatCard(
                title = "Total Paid to Date",
                value = "UGX " + String.format("%,.0f", debtorCase?.recoveredAmount ?: 18500000.0),
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF16A34A),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settlement Account Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loan Reference: ${debtorCase?.loanNumber ?: "LN-CENT-88321"}", style = MaterialTheme.typography.bodySmall)
                Text("Creditor Bank: ${debtorCase?.clientName ?: "Centenary Bank"}", style = MaterialTheme.typography.bodySmall)
                Text("Designated Officer: ${debtorCase?.assignedOfficerName ?: "David Ochieng"}", style = MaterialTheme.typography.bodySmall)
                Text("Official Escrow Deposit: MTN/Airtel Merchant Code 992812 or Stanbic Account 9030018471201", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
