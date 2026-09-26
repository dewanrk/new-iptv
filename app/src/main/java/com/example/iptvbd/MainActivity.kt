package com.example.iptvbd

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val blockedDomains = listOf("fogsham.com")

    private val customCss = """
        * { box-sizing: border-box !important; }
        html, body {
            width: 100% !important;
            overflow-x: hidden !important;
            margin: 0 !important;
            padding: 0 !important;
            background: #0e0f13 !important;
            font-family: -apple-system, Roboto, Arial, sans-serif !important;
        }
        header {
            text-align: center !important;
            padding: 6px 0 !important;
            background: #14161d !important;
        }
        .ifream {
            position: sticky !important;
            top: 0 !important;
            z-index: 50 !important;
            display: block !important;
            float: none !important;
            width: 100% !important;
            height: 56vw !important;
            max-height: 260px !important;
            border: none !important;
            margin: 0 auto !important;
            background: #000 !important;
            box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;
        }
        .owl-filter, .container-fluid, .custom_fluid {
            width: 100% !important;
            max-width: 100% !important;
            padding: 6px 8px 24px 8px !important;
            float: none !important;
            overflow: visible !important;
            background: #0e0f13 !important;
        }
        .filter-menu {
            position: sticky !important;
            top: 56vw !important;
            z-index: 40 !important;
            background: #0e0f13 !important;
            padding: 8px 0 !important;
        }
        .filter-menu ul {
            display: flex !important;
            flex-wrap: nowrap !important;
            overflow-x: auto !important;
            -webkit-overflow-scrolling: touch !important;
            justify-content: flex-start !important;
            padding: 4px 6px !important;
            margin: 0 !important;
            list-style: none !important;
            scrollbar-width: none !important;
        }
        .filter-menu ul::-webkit-scrollbar { display: none !important; }
        .filter-menu ul li {
            flex: 0 0 auto !important;
            margin: 0 5px !important;
            padding: 9px 18px !important;
            font-size: 14px !important;
            font-weight: 600 !important;
            color: #cfd3dc !important;
            background: #1e212b !important;
            border-radius: 999px !important;
            border: 1px solid #2c303c !important;
            cursor: pointer !important;
            white-space: nowrap !important;
        }
        .filter-menu ul li.btn-active {
            background: #e50914 !important;
            border-color: #e50914 !important;
            color: #ffffff !important;
        }
        .filter-item {
            display: flex !important;
            flex-wrap: wrap !important;
            justify-content: center !important;
            width: 100% !important;
            gap: 10px !important;
            padding-top: 6px !important;
        }
        .filter-item .item {
            width: 29% !important;
            margin: 0 !important;
            float: none !important;
            background: #1a1c24 !important;
            border-radius: 14px !important;
            overflow: hidden !important;
            border: 1px solid #262a35 !important;
            transition: transform 0.12s ease, border-color 0.12s ease !important;
        }
        .filter-item .item:active {
            transform: scale(0.94) !important;
            border-color: #e50914 !important;
        }
        .filter-item .item_content {
            padding: 6px !important;
        }
        .filter-item .item img {
            width: 100% !important;
            height: auto !important;
            border-radius: 9px !important;
            display: block !important;
        }
        .filter-menu[style*="text-align:center"] { display: none !important; }
        footer.footer {
            background: #14161d !important;
            color: #7d8290 !important;
            padding: 14px 0 !important;
            font-size: 12px !important;
        }
        .channel-label {
            display: block !important;
            text-align: center !important;
            color: #e8e9ee !important;
            font-size: 12px !important;
            font-weight: 600 !important;
            padding: 6px 4px 8px 4px !important;
            white-space: normal !important;
        }
        #channelSearchBox {
            width: 92% !important;
            display: block !important;
            margin: 10px auto !important;
            padding: 12px 16px !important;
            font-size: 15px !important;
            border-radius: 12px !important;
            border: 1px solid #2c303c !important;
            background: #1e212b !important;
            color: #ffffff !important;
            outline: none !important;
        }
        #channelSearchBox::placeholder { color: #888ea0 !important; }
    """.trimIndent()

    private val labelAndSearchJs = """
        (function() {
            var items = document.querySelectorAll('.filter-item .item');
            items.forEach(function(item) {
                if (item.querySelector('.channel-label')) return;
                var link = item.querySelector('a[onclick]');
                if (!link) return;
                var match = link.getAttribute('onclick').match(/stream=([^'"]+)/);
                if (!match) return;
                var raw = decodeURIComponent(match[1]);
                var name = raw.replace(/[-_]+/g, ' ').trim();
                name = name.replace(/\b\w/g, function(c) { return c.toUpperCase(); });
                var label = document.createElement('span');
                label.className = 'channel-label';
                label.textContent = name;
                label.setAttribute('data-name', name.toLowerCase());
                var content = item.querySelector('.item_content');
                if (content) content.appendChild(label);
                item.setAttribute('data-channel-name', name.toLowerCase());
            });

            if (!document.getElementById('channelSearchBox')) {
                var searchBox = document.createElement('input');
                searchBox.type = 'text';
                searchBox.id = 'channelSearchBox';
                searchBox.placeholder = 'চ্যানেলের নাম লিখে খুঁজুন...';
                var filterMenu = document.querySelector('.filter-menu');
                if (filterMenu && filterMenu.parentNode) {
                    filterMenu.parentNode.insertBefore(searchBox, filterMenu.nextSibling);
                }
                searchBox.addEventListener('input', function() {
                    var q = searchBox.value.trim().toLowerCase();
                    var allItems = document.querySelectorAll('.filter-item .item');
                    allItems.forEach(function(it) {
                        var name = it.getAttribute('data-channel-name') || '';
                        if (q === '' || name.indexOf(q) !== -1) {
                            it.style.display = '';
                        } else {
                            it.style.display = 'none';
                        }
                    });
                });
            }
        })();
    """.trimIndent()

    private val unmuteJs = """
        (function() {
            function tryUnmute() {
                try {
                    var vids = document.querySelectorAll('video');
                    vids.forEach(function(v) {
                        v.muted = false;
                        v.volume = 1.0;
                    });
                } catch (e) {}
                try {
                    var frame = document.querySelector('iframe.ifream');
                    if (frame && frame.contentWindow && frame.contentWindow.document) {
                        var innerVids = frame.contentWindow.document.querySelectorAll('video');
                        innerVids.forEach(function(v) {
                            v.muted = false;
                            v.volume = 1.0;
                        });
                    }
                } catch (e) {}
            }
            tryUnmute();
            var count = 0;
            var interval = setInterval(function() {
                tryUnmute();
                count++;
                if (count > 15) clearInterval(interval);
            }, 1000);
        })();
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

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: ""
                if (blockedDomains.any { url.contains(it) }) {
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                }
                return super.shouldInterceptRequest(view, request)
            }

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

                view?.evaluateJavascript(labelAndSearchJs, null)

                view?.evaluateJavascript(unmuteJs, null)
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
