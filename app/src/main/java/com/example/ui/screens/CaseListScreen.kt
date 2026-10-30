package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaseEntity
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.VmlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseListScreen(
    viewModel: VmlViewModel,
    onNavigate: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCases by viewModel.allCases.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    var showNewCaseDialog by remember { mutableStateOf(false) }

    if (showNewCaseDialog) {
        NewCaseDialog(
            viewModel = viewModel,
            onDismiss = { showNewCaseDialog = false }
        )
    }

    val filteredCases = remember(allCases, searchQuery, selectedStatus) {
        allCases.filter { case ->
            val matchesQuery = searchQuery.isBlank() ||
                    case.debtorName.contains(searchQuery, ignoreCase = true) ||
                    case.loanNumber.contains(searchQuery, ignoreCase = true) ||
                    case.clientName.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatus == "ALL" || case.status.equals(selectedStatus, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("case_list_screen")
    ) {
        VmlHeader(
            title = "Recovery Cases",
            subtitle = "Active Debtor Claims, Balances & Recovery Progress",
            badgeText = "${filteredCases.size} Cases",
            actions = {
                Button(
                    onClick = { showNewCaseDialog = true },
                    modifier = Modifier.testTag("new_case_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Case")
                }
            }
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Search debtor name, loan # or bank") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .testTag("case_search_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        val statuses = listOf("ALL", "ACTIVE", "PENDING_PTP", "LEGAL_ESCALATION", "CLOSED")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            statuses.forEach { st ->
                FilterChip(
                    selected = selectedStatus == st,
                    onClick = { viewModel.setStatusFilter(st) },
                    label = { Text(st.replace("_", " ")) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Case Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCases) { case ->
                CaseCardItem(
                    case = case,
                    onClick = { viewModel.selectCase(case.caseId) }
                )
            }
        }
    }
}

@Composable
fun CaseCardItem(
    case: CaseEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("case_item_${case.caseId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = case.debtorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${case.clientName} • #${case.loanNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                VmlStatusBadge(case.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Claim", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("UGX " + String.format("%,.0f", case.totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Recovered", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("UGX " + String.format("%,.0f", case.recoveredAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
                Column {
                    Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("UGX " + String.format("%,.0f", case.balance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Officer: ${case.assignedOfficerName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${case.ageingDays} Days Overdue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
