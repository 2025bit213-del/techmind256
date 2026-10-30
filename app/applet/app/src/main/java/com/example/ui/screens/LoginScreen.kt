package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.UserRole
import com.example.viewmodel.VmlViewModel

@Composable
fun LoginScreen(
    viewModel: VmlViewModel,
    modifier: Modifier = Modifier
) {
    val systemConfig by viewModel.systemConfig.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var identifier by remember { mutableStateOf("admin@vmlrecover.ug") }
    var password by remember { mutableStateOf("admin") }
    var passwordVisible by remember { mutableStateOf(false) }

    val rolesList = listOf(
        RoleCardItem(UserRole.ADMIN, "admin@vmlrecover.ug", "Administrator", "Complete Control, Safe Migrations & Admin Authorizations", Icons.Default.Shield, VmlCrimson),
        RoleCardItem(UserRole.DIRECTOR, "director@vmlrecover.ug", "Director / Founder", "Executive Portfolio & Recovery Rate Metrics", Icons.Default.Analytics, VmlAmber),
        RoleCardItem(UserRole.COLLECTION_MANAGER, "manager@vmlrecover.ug", "Collection Manager", "Case Allocation, Adding Clients & Supervision", Icons.Default.SupervisorAccount, VmlNavy),
        RoleCardItem(UserRole.COLLECTION_OFFICER, "collector@vmlrecover.ug", "Collection Officer", "Phone Calls, Field Visits & Promises to Pay", Icons.Default.Call, VmlTeal),
        RoleCardItem(UserRole.LEGAL_OFFICER, "legal@vmlrecover.ug", "Legal Officer", "Demand Notices, Court Summons & Litigation", Icons.Default.Gavel, VmlNavyDark),
        RoleCardItem(UserRole.FINANCE_OFFICER, "finance@vmlrecover.ug", "Finance Officer", "Payment Verification & Reconciliation", Icons.Default.Paid, VmlGreen),
        RoleCardItem(UserRole.CLIENT, "client@stanbic.co", "Client", "Client Portal - View All Debtors, Sort by Bank, Aging & Sector", Icons.Default.AccountBalance, VmlNavy)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VmlNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(VmlSurface.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Logo",
                            tint = VmlSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "VML RECOVER",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = VmlSurface,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Enterprise Debt Collection & Loan Recovery Platform",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = VmlSurface.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        // System Lockdown Alert Banner
        if (systemConfig?.isSystemLocked == true) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlCrimson.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCrimson)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "System Lockdown",
                            tint = VmlCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SYSTEM MAINTENANCE LOCKDOWN ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VmlCrimson
                            )
                            Text(
                                text = systemConfig?.lockdownReason ?: "Only Administrators can log in during this period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VmlTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Login Error Banner
        if (loginError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VmlCrimson.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VmlCrimson.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = VmlCrimson)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = loginError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VmlCrimson
                        )
                    }
                }
            }
        }

        // Credentials Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VmlSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sign In to Your Workspace",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VmlTextPrimary
                    )
                    Text(
                        text = "Enter your work email address or select a verified role below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VmlTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text("Email Address or User ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = VmlNavy)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VmlNavy,
                            unfocusedBorderColor = VmlCardBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = VmlNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = VmlTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VmlNavy,
                            unfocusedBorderColor = VmlCardBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.login(identifier, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isAuthenticating && identifier.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = VmlNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(color = VmlSurface, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Authenticating...")
                        } else {
                            Text("Secure Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Quick Role Picker
        item {
            Text(
                text = "Fast Role Selection (Development & Demo)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VmlTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Tap any role to immediately load credentials and enter that portal.",
                style = MaterialTheme.typography.bodyMedium,
                color = VmlTextSecondary
            )
        }

        items(rolesList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        identifier = item.email
                        password = "admin"
                        viewModel.login(item.email, "admin")
                    },
                colors = CardDefaults.cardColors(containerColor = VmlSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VmlCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(item.accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VmlTextPrimary
                            )
                            if (item.role == UserRole.CLIENT) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VmlNavy.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "NEW CLIENT PORTAL",
                                        color = VmlNavy,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = VmlTextSecondary
                        )
                        Text(
                            text = item.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = VmlNavy
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Login",
                        tint = VmlTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class RoleCardItem(
    val role: UserRole,
    val email: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: androidx.compose.ui.graphics.Color
)
