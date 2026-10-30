package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaseEntity
import com.example.ui.components.AdminDeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlTopAppBar
import com.example.ui.components.formatUgx
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
fun CaseListScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var caseToDelete by remember { mutableStateOf<CaseEntity?>(null) }

    val filteredList = allCases.filter {
        searchQuery.isBlank() ||
                it.debtorName.contains(searchQuery, ignoreCase = true) ||
                it.caseId.contains(searchQuery, ignoreCase = true) ||
                it.creditorName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            // ONLY the Logout button on top bar
            VmlTopAppBar(
                title = "All Recovery Cases",
                subtitle = "${allCases.size} Active Loan Portfolios",
                currentUser = currentUser,
                showBackButton = true,
                onBackClick = { viewModel.navigateTo(NavigationScreen.DASHBOARD) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Add Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search cases by debtor, ID, or bank...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VmlNavy) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VmlNavy,
                            unfocusedBorderColor = VmlCardBorder
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (viewModel.canAddClient()) {
                                showAddDialog = true
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Permission Denied: Only Admin or Collection Manager can add cases.")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            items(filteredList, key = { it.caseId }) { caseItem ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
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
                            StatusBadge(text = caseItem.creditorName, color = VmlNavy)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Outstanding: " + formatUgx(caseItem.totalOutstanding),
                                fontWeight = FontWeight.Bold,
                                color = VmlCrimson,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = caseItem.legalStage.replace("_", " "), color = VmlNavyDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { caseToDelete = caseItem },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VmlCrimson, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    if (caseToDelete != null) {
        val item = caseToDelete!!
        AdminDeleteConfirmationDialog(
            itemType = "Case Record",
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
