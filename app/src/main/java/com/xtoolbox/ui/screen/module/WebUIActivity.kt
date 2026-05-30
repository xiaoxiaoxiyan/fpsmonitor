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
    private var moduleId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webUIPath = intent.getStringExtra("webui_path") ?: run {
            finish()
            return
        }
        moduleId = intent.getStringExtra("module_id") ?: ""

        if (!webUIPath.startsWith("/data/adb/modules/")) {
            finish()
            return
        }

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            savePassword = false
            saveFormData = false
        }

        webView.addJavascriptInterface(KsuWebUIXBridge(), "ksu")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
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

    inner class KsuWebUIXBridge {
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

        @JavascriptInterface
        fun getModuleId(): String = moduleId

        @JavascriptInterface
        fun getModuleDir(): String {
            val webUIPath = intent.getStringExtra("webui_path") ?: ""
            val segments = webUIPath.split("/")
            val modulesIndex = segments.indexOf("modules")
            return if (modulesIndex >= 0 && modulesIndex + 1 < segments.size) {
                "/data/adb/modules/${segments[modulesIndex + 1]}"
            } else ""
        }

        @JavascriptInterface
        fun execAsRoot(command: String): String {
            val shell = Shell.getShell()
            if (!shell.isRoot) return "ERROR: No root access"
            val result = Shell.cmd(command).exec()
            return result.out.joinToString("\n")
        }
    }
}
