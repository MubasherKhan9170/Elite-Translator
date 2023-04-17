package com.translate.translator.voice.translation.dictionary.all.language

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.Observer
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils


class WebActivity : BaseActivity() {

    private var webView: WebView? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

            val key = intent.getStringExtra("key")

        this.webView = findViewById<View>(R.id.web_screen_id) as WebView

       // val webSettings: WebSettings = webView!!.settings
        webView!!.settings.javaScriptEnabled = true
      /*  webView!!.settings.loadWithOverviewMode = true
        webView!!.settings.useWideViewPort = true*/

        val webViewClient = WebViewClientImpl(this)
        webView!!.webViewClient = webViewClient

        when(key){
            "privacy" -> {
                webView!!.loadUrl("https://sites.google.com/view/gotranslatepp/home")
            }
            "term" -> {
                webView!!.loadUrl("https://sites.google.com/view/gotranslatealllanguages/home")
            }
            else ->{
                webView!!.loadUrl("https://sites.google.com/view/gotranslatepp/home")
            }

        }
    }

    override fun onResume() {
        super.onResume()

        InternetUtils.get(this).observe(this, Observer {
            webView!!.setNetworkAvailable(it)
        })
    }
}