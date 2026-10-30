package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.UserRole
import com.example.viewmodel.VmlViewModel

@Composable
fun DashboardScreen(
    viewModel: VmlViewModel,
    onNavigate: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.portfolioMetrics.collectAsState()
    val userRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        VmlHeader(
            title = "Recovery Operations Dashboard",
            subtitle = "Welcome back, ${currentUser?.fullName ?: "Officer"}",
            badgeText = userRole.displayName,
            badgeColor = MaterialTheme.colorScheme.primary,
            actions = {
                FilledTonalButton(
                    onClick = { onNavigate(NavigationScreen.CASES) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Cases")
                }
            }
        )

        // Hero Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.vml_hero),
                    contentDescription = "VML Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Uganda Institutional Debt Recovery Portfolio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Standard Operating Procedure: Respectful, Legal, Efficient & Compliant Collections",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High-level Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(
                title = "Total Assigned",
                value = "UGX " + String.format("%,.0f", metrics.totalAssigned / 1000000.0) + "M",
                icon = Icons.Default.AccountBalance,
                modifier = Modifier.weight(1f),
                subtitle = "Active Claims"
            )
            VmlStatCard(
                title = "Recovered",
                value = "UGX " + String.format("%,.0f", metrics.totalRecovered / 1000000.0) + "M",
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF16A34A),
                modifier = Modifier.weight(1f),
                subtitle = "${String.format("%.1f", metrics.recoveryRatePercent)}% Recovery Rate"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(
                title = "Outstanding",
                value = "UGX " + String.format("%,.0f", metrics.totalOutstanding / 1000000.0) + "M",
                icon = Icons.Default.TrendingDown,
                iconTint = Color(0xFFEA580C),
                modifier = Modifier.weight(1f),
                subtitle = "${metrics.totalActiveCases} Active Debtors"
            )
            VmlStatCard(
                title = "Pending Promises",
                value = "UGX " + String.format("%,.0f", metrics.pendingPromisesAmount / 1000000.0) + "M",
                icon = Icons.Default.Handshake,
                iconTint = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f),
                subtitle = "${metrics.pendingPromisesCount} Commitments"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Tiles
        Text(
            text = "Workflow Quick Launch",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(
                onClick = { onNavigate(NavigationScreen.PROMISES) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Promise Tracker", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Follow up debtor payment promises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedCard(
                onClick = { onNavigate(NavigationScreen.FIELD_VISITS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Field Visits", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Geo-checkins & premises inspections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(
                onClick = { onNavigate(NavigationScreen.LEGAL_HUB) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Legal Escalations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Court litigation & demand notices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedCard(
                onClick = { onNavigate(NavigationScreen.REPORTS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analytics & Reports", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Recovery statistics & PDF exports", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
