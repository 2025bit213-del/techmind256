package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaseEntity
import com.example.ui.components.AdminDeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlPrintButton
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

@Composable
fun ClientPortalScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val filteredCases by viewModel.filteredClientCases.collectAsState()

    val searchQuery by viewModel.clientSearchQuery.collectAsState()
    val bankFilter by viewModel.clientBankFilter.collectAsState()
    val amountSort by viewModel.clientAmountSort.collectAsState()
    val agingFilter by viewModel.clientAgingFilter.collectAsState()
    val sectorFilter by viewModel.clientSectorFilter.collectAsState()
    val legalStageFilter by viewModel.clientLegalStageFilter.collectAsState()
    val debtorTypeFilter by viewModel.clientDebtorTypeFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var caseToDelete by remember { mutableStateOf<CaseEntity?>(null) }

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Calculations for Client Overview
    val totalOutstanding = filteredCases.sumOf { it.totalOutstanding }
    val totalPaid = filteredCases.sumOf { it.amountPaid }
    val totalOriginal = filteredCases.sumOf { it.principalAmount + it.interestAmount }
    val recoveryRate = if (totalOriginal > 0) ((totalPaid / totalOriginal) * 100).toInt() else 0

    Scaffold(
        topBar = {
            // ONLY the Logout button on top bar as requested
            VmlTopAppBar(
                title = "Client Portal — Debtors Database",
                subtitle = "Connected to ${currentUser?.organization ?: "Bank / Corporate"} (${currentUser?.fullName})",
                currentUser = currentUser,
                showBackButton = false,
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
            // Summary Card
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
                                    text = "Total Active Portfolio",
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
                                color = VmlTeal.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VmlTeal)
                            ) {
                                Text(
                                    text = "${filteredCases.size} Debtors",
                                    color = VmlSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Recovered so far",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VmlSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatUgx(totalPaid),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlGreen
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Recovery Rate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VmlSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$recoveryRate%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VmlAmber
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Print Portfolio Report Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val reportContent = buildString {
                                        append("<h3>DEBTORS RECOVERY PORTFOLIO STATEMENT</h3>")
                                        append("<p>Client: ${currentUser?.organization ?: "Bank / Corporate"}</p>")
                                        append("<p>Date: ${java.util.Date()}</p>")
                                        append("<hr/>")
                                        append("<table border='1' cellpadding='6' cellspacing='0' style='width:100%; border-collapse:collapse;'>")
                                        append("<tr><th>Case ID</th><th>Debtor</th><th>Type</th><th>Bank</th><th>Sector</th><th>Aging</th><th>Legal Stage</th><th>Outstanding</th></tr>")
                                        filteredCases.forEach { c ->
                                            append("<tr>")
                                            append("<td>${c.caseId}</td>")
                                            append("<td>${c.debtorName}</td>")
                                            append("<td>${c.debtorType}</td>")
                                            append("<td>${c.creditorName}</td>")
                                            append("<td>${c.sectorOfWork}</td>")
                                            append("<td>${c.ageingCategory}</td>")
                                            append("<td>${c.legalStage}</td>")
                                            append("<td>UGX ${c.totalOutstanding}</td>")
                                            append("</tr>")
                                        }
                                        append("</table>")
                                    }
                                    printDocumentUsingAndroidPrint(context, "Debtors_Portfolio_Report", reportContent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Print Portfolio", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (viewModel.canAddClient()) {
                                        showAddDialog = true
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Permission Denied: Only Admin or Collection Manager can add new clients/debtors.")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlSurface.copy(alpha = 0.2f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = VmlSurface, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Client/Debtor", color = VmlSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.clientSearchQuery.value = it },
                    placeholder = { Text("Search debtors by name, case ID, or bank...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VmlNavy) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clientSearchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VmlNavy,
                        unfocusedBorderColor = VmlCardBorder
                    ),
                    singleLine = true
                )
            }

            // Sorting & Filtering Options
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sort & Filter Debtors:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )

                    // 1. Debtor Type (Company vs Individual)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Type:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf("ALL" to "All Types", "INDIVIDUAL" to "Individual", "COMPANY" to "Company").forEach { (typeVal, label) ->
                            FilterChip(
                                selected = debtorTypeFilter == typeVal,
                                onClick = { viewModel.clientDebtorTypeFilter.value = typeVal },
                                label = { Text(label) },
                                leadingIcon = {
                                    if (typeVal == "INDIVIDUAL") Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                    else if (typeVal == "COMPANY") Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }

                    // 2. Bank Filter
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bank:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf(
                            "ALL" to "All Banks",
                            "Stanbic Bank Uganda" to "Stanbic Bank",
                            "Centenary Bank" to "Centenary Bank",
                            "DFCU Bank" to "DFCU Bank",
                            "Absa Bank Uganda" to "Absa Bank"
                        ).forEach { (bVal, bLabel) ->
                            FilterChip(
                                selected = bankFilter == bVal,
                                onClick = { viewModel.clientBankFilter.value = bVal },
                                label = { Text(bLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }

                    // 3. Amount Sort
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Amount:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf(
                            "NONE" to "Default Order",
                            "HIGH_TO_LOW" to "Highest First",
                            "LOW_TO_HIGH" to "Lowest First"
                        ).forEach { (sortVal, sortLabel) ->
                            FilterChip(
                                selected = amountSort == sortVal,
                                onClick = { viewModel.clientAmountSort.value = sortVal },
                                label = { Text(sortLabel) },
                                leadingIcon = {
                                    if (sortVal == "HIGH_TO_LOW") Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                                    else if (sortVal == "LOW_TO_HIGH") Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }

                    // 4. Loan Aging
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Aging:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf(
                            "ALL" to "All Aging",
                            "30_DAYS" to "30 Days",
                            "60_DAYS" to "60 Days",
                            "90_DAYS" to "90 Days",
                            "180_PLUS" to "180+ Days"
                        ).forEach { (aKey, aLabel) ->
                            FilterChip(
                                selected = agingFilter == aKey,
                                onClick = { viewModel.clientAgingFilter.value = aKey },
                                label = { Text(aLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }

                    // 5. Sector of Work
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sector:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf(
                            "ALL" to "All Sectors",
                            "Trade & Retail" to "Trade & Retail",
                            "Logistics & Transport" to "Logistics",
                            "Manufacturing" to "Manufacturing",
                            "Agriculture" to "Agriculture",
                            "Services" to "Services",
                            "Real Estate" to "Real Estate"
                        ).forEach { (sKey, sLabel) ->
                            FilterChip(
                                selected = sectorFilter == sKey,
                                onClick = { viewModel.clientSectorFilter.value = sKey },
                                label = { Text(sLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }

                    // 6. Legal Case Level
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Legal Level:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        listOf(
                            "ALL" to "All Levels",
                            "PRE_LEGAL" to "Pre-Legal",
                            "DEMAND_LETTER" to "Demand Letter",
                            "SUMMONS_ISSUED" to "Summons Issued",
                            "COURT_HEARING" to "Court Hearing",
                            "JUDGMENT_ENFORCEMENT" to "Judgment Execution"
                        ).forEach { (lKey, lLabel) ->
                            FilterChip(
                                selected = legalStageFilter == lKey,
                                onClick = { viewModel.clientLegalStageFilter.value = lKey },
                                label = { Text(lLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VmlNavy,
                                    selectedLabelColor = VmlSurface
                                )
                            )
                        }
                    }
                }
            }

            // Results count banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matching Debtors (${filteredCases.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                    if (filteredCases.size != allCases.size) {
                        TextButton(onClick = {
                            viewModel.clientSearchQuery.value = ""
                            viewModel.clientBankFilter.value = "ALL"
                            viewModel.clientAmountSort.value = "NONE"
                            viewModel.clientAgingFilter.value = "ALL"
                            viewModel.clientSectorFilter.value = "ALL"
                            viewModel.clientLegalStageFilter.value = "ALL"
                            viewModel.clientDebtorTypeFilter.value = "ALL"
                        }) {
                            Text("Reset Filters", color = VmlCrimson)
                        }
                    }
                }
            }

            // Empty State
            if (filteredCases.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VmlSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = VmlTextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No debtors found", fontWeight = FontWeight.Bold, color = VmlTextPrimary)
                            Text("Try adjusting your bank, sector, aging, or legal filters.", color = VmlTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Debtors List Items
            items(filteredCases, key = { it.caseId }) { caseItem ->
                DebtorClientCard(
                    caseItem = caseItem,
                    onViewDetails = { viewModel.selectCase(caseItem.caseId) },
                    onPrintStatement = {
                        val statementHtml = """
                            <h2>DEBTOR ACCOUNT STATEMENT</h2>
                            <p><strong>Case ID:</strong> ${caseItem.caseId}</p>
                            <p><strong>Debtor:</strong> ${caseItem.debtorName} (${caseItem.debtorType})</p>
                            <p><strong>Creditor Bank:</strong> ${caseItem.creditorName}</p>
                            <p><strong>Account Number:</strong> ${caseItem.creditorAccountNumber}</p>
                            <p><strong>Sector:</strong> ${caseItem.sectorOfWork}</p>
                            <p><strong>Loan Aging:</strong> ${caseItem.ageingCategory}</p>
                            <p><strong>Legal Status:</strong> ${caseItem.legalStage}</p>
                            <p><strong>Principal:</strong> UGX ${caseItem.principalAmount}</p>
                            <p><strong>Interest & Fees:</strong> UGX ${caseItem.interestAmount + caseItem.penaltiesAmount}</p>
                            <p><strong>Total Paid:</strong> UGX ${caseItem.amountPaid}</p>
                            <h3><strong>TOTAL OUTSTANDING: UGX ${caseItem.totalOutstanding}</strong></h3>
                        """.trimIndent()
                        printDocumentUsingAndroidPrint(context, "Statement_${caseItem.caseId}", statementHtml)
                    },
                    onDelete = {
                        // Deleting anything strictly requires permission from the Admin
                        caseToDelete = caseItem
                    }
                )
            }
        }
    }

    // Add Client / Debtor Dialog (Requires permission to add)
    if (showAddDialog) {
        AddClientDebtorDialog(
            onDismiss = { showAddDialog = false },
            onConfirmAdd = { newCase ->
                viewModel.addCase(newCase) { success, msg ->
                    if (success) {
                        showAddDialog = false
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            }
        )
    }

    // Admin Authorization Required for Deleting
    if (caseToDelete != null) {
        val item = caseToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Debtor / Client Record",
            itemId = "${item.debtorName} (${item.caseId})",
            currentUser = currentUser,
            onDismiss = { caseToDelete = null },
            onConfirmDelete = { adminPasswordAttempt, reason ->
                viewModel.deleteCase(item.caseId, adminPasswordAttempt, reason) { success, msg ->
                    if (success) {
                        caseToDelete = null
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
fun DebtorClientCard(
    caseItem: CaseEntity,
    onViewDetails: () -> Unit,
    onPrintStatement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VmlSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Debtor Name, Type Badge & Bank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                align = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (caseItem.debtorType == "COMPANY") VmlNavyDark.copy(alpha = 0.1f)
                                else VmlTeal.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (caseItem.debtorType == "COMPANY") Icons.Default.Business else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (caseItem.debtorType == "COMPANY") VmlNavyDark else VmlTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = caseItem.debtorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VmlTextPrimary
                        )
                        Text(
                            text = "${caseItem.debtorType} • ${caseItem.caseId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                    }
                }

                StatusBadge(
                    text = caseItem.creditorName,
                    color = VmlNavy
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Sector, Aging, Legal Stage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(text = caseItem.sectorOfWork, color = VmlTeal)
                StatusBadge(
                    text = when (caseItem.ageingCategory) {
                        "180_PLUS" -> "180+ Days Delinquent"
                        "90_DAYS" -> "90 Days Aging"
                        "60_DAYS" -> "60 Days Aging"
                        else -> "30 Days Aging"
                    },
                    color = if (caseItem.ageingCategory == "180_PLUS") VmlCrimson else VmlAmber
                )
                StatusBadge(text = caseItem.legalStage.replace("_", " "), color = VmlNavyDark)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financial Summary Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Principal Debt",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                        Text(
                            text = formatUgx(caseItem.principalAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = VmlTextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Amount Paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                        Text(
                            text = formatUgx(caseItem.amountPaid),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = VmlGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Outstanding Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                        Text(
                            text = formatUgx(caseItem.totalOutstanding),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = VmlCrimson
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                align = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Details", fontSize = 13.sp)
                }

                // Print Statement Button
                OutlinedButton(
                    onClick = onPrintStatement,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VmlNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 13.sp)
                }

                // Delete Button (Requires Admin Permission)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VmlCrimson.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete (Requires Admin)",
                        tint = VmlCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Dialog to add a new client / debtor
@Composable
fun AddClientDebtorDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (CaseEntity) -> Unit
) {
    var debtorName by remember { mutableStateOf("") }
    var debtorType by remember { mutableStateOf("INDIVIDUAL") }
    var bankName by remember { mutableStateOf("Stanbic Bank Uganda") }
    var sector by remember { mutableStateOf("Trade & Retail") }
    var principal by remember { mutableStateOf("15000000") }
    var phone by remember { mutableStateOf("+256 700 112 233") }
    var nationalId by remember { mutableStateOf("CM90012233KLAX") }
    var legalStage by remember { mutableStateOf("PRE_LEGAL") }
    var aging by remember { mutableStateOf("90_DAYS") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Client / Debtor Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = debtorName,
                    onValueChange = { debtorName = it },
                    label = { Text("Debtor Full Name / Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = debtorType == "INDIVIDUAL",
                        onClick = { debtorType = "INDIVIDUAL" },
                        label = { Text("Individual") }
                    )
                    FilterChip(
                        selected = debtorType == "COMPANY",
                        onClick = { debtorType = "COMPANY" },
                        label = { Text("Company") }
                    )
                }

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Creditor Bank / Institution") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sector,
                    onValueChange = { sector = it },
                    label = { Text("Sector of Work (e.g. Retail, Logistics)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = { Text("Principal Amount (UGX)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = { Text("National ID / Reg Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pAmount = principal.toDoubleOrNull() ?: 10000000.0
                    val caseId = "VML-2025-" + (System.currentTimeMillis() % 10000)
                    val newCase = CaseEntity(
                        caseId = caseId,
                        debtorName = debtorName.ifBlank { "New Debtor" },
                        debtorNationalId = nationalId,
                        debtorPhone = phone,
                        creditorName = bankName,
                        principalAmount = pAmount,
                        interestAmount = pAmount * 0.12,
                        debtorType = debtorType,
                        sectorOfWork = sector,
                        ageingCategory = aging,
                        legalStage = legalStage,
                        assignedOfficerName = "David Ochieng"
                    )
                    onConfirmAdd(newCase)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VmlNavy)
            ) {
                Text("Add Record")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
