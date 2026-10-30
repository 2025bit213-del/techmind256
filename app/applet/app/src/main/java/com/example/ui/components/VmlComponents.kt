package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmlTopAppBar(
    title: String,
    subtitle: String? = null,
    currentUser: UserEntity? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = VmlSurface,
                        fontSize = 18.sp
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = VmlSurface.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = VmlSurface
                    )
                }
            }
        },
        actions = {
            // ONLY the Logout button, clean and prominent as requested
            if (currentUser != null) {
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .background(VmlSurface.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = VmlSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VmlNavy
        )
    )
}

@Composable
fun VmlSectionHeader(
    title: String,
    subtitle: String? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        align = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = VmlTextPrimary
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = VmlTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
        if (actionButton != null) {
            actionButton()
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun formatUgx(amount: Double): String {
    return "UGX " + NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())
}

// ADMIN DELETION CONFIRMATION DIALOG
@Composable
fun AdminDeleteConfirmationDialog(
    itemType: String,
    itemId: String,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirmDelete: (adminPasswordAttempt: String, reason: String) -> Unit
) {
    var adminPassword by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Verified authorization for deletion") }
    val isAdmin = currentUser?.role == "ADMIN"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Admin Authorization",
                tint = VmlCrimson,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Administrator Authorization Required",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Deleting $itemType ($itemId) strictly requires permission from the Administrator. An irreversible audit trail will be permanently recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VmlTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (!isAdmin) {
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Enter Admin Authorization Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = VmlNavy)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Demo Admin Pass: 'admin'",
                        style = MaterialTheme.typography.labelSmall,
                        color = VmlTextSecondary,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Deletion (Audit Log)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete(adminPassword, reason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VmlCrimson)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Authorize & Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// PRINT HELPER & PRINT BUTTON COMPONENT
fun printDocumentUsingAndroidPrint(context: Context, docTitle: String, htmlOrText: String) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null && context is Activity) {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter(docTitle)
                    val printJob = printManager.print(
                        docTitle,
                        printAdapter,
                        PrintAttributes.Builder().build()
                    )
                }
            }
            val formattedHtml = """
                <html>
                <head>
                    <style>
                        body { font-family: sans-serif; padding: 24px; color: #1e293b; }
                        h1 { color: #1e3a8a; border-bottom: 2px solid #0d9488; padding-bottom: 8px; }
                        .meta { color: #64748b; font-size: 14px; margin-bottom: 16px; }
                        .content { font-size: 16px; line-height: 1.6; background: #f8fafc; padding: 16px; border-radius: 8px; }
                        .footer { margin-top: 32px; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 12px; }
                    </style>
                </head>
                <body>
                    <h1>VML RECOVER - OFFICIAL DOCUMENT</h1>
                    <div class="meta">Document: <strong>$docTitle</strong> | Generated: ${java.util.Date()}</div>
                    <div class="content">$htmlOrText</div>
                    <div class="footer">Certified by VML Recovery Enterprise Systems. All rights reserved.</div>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, formattedHtml, "text/HTML", "UTF-8", null)
        }
    } catch (e: Exception) {
        // Fallback or preview
    }
}

@Composable
fun VmlPrintButton(
    docTitle: String,
    docContent: String,
    modifier: Modifier = Modifier,
    label: String = "Print Document"
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            printDocumentUsingAndroidPrint(context, docTitle, docContent)
        },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = VmlNavy
        )
    ) {
        Icon(
            imageVector = Icons.Default.Print,
            contentDescription = "Print",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontWeight = FontWeight.SemiBold)
    }
}
