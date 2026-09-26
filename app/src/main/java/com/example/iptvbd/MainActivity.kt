package com.example.iptvbd

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val customCss = """
        * { box-sizing: border-box !important; }
        html, body {
            width: 100% !important;
            overflow-x: hidden !important;
            margin: 0 !important;
            padding: 0 !important;
        }
        header { text-align: center !important; padding: 4px 0 !important; }
        .ifream {
            position: relative !important;
            display: block !important;
            float: none !important;
            width: 100% !important;
            height: 56vw !important;
            max-height: 260px !important;
            border: none !important;
            margin: 0 auto !important;
        }
        .owl-filter, .container-fluid, .custom_fluid {
            width: 100% !important;
            max-width: 100% !important;
            padding: 0 8px !important;
            float: none !important;
            overflow: visible !important;
        }
        .filter-item {
            display: flex !important;
            flex-wrap: wrap !important;
            justify-content: center !important;
            width: 100% !important;
        }
        .filter-item .item {
            width: 30% !important;
            margin: 1.5% !important;
            float: none !important;
        }
        .filter-item .item img {
            width: 100% !important;
            height: auto !important;
        }
        .filter-menu ul {
            display: flex !important;
            flex-wrap: wrap !important;
            justify-content: center !important;
            padding: 4px !important;
            margin: 0 !important;
        }
        .filter-menu ul li {
            margin: 3px !important;
            font-size: 12px !important;
        }
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val viewportJs = """
                    (function() {
                        var meta = document.querySelector('meta[name="viewport"]');
                        if (!meta) {
                            meta = document.createElement('meta');
                            meta.name = 'viewport';
                            document.getElementsByTagName('head')[0].appendChild(meta);
                        }
                        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes';
                    })();
                """.trimIndent()
                view?.evaluateJavascript(viewportJs, null)

                val cssJs = """
                    (function() {
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = `$customCss`;
                        document.head.appendChild(style);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(cssJs, null)
            }
        }
        webView.webChromeClient = WebChromeClient()

        // আপনার ওয়েবসাইটের লিংক
        webView.loadUrl("http://iptvidn.com/")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
