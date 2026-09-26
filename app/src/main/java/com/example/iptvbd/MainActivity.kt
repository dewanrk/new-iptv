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
            background: #101114 !important;
            font-family: -apple-system, Roboto, Arial, sans-serif !important;
        }

        header { display: none !important; }

        #ivBdTopBar {
            width: 100% !important;
            padding: 10px 14px !important;
            background: #1a1c22 !important;
            color: #ffffff !important;
            font-size: 18px !important;
            font-weight: 700 !important;
            text-align: left !important;
            border-bottom: 2px solid #e50914 !important;
        }

        /* ভিডিও প্লেয়ার - অনেক বড় বা ছোট TV স্ক্রিনেও ঠিকভাবে দেখাবে */
        .ifream {
            position: static !important;
            display: block !important;
            float: none !important;
            width: 100% !important;
            aspect-ratio: 16 / 9 !important;
            height: auto !important;
            max-height: 340px !important;
            border: none !important;
            margin: 0 auto !important;
            background: #000 !important;
        }

        .owl-filter, .container-fluid, .custom_fluid {
            width: 100% !important;
            max-width: 100% !important;
            padding: 8px 10px 30px 10px !important;
            float: none !important;
            overflow: visible !important;
            position: static !important;
            background: #101114 !important;
        }

        /* ক্যাটাগরি বাটন - স্বাভাবিক (static) পজিশনে, ভাসবে না */
        .filter-menu {
            position: static !important;
            width: 100% !important;
            padding: 10px 0 !important;
            background: #101114 !important;
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
            position: static !important;
        }
        .filter-menu ul::-webkit-scrollbar { display: none !important; }
        .filter-menu ul li {
            position: static !important;
            flex: 0 0 auto !important;
            margin: 0 5px !important;
            padding: 10px 20px !important;
            font-size: 15px !important;
            font-weight: 600 !important;
            color: #d2d5dc !important;
            background: #22252c !important;
            border-radius: 8px !important;
            border: 1px solid #33363f !important;
            cursor: pointer !important;
            white-space: nowrap !important;
        }
        .filter-menu ul li.btn-active {
            background: #e50914 !important;
            border-color: #e50914 !important;
            color: #ffffff !important;
        }

        /* চ্যানেল গ্রিড - পুরোপুরি static/normal flow-তে, isotope প্লাগিনের কোনো absolute positioning কাজ করবে না */
        .filter-item {
            position: static !important;
            width: 100% !important;
            height: auto !important;
            display: grid !important;
            grid-template-columns: repeat(3, 1fr) !important;
            gap: 10px !important;
            padding-top: 8px !important;
        }
        .filter-item .item {
            position: static !important;
            top: auto !important;
            left: auto !important;
            width: 100% !important;
            margin: 0 !important;
            float: none !important;
            background: #1a1c22 !important;
            border: 1px solid #2a2d35 !important;
        }
        .filter-item .item_content {
            position: static !important;
            padding: 4px !important;
        }
        .filter-item .item img {
            width: 100% !important;
            height: auto !important;
            display: block !important;
        }
        .filter-item .item.iv-focused {
            border-color: #e50914 !important;
            outline: 3px solid #e50914 !important;
        }

        .filter-menu[style*="text-align:center"] { display: none !important; }

        .channel-label {
            display: block !important;
            text-align: center !important;
            color: #e8e9ee !important;
            font-size: 12px !important;
            font-weight: 600 !important;
            padding: 5px 3px !important;
            white-space: normal !important;
        }

        #channelSearchBox {
            width: 92% !important;
            display: block !important;
            margin: 10px auto !important;
            padding: 11px 16px !important;
            font-size: 15px !important;
            border-radius: 8px !important;
            border: 1px solid #33363f !important;
            background: #1a1c22 !important;
            color: #ffffff !important;
            outline: none !important;
        }
        #channelSearchBox::placeholder { color: #888ea0 !important; }

        footer.footer {
            background: #1a1c22 !important;
            color: #7d8290 !important;
            padding: 12px 0 !important;
            font-size: 11px !important;
        }
    """.trimIndent()

    // নিজেদের কাস্টম ক্যাটাগরি ফিল্টার - সাইটের নিজস্ব প্লাগিনের উপর নির্ভর না করে
    private val customFilterJs = """
        (function() {
            var buttons = document.querySelectorAll('.filter-btn');
            buttons.forEach(function(btn) {
                var clone = btn.cloneNode(true);
                btn.parentNode.replaceChild(clone, btn);
            });
            var freshButtons = document.querySelectorAll('.filter-btn');
            var items = document.querySelectorAll('.filter-item .item');

            freshButtons.forEach(function(btn) {
                btn.addEventListener('click', function() {
                    freshButtons.forEach(function(b) { b.classList.remove('btn-active'); });
                    btn.classList.add('btn-active');
                    var filter = btn.getAttribute('data-filter');
                    items.forEach(function(item) {
                        if (filter === '*' || item.classList.contains(filter.replace('.', ''))) {
                            item.style.display = '';
                        } else {
                            item.style.display = 'none';
                        }
                    });
                    var searchBox = document.getElementById('channelSearchBox');
                    if (searchBox) searchBox.value = '';
                });
            });
        })();
    """.trimIndent()

    // চ্যানেলের নাম বসানো + সার্চ বক্স + TV রিমোটের জন্য ফোকাস হাইলাইট
    private val labelSearchFocusJs = """
        (function() {
            if (!document.getElementById('ivBdTopBar')) {
                var bar = document.createElement('div');
                bar.id = 'ivBdTopBar';
                bar.textContent = 'IPTV BD';
                document.body.insertBefore(bar, document.body.firstChild);
            }

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
                var content = item.querySelector('.item_content');
                if (content) content.appendChild(label);
                item.setAttribute('data-channel-name', name.toLowerCase());

                // TV রিমোট (D-pad) দিয়ে ফোকাস করা যাওয়ার জন্য
                item.setAttribute('tabindex', '0');
                item.addEventListener('focus', function() { item.classList.add('iv-focused'); });
                item.addEventListener('blur', function() { item.classList.remove('iv-focused'); });
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
                        it.style.display = (q === '' || name.indexOf(q) !== -1) ? '' : 'none';
                    });
                });
            }
        })();
    """.trimIndent()

    private val unmuteJs = """
        (function() {
            function tryUnmute() {
                try {
                    document.querySelectorAll('video').forEach(function(v) {
                        v.muted = false;
                        v.volume = 1.0;
                    });
                } catch (e) {}
                try {
                    var frame = document.querySelector('iframe.ifream');
                    if (frame && frame.contentWindow && frame.contentWindow.document) {
                        frame.contentWindow.document.querySelectorAll('video').forEach(function(v) {
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

                // পেজের নিজস্ব JS (owl-filter) প্রথমে জায়গামতো বসতে সময় দিয়ে,
                // তারপর আমাদের নিজস্ব ফিল্টার আর লেবেল বসানো হচ্ছে
                view?.postDelayed({
                    view.evaluateJavascript(labelSearchFocusJs, null)
                    view.evaluateJavascript(customFilterJs, null)
                    view.evaluateJavascript(unmuteJs, null)
                }, 400)
            }
        }
        webView.webChromeClient = WebChromeClient()

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
