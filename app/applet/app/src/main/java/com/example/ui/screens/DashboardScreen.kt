package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun DashboardScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allPromises by viewModel.allPromises.collectAsState()
    val allLegalDocs by viewModel.allLegalDocs.collectAsState()

    val totalDebt = allCases.sumOf { it.principalAmount + it.interestAmount }
    val totalRecovered = allCases.sumOf { it.amountPaid }
    val totalOutstanding = allCases.sumOf { it.totalOutstanding }
    val recoveryRate = if (totalDebt > 0) ((totalRecovered / totalDebt) * 100).toInt() else 0

    Scaffold(
        topBar = {
            // ONLY the Logout button on top bar as requested
            VmlTopAppBar(
                title = "VML Recover",
                subtitle = "${currentUser?.fullName} • ${currentUser?.role}",
                currentUser = currentUser,
                showBackButton = false,
                onLogoutClick = { viewModel.logout() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navigation Quick Links Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.navigateTo(NavigationScreen.CLIENT_PORTAL) },
                        label = { Text("Client Debtors Portal", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VmlTeal,
                            selectedLabelColor = VmlSurface
                        )
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.navigateTo(NavigationScreen.CASES) },
                        label = { Text("All Cases (${allCases.size})") },
                        leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.navigateTo(NavigationScreen.LEGAL_HUB) },
                        label = { Text("Legal Hub") },
                        leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.navigateTo(NavigationScreen.PROMISES) },
                        label = { Text("PTP Tracker") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.navigateTo(NavigationScreen.FINANCE_RECONCILIATION) },
                        label = { Text("Finance") },
                        leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    if (currentUser?.role == "ADMIN") {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.navigateTo(NavigationScreen.ADMIN_SYSTEM_CONTROL) },
                            label = { Text("Admin Controls") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Executive Summary Card
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
                                Text(
                                    text = "Total Outstanding Portfolio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VmlSurface.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatUgx(totalOutstanding),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlSurface
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VmlGreen.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VmlGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = VmlSurface, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$recoveryRate% Recovered",
                                        color = VmlSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Recovered",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VmlSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatUgx(totalRecovered),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlGreen
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Active Legal Filings",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VmlSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${allLegalDocs.size} Documents",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlAmber
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Print Master Report Button
                        Button(
                            onClick = {
                                val summaryHtml = """
                                    <h2>VML RECOVER — PORTFOLIO RECOVERY MASTER REPORT</h2>
                                    <p><strong>Generated By:</strong> ${currentUser?.fullName} (${currentUser?.role})</p>
                                    <p><strong>Date:</strong> ${java.util.Date()}</p>
                                    <hr/>
                                    <p><strong>Total Cases:</strong> ${allCases.size}</p>
                                    <p><strong>Total Portfolio Amount:</strong> UGX $totalDebt</p>
                                    <p><strong>Total Amount Recovered:</strong> UGX $totalRecovered ($recoveryRate%)</p>
                                    <p><strong>Total Outstanding:</strong> UGX $totalOutstanding</p>
                                    <p><strong>Pending PTP Commitments:</strong> ${allPromises.size}</p>
                                    <p><strong>Active Court Documents:</strong> ${allLegalDocs.size}</p>
                                """.trimIndent()
                                printDocumentUsingAndroidPrint(context, "Master_Recovery_Summary", summaryHtml)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print Executive Summary Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Portal Access Card (Prompt Highlight)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(NavigationScreen.CLIENT_PORTAL) },
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VmlTeal)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VmlTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = VmlTeal, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Client Debtors Portal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VmlTextPrimary
                            )
                            Text(
                                text = "View all debtors connected to bank clients, sort by Bank, Amount, Loan Aging, Sector & Legal Level.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = VmlTextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VmlTeal)
                    }
                }
            }

            // Recent Cases Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "High-Risk Recovery Cases",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(NavigationScreen.CASES) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View All", fontSize = 12.sp)
                    }
                }
            }

            items(allCases.take(3)) { caseItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectCase(caseItem.caseId) },
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Text(
                                text = caseItem.debtorName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VmlTextPrimary
                            )
                            StatusBadge(text = caseItem.creditorName, color = VmlNavy)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${caseItem.debtorType} • Sector: ${caseItem.sectorOfWork} • Aging: ${caseItem.ageingCategory}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = VmlTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Outstanding: " + formatUgx(caseItem.totalOutstanding),
                                fontWeight = FontWeight.Bold,
                                color = VmlCrimson,
                                fontSize = 13.sp
                            )
                            Text(
                                text = caseItem.legalStage.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = VmlNavyDark
                            )
                        }
                    }
                }
            }
        }
    }
}
