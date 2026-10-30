package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.viewmodel.UserRole
import com.example.viewmodel.VmlViewModel

data class DemoRoleCredential(
    val title: String,
    val roleBadge: String,
    val email: String,
    val description: String,
    val badgeColor: Color,
    val badgeBg: Color
)

@Composable
fun LoginScreen(
    viewModel: VmlViewModel,
    onLoginSuccess: () -> Unit
) {
    var identifier by remember { mutableStateOf("admin@vmlrecover.ug") }
    var password by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showEmergencyOverrideDialog by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val isAccountLockedOut by viewModel.isAccountLockedOut.collectAsState()
    val isPendingVerification by viewModel.isPendingEmailVerification.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    var showDemoSwitcher by remember { mutableStateOf(true) }

    // Email Verification Dialog for first-time access
    if (isPendingVerification) {
        EmailVerificationDialog(
            viewModel = viewModel,
            onSuccess = onLoginSuccess,
            onDismiss = { viewModel.cancelEmailVerification() }
        )
    }

    // Emergency Administrator Recovery Key Dialog
    if (showEmergencyOverrideDialog) {
        EmergencyOverrideDialog(
            viewModel = viewModel,
            onDismiss = { showEmergencyOverrideDialog = false }
        )
    }

    val demoRoles = listOf(
        DemoRoleCredential(
            title = "Administrator",
            roleBadge = "Admin Master",
            email = "admin@vmlrecover.ug",
            description = "Member registration, security, and full oversight",
            badgeColor = Color(0xFF0284C7),
            badgeBg = Color(0xFFE0F2FE)
        ),
        DemoRoleCredential(
            title = "Director / Founder",
            roleBadge = "Executive",
            email = "director@vmlrecover.ug",
            description = "Portfolio analytics and performance tracking",
            badgeColor = Color(0xFF7C3AED),
            badgeBg = Color(0xFFF3E8FF)
        ),
        DemoRoleCredential(
            title = "Collection Officer",
            roleBadge = "Field Agent",
            email = "officer@vmlrecover.ug",
            description = "Field visits, phone calls, and promise logging",
            badgeColor = Color(0xFF0D9488),
            badgeBg = Color(0xFFCCFBF1)
        ),
        DemoRoleCredential(
            title = "Finance Officer",
            roleBadge = "Accounts",
            email = "finance@vmlrecover.ug",
            description = "Payment reconciliation and receipt issuing",
            badgeColor = Color(0xFF16A34A),
            badgeBg = Color(0xFFDCFCE7)
        ),
        DemoRoleCredential(
            title = "Debtor (First-Time Verification)",
            roleBadge = "Requires Verification",
            email = "debtor@vmlrecover.ug",
            description = "Tests first-time access email verification code",
            badgeColor = Color(0xFFEA580C),
            badgeBg = Color(0xFFFFEDD5)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Branding & Hero
            Card(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .size(105.dp)
                    .testTag("app_logo_card"),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vml_logo),
                    contentDescription = "VML Recover Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VML RECOVER",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            Text(
                text = "Corporate Loan Recovery & Debt Collection System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Sign In to Your Workspace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            viewModel.clearLoginError()
                        },
                        label = { Text("Email or Staff ID") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 62.dp)
                            .testTag("email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearLoginError()
                        },
                        label = { Text("Password") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 62.dp)
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = loginError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.login(identifier, password) { success ->
                                if (success) onLoginSuccess()
                            }
                        },
                        enabled = identifier.isNotBlank() && password.isNotBlank() && !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isAccountLockedOut) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { showEmergencyOverrideDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("emergency_override_button")
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergency Administrator Recovery Key",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Restricted Access Policy Card (NO public self-registration!)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restricted_registration_policy_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin-Only Member Registration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Only authorized System Administrators can register new members to VML Recover. Accounts require email verification upon first login.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (allUsers.size <= 1) {
                // Production Mode: Zero Demo Accounts
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("production_mode_login_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Production Workspace Active", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("This terminal is in clean Production Mode with zero sample data. All activities are recorded to the live corporate audit trail.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (showDemoSwitcher) {
                // Fast Demo Role Switcher with toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Demo Accounts (Testing):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showDemoSwitcher = false }) {
                        Text("Hide", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(demoRoles) { role ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .clickable {
                                    identifier = role.email
                                    password = "password"
                                    viewModel.clearLoginError()
                                }
                                .testTag("demo_role_${role.title.lowercase().replace(" ", "_")}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = role.badgeBg
                                ) {
                                    Text(
                                        text = role.roleBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = role.badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = role.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = role.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmergencyOverrideDialog(
    viewModel: VmlViewModel,
    onDismiss: () -> Unit
) {
    var recoveryKeyInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("emergency_override_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Administrator Recovery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your cryptographically registered Recovery Key to unlock the primary administrator account (Hint: VML-SECURE-ADMIN-2026):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = recoveryKeyInput,
                    onValueChange = { recoveryKeyInput = it },
                    label = { Text("Emergency Recovery Key") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).testTag("recovery_key_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg ?: "",
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
                            isSubmitting = true
                            viewModel.overrideLoginAdmin(recoveryKeyInput) { success, msg ->
                                isSubmitting = false
                                if (success) {
                                    onDismiss()
                                } else {
                                    errorMsg = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("submit_recovery_key_button")
                    ) {
                        Text("Unlock Administrator")
                    }
                }
            }
        }
    }
}
