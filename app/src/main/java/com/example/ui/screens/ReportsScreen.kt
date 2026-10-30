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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.viewmodel.VmlViewModel

@Composable
fun ReportsScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.portfolioMetrics.collectAsState()
    val clientSummaries by viewModel.clientSummaries.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reports_screen")
    ) {
        VmlHeader(
            title = "Recovery Reports & Analytics",
            subtitle = "Institutional Client Portfolios, Ageing Analysis & Performance",
            badgeText = "Executive Reports"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(title = "Total Assigned", value = "UGX " + String.format("%,.0f", metrics.totalAssigned / 1000000.0) + "M", icon = Icons.Default.Assessment, modifier = Modifier.weight(1f))
            VmlStatCard(title = "Overall Recovery", value = "${String.format("%.1f", metrics.recoveryRatePercent)}%", icon = Icons.Default.TrendingUp, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Client Portfolio Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        clientSummaries.forEach { summary ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(summary.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Debtors: ${summary.activeCases}", style = MaterialTheme.typography.bodySmall)
                        Text("Total Claim: UGX ${String.format("%,.0f", summary.totalDebt / 1000000.0)}M", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Recovered: UGX ${String.format("%,.0f", summary.recoveredAmount / 1000000.0)}M", style = MaterialTheme.typography.bodySmall)
                        Text("Recovery: ${String.format("%.1f", summary.recoveryPercentage)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
