package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AuditLogEntity
import com.example.data.model.SystemConfigEntity
import com.example.data.model.UserEntity
import com.example.ui.components.VmlHeader
import com.example.ui.components.VmlStatCard
import com.example.ui.components.VmlStatusBadge
import com.example.viewmodel.UserRole
import com.example.viewmodel.VmlViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val systemConfig by viewModel.systemConfig.collectAsState()
    val metrics by viewModel.portfolioMetrics.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showRegisterMemberDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Register Member Dialog (Admin Only)
    if (showRegisterMemberDialog) {
        RegisterMemberDialog(
            viewModel = viewModel,
            onDismiss = { showRegisterMemberDialog = false },
            onSuccess = { msg ->
                feedbackMessage = msg
                showRegisterMemberDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_control_screen")
    ) {
        VmlHeader(
            title = "Admin System Control",
            subtitle = "Restricted Member Onboarding, Email Verification & Security Compliance",
            badgeText = "System Administrator",
            badgeColor = MaterialTheme.colorScheme.primary,
            actions = {
                Button(
                    onClick = { showRegisterMemberDialog = true },
                    modifier = Modifier.testTag("register_member_top_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register Member")
                }
            }
        )

        // Feedback Banner
        feedbackMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { feedbackMessage = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VmlStatCard(
                title = "Total Members",
                value = users.size.toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                subtitle = "Registered by Admin"
            )
            val pendingVerificationCount = users.count { !it.isEmailVerified }
            VmlStatCard(
                title = "Pending Email",
                value = pendingVerificationCount.toString(),
                icon = Icons.Default.MarkEmailUnread,
                iconTint = Color(0xFFEA580C),
                modifier = Modifier.weight(1f),
                subtitle = "Require Verification"
            )
            val lockedCount = users.count { it.isLocked }
            VmlStatCard(
                title = "Locked Accounts",
                value = lockedCount.toString(),
                icon = Icons.Default.LockPerson,
                iconTint = if (lockedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                subtitle = "Security Lockouts"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs: Members, System Config, Audit Logs
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Member Management (${users.size})") },
                icon = { Icon(Icons.Default.Group, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Audit Trail") },
                icon = { Icon(Icons.Default.HistoryEdu, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Security Policy") },
                icon = { Icon(Icons.Default.Security, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> MemberManagementTab(
                users = users,
                viewModel = viewModel,
                onResendVerification = { user ->
                    viewModel.adminResendVerificationCode(user.userId) { success, msg ->
                        feedbackMessage = msg
                    }
                }
            )
            1 -> AuditTrailTab(auditLogs = auditLogs)
            2 -> SecurityPolicyTab(systemConfig = systemConfig, viewModel = viewModel)
        }
    }
}

@Composable
fun MemberManagementTab(
    users: List<UserEntity>,
    viewModel: VmlViewModel,
    onResendVerification: (UserEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("member_management_list")
    ) {
        items(users) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_card_${user.userId}"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.email + " • ID: " + user.userId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Role & Lock Badges
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = user.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (user.isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "LOCKED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Verification Status Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (user.isEmailVerified) Color(0xFFF0FDF4) else Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.isEmailVerified) Icons.Default.VerifiedUser else Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = if (user.isEmailVerified) Color(0xFF16A34A) else Color(0xFFEA580C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.isEmailVerified) {
                                    "Email Verified (Active Access)"
                                } else {
                                    "Pending First-Time Email Verification (OTP sent to user's inbox)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (user.isEmailVerified) Color(0xFF15803D) else Color(0xFFC2410C)
                            )
                        }

                        if (!user.isEmailVerified) {
                            TextButton(
                                onClick = { onResendVerification(user) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resend Code", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Admin Action Buttons: Toggle Lock (Password management removed from home screen for security)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.isLocked) {
                            Button(
                                onClick = { viewModel.unlockUser(user.userId) { _, _ -> } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlock Account", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.lockUser(user.userId, "Administrative Security Suspension") { _, _ -> } },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock Account", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterMemberDialog(
    viewModel: VmlViewModel,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("password") }
    var selectedRole by remember { mutableStateOf("COLLECTION_OFFICER") }
    var department by remember { mutableStateOf("Field Recovery") }
    var phone by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("VML Recover Ltd") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val roles = listOf(
        "COLLECTION_OFFICER" to "Collection Officer (Field Agent)",
        "COLLECTION_MANAGER" to "Collection Manager (Supervisor)",
        "FINANCE_OFFICER" to "Finance Officer (Reconciliation)",
        "LEGAL_OFFICER" to "Legal Officer (Litigation)",
        "DIRECTOR" to "Director (Executive Analytics)",
        "CLIENT_PORTAL" to "Company Client (Creditor)",
        "DEBTOR_PORTAL" to "Individual Debtor (Borrower)",
        "ADMIN" to "System Administrator"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("register_member_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Register New Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "As Administrator, register a new team member. A 6-digit email verification token will be generated to prove first-time access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("new_member_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("new_member_email_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Temporary Password (min 6 chars)") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("new_member_password_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Unit") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Assigned Member Role:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Role Selector Radio Buttons
                Column {
                    roles.forEach { (roleKey, roleLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedRole == roleKey,
                                onClick = { selectedRole = roleKey }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = roleLabel, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
                            if (fullName.isBlank() || email.isBlank() || tempPassword.length < 6) {
                                errorMessage = "Please enter valid member name, email, and password (at least 6 characters)."
                                return@Button
                            }
                            isSubmitting = true
                            viewModel.registerMemberByAdmin(
                                fullName = fullName,
                                email = email,
                                plainPassword = tempPassword,
                                role = selectedRole,
                                department = department,
                                phone = phone,
                                organization = organization
                            ) { success, msg ->
                                isSubmitting = false
                                if (success) {
                                    onSuccess(msg)
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.testTag("submit_register_member_button")
                    ) {
                        Text("Register & Issue Code")
                    }
                }
            }
        }
    }
}

@Composable
fun AuditTrailTab(auditLogs: List<AuditLogEntity>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().testTag("admin_audit_trail_list")
    ) {
        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor = when (log.severity) {
                        "SECURITY_ALERT", "CRITICAL" -> MaterialTheme.colorScheme.error
                        "WARNING" -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Icon(
                        imageVector = if (log.severity == "INFO") Icons.Default.Info else Icons.Default.Security,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.action,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Initiator: ${log.userName} (${log.userId})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityPolicyTab(systemConfig: SystemConfigEntity, viewModel: VmlViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var showPurgeDialog by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    if (showPurgeDialog) {
        PurgeDemoDataDialog(
            currentAdminEmail = currentUser?.email ?: "tpirez60@gmail.com",
            onDismiss = { showPurgeDialog = false },
            onConfirm = { customEmail ->
                viewModel.purgeDemoData(customEmail) { success, msg ->
                    actionFeedback = msg
                    showPurgeDialog = false
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        actionFeedback?.let { msg ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { actionFeedback = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Corporate Security & Access Governance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "First-Time Email Verification Policy: ACTIVE\nAll newly registered members must provide a 6-digit OTP code sent to their email to prove authorized access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero Password Exposure: Passwords and database hashes are never rendered on screen. Hashing utilizes PBKDF2-HMAC-SHA256 with 10,000 iterations and per-user salt.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.unlockAllAccounts() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock All System Accounts")
                }
            }
        }

        // Option 2: 1-Click Purge Demo Data & Start Fresh Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("production_mode_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (allCases.isEmpty() && allUsers.size <= 1) Color(0xFF16A34A) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (allCases.isEmpty() && allUsers.size <= 1) Icons.Default.CheckCircle else Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = if (allCases.isEmpty() && allUsers.size <= 1) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allCases.isEmpty() && allUsers.size <= 1) "Production Mode: Active (Clean Workspace)" else "Initialize Production Mode (Purge Demo Data)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (allCases.isEmpty() && allUsers.size <= 1) {
                        "Your system is currently running in clean Production Mode with 0 sample records. Any newly registered cases or members belong exclusively to your organization."
                    } else {
                        "Purge all sample loan files (Centenary Bank demo cases), mock payments, test field visits, and demo accounts with one click. Preserves only your Administrator account so you can start inputting real cases immediately."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showPurgeDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("purge_demo_data_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allCases.isEmpty() && allUsers.size <= 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allCases.isEmpty() && allUsers.size <= 1) "Reset / Re-initialize Database" else "Purge Demo Data & Start Fresh (Option 2)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PurgeDemoDataDialog(
    currentAdminEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var adminEmailInput by remember { mutableStateOf(currentAdminEmail) }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("purge_demo_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Initialize Production Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Are you sure you want to purge all demo data? This will permanently erase:\n\n• All demo recovery cases & balances\n• All mock payments & receipts\n• All demo field visits & promises to pay\n• All non-admin sample staff profiles\n\nYour Master Administrator account will be retained.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = adminEmailInput,
                    onValueChange = { adminEmailInput = it },
                    label = { Text("Primary Admin Email Address") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("production_admin_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isSubmitting = true
                            onConfirm(adminEmailInput.trim())
                        },
                        enabled = !isSubmitting && adminEmailInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_purge_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Confirm & Purge Demo Data")
                        }
                    }
                }
            }
        }
    }
}
