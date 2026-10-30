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
fun AdminAnalyticsScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.portfolioMetrics.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_analytics_screen")
    ) {
        VmlHeader(
            title = "Executive Intelligence & Analytics",
            subtitle = "Directorate Performance, Yield & Risk Heatmap",
            badgeText = "Executive View"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(title = "Assigned Portfolio", value = "UGX " + String.format("%,.0f", metrics.totalAssigned / 1000000.0) + "M", icon = Icons.Default.MonetizationOn, modifier = Modifier.weight(1f))
            VmlStatCard(title = "Recovery Ratio", value = "${String.format("%.1f", metrics.recoveryRatePercent)}%", icon = Icons.Default.AutoGraph, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Strategic Portfolio Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Active Borrowers: ${metrics.totalActiveCases}", style = MaterialTheme.typography.bodyMedium)
                Text("Promises to Pay in Pipeline: ${metrics.pendingPromisesCount}", style = MaterialTheme.typography.bodyMedium)
                Text("Litigation Escalations: ${metrics.legalEscalationsCount}", style = MaterialTheme.typography.bodyMedium)
                Text("Total Outstanding Claim: UGX " + String.format("%,.0f", metrics.totalOutstanding), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
