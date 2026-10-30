package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.components.VmlTopAppBar
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminControlScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val systemConfig by viewModel.systemConfig.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    // State for viewing Database username and password protected by password
    var isDbCredentialsRevealed by remember { mutableStateOf(false) }
    var showPasswordPromptForDb by remember { mutableStateOf(false) }
    var enteredPasswordForDb by remember { mutableStateOf("") }
    var dbPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            // ONLY Logout button on top bar
            VmlTopAppBar(
                title = "Administrator Command & Security",
                subtitle = "Access Governance, Audit Trail & System Lockdown",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. System Maintenance Lockdown Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(VmlCrimson.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = VmlCrimson, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "System Maintenance Lockdown",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = VmlTextPrimary
                                    )
                                    Text(
                                        text = if (systemConfig?.isSystemLocked == true) "Active: Only Admin can log in" else "Inactive: Normal Operations",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = if (systemConfig?.isSystemLocked == true) VmlCrimson else VmlGreen
                                    )
                                }
                            }

                            Switch(
                                checked = systemConfig?.isSystemLocked == true,
                                onCheckedChange = {
                                    viewModel.toggleSystemLockdown("Scheduled Security Audit & Schema Verification") { _, msg ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VmlCrimson,
                                    checkedTrackColor = VmlCrimson.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // 2. Database Username & Password Section (Password Protected Without ###)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlNavy.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            align = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = VmlNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Database Connection Credentials",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VmlTextPrimary
                                )
                            }

                            if (isDbCredentialsRevealed) {
                                TextButton(onClick = { isDbCredentialsRevealed = false }) {
                                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = VmlTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hide", color = VmlTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isDbCredentialsRevealed) {
                            // Revealed actual database credentials without any ###
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = VmlNavyDark.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Database Engine: SQLite / Room Encrypted V2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VmlNavy)
                                    Text("Database File: vml_recover_secure.db", fontSize = 13.sp)
                                    Text("Database Username: vml_admin_master", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Database Password: VmlSecureDb#Pass2025!KeyStore", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Encryption: MasterKey Keystore AES-256", fontSize = 12.sp, color = VmlGreen)
                                }
                            }
                        } else {
                            // Password required to view
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = VmlNavy, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Protected Database Credentials",
                                        fontWeight = FontWeight.Bold,
                                        color = VmlTextPrimary
                                    )
                                    Text(
                                        text = "Viewing database username and password requires password authorization.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = VmlTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            showPasswordPromptForDb = true
                                            enteredPasswordForDb = ""
                                            dbPasswordError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VmlNavy)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Enter Password to View Credentials")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. User Accounts & Unlock Management
            item {
                Text(
                    text = "System User Accounts & Security Locks (${allUsers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = VmlTextPrimary
                )
            }

            items(allUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        align = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VmlTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(text = user.role, color = VmlNavy)
                            }
                            Text(text = user.email, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = VmlTextSecondary)
                            if (user.isLocked) {
                                Text(
                                    text = "LOCKED: ${user.lockReason ?: "Max failed attempts"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VmlCrimson
                                )
                            }
                        }

                        if (user.isLocked) {
                            Button(
                                onClick = {
                                    viewModel.unlockUser(user.userId) { _, msg ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VmlGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlock")
                            }
                        }
                    }
                }
            }

            // 4. Audit Log Trail & Print Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    align = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Security Audit Logs (${auditLogs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )

                    Button(
                        onClick = {
                            val auditHtml = """
                                <h2>VML RECOVER — MASTER AUDIT LOG ARCHIVE</h2>
                                <p><strong>Audited By:</strong> ${currentUser?.fullName} (Administrator)</p>
                                <p><strong>Date:</strong> ${java.util.Date()}</p>
                                <hr/>
                                <table border='1' cellpadding='6' cellspacing='0' style='width:100%; border-collapse:collapse;'>
                                    <tr><th>Time</th><th>Action</th><th>Actor</th><th>Role</th><th>Target</th><th>Details</th></tr>
                                    ${auditLogs.joinToString("") { log ->
                                        val timeStr = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                        "<tr><td>$timeStr</td><td>${log.action}</td><td>${log.actorUserId}</td><td>${log.actorRole}</td><td>${log.targetEntity}:${log.targetId}</td><td>${log.details}</td></tr>"
                                    }}
                                </table>
                            """.trimIndent()
                            printDocumentUsingAndroidPrint(context, "System_Audit_Logs", auditHtml)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VmlTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Audit Log")
                    }
                }
            }

            items(auditLogs.take(20)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlSurface),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (log.action.contains("DELETE") || log.action.contains("LOCK")) VmlCrimson else VmlNavy
                            )
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = VmlTextSecondary
                            )
                        }
                        Text(
                            text = "${log.actorUserId} (${log.actorRole}) • Target: ${log.targetEntity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlTextSecondary
                        )
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = VmlTextPrimary
                        )
                    }
                }
            }
        }
    }

    // Password Prompt Dialog for Database Credentials
    if (showPasswordPromptForDb) {
        AlertDialog(
            onDismissRequest = { showPasswordPromptForDb = false },
            title = { Text("Database Credentials Access", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter Administrator Password to decrypt and view database username and password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VmlTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPasswordForDb,
                        onValueChange = { enteredPasswordForDb = it },
                        label = { Text("Admin Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Demo Admin Pass: 'admin'",
                        style = MaterialTheme.typography.labelSmall,
                        color = VmlNavy,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (dbPasswordError != null) {
                        Text(
                            text = dbPasswordError ?: "",
                            color = VmlCrimson,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPasswordForDb.trim() == "admin") {
                            isDbCredentialsRevealed = true
                            showPasswordPromptForDb = false
                        } else {
                            dbPasswordError = "Invalid administrator password."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VmlNavy)
                ) {
                    Text("Unlock Credentials")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasswordPromptForDb = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
