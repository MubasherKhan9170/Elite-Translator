package com.translate.translator.voice.translation.dictionary.all.language.work

import android.content.Context
import android.net.TrafficStats
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup
import java.net.URLEncoder
class WebScrappingWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
        private const val TAG = "WebScrappingWorker"
    }

    /**
     * A coroutine-friendly method to do your work.
     * Note: In recent work version upgrade, 1.0.0-alpha12 and onwards have a breaking change.
     * The doWork() function now returns Result instead of Payload because they have combined Payload into Result.
     * Read more here - https://developer.android.com/jetpack/androidx/releases/work#1.0.0-alpha12
     */
    override fun doWork(): Result {
        // ADD THIS LINE
        val sl = inputData.getString("SourceLanguageCode")
        var tl = inputData.getString("TargetLanguageCode")
        val text = inputData.getString("SourceText")
        Log.d(TAG, "doWork: $sl")
        Log.d(TAG, "doWork: $tl")
        Log.d(TAG, "doWork: $text")
        if(tl == "zh"){
            tl = "zh-CN"
        }


        TrafficStats.setThreadStatsTag(0xF00D)
        try {
            // Make network request using HttpClient.execute()
            val url = "https://translate.google.com/m?hl=en&sl=$sl&tl=$tl&q=" + URLEncoder.encode(text, "UTF-8")

            val doc = Jsoup.connect(url).get()


            val element = doc.getElementsByClass("result-container")


            if (element.text() != "" && !TextUtils.isEmpty(element.text())) {
                Log.d(TAG, "doWork: "+ element.text())

                return Result.success(createOutputData(element.text()))
            }
            return Result.failure()
        } finally {
            TrafficStats.clearThreadStatsTag()
        }


    }

    // Method to create output data
    private fun createOutputData(result: String): Data {
        return Data.Builder()
            .putString("translatedText", result)
            .build()
    }

}
