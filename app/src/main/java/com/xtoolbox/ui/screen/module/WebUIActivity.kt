package com.xtoolbox.ui.screen.module

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils

class WebUIActivity : ComponentActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webUIPath = intent.getStringExtra("webui_path") ?: run {
            finish()
            return
        }

        if (!webUIPath.startsWith("/data/adb/modules/")) {
            finish()
            return
        }

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            savePassword = false
            saveFormData = false
        }

        webView.addJavascriptInterface(KsuBridge(), "ksu")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && !url.startsWith("file:///data/adb/modules/")) {
                    return true
                }
                return false
            }
        }
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl("file://$webUIPath")
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class KsuBridge {
        @JavascriptInterface
        fun exec(command: String): String {
            val result = Shell.cmd(command).exec()
            return result.out.joinToString("\n")
        }

        @JavascriptInterface
        fun toast(message: String) {
            runOnUiThread {
                android.widget.Toast.makeText(this@WebUIActivity, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
