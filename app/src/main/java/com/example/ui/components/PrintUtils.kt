package com.example.ui.components

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

object PrintUtils {

    fun printHtmlDocument(context: Context, jobName: String, htmlContent: String) {
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val adapter = view?.createPrintDocumentAdapter(jobName)
                    if (printManager != null && adapter != null) {
                        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
                    }
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun generateReceiptHtml(
        receiptNo: String,
        debtorName: String,
        loanNo: String,
        amount: Double,
        channel: String,
        clientName: String,
        date: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: sans-serif; padding: 20px; color: #1e293b; }
                    .header { text-align: center; border-bottom: 2px solid #0284c7; padding-bottom: 10px; margin-bottom: 20px; }
                    .title { font-size: 20px; font-weight: bold; color: #0284c7; }
                    .receipt-row { display: flex; justify-content: space-between; margin: 8px 0; border-bottom: 1px dashed #cbd5e1; padding-bottom: 4px; }
                    .label { font-weight: bold; color: #64748b; }
                    .value { font-weight: bold; }
                    .total { font-size: 18px; color: #16a34a; font-weight: bold; margin-top: 15px; text-align: right; }
                    .footer { text-align: center; margin-top: 30px; font-size: 11px; color: #94a3b8; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">VML RECOVER LTD</div>
                    <div>Official Debt Recovery Receipt</div>
                </div>
                <div class="receipt-row"><span class="label">Receipt Number:</span><span class="value">$receiptNo</span></div>
                <div class="receipt-row"><span class="label">Date:</span><span class="value">$date</span></div>
                <div class="receipt-row"><span class="label">Debtor Name:</span><span class="value">$debtorName</span></div>
                <div class="receipt-row"><span class="label">Loan Account:</span><span class="value">$loanNo</span></div>
                <div class="receipt-row"><span class="label">Creditor / Client:</span><span class="value">$clientName</span></div>
                <div class="receipt-row"><span class="label">Payment Channel:</span><span class="value">$channel</span></div>
                <div class="total">Amount Received: UGX ${String.format("%,.0f", amount)}</div>
                <div class="footer">
                    VML Recover Security Kernel • Validated & Digitally Signed • Kampala, Uganda
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
