package com.translate.translator.voice.translation.dictionary.all.language

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient


class WebViewClientImpl(activity: Activity?) : WebViewClient() {
    private var activity: Activity? = null

    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        if (Uri.parse(url).host == "www.google.com") return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity!!.startActivity(intent)
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
    }



    init {
        this.activity = activity
    }

}