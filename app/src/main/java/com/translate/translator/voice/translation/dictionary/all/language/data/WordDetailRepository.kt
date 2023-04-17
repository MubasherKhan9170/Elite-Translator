package com.translate.translator.voice.translation.dictionary.all.language.data

import android.content.Context
import android.util.Log

import android.widget.Toast
import androidx.lifecycle.LiveData
import com.translate.translator.voice.translation.dictionary.all.language.api.WebService
import com.translate.translator.voice.translation.dictionary.all.language.database.*
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.DictionaryResponseCallback
import com.squareup.moshi.JsonDataException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject


class WordDetailRepository @Inject constructor(
    private val webService: WebService,
    private val historyTable: TranslationDatabaseDao,
    private val recentTable: RecentDatabaseDao,
    private val multiLangTable: MultiLanguageDatabaseDao,
    private val multiHistoryTable: MultiTranslationDatabaseDao
) {
    private val webservice: WebService = webService

    fun getWordDetailResponseResult(
        word: String,
        context: Context?,
        callback: DictionaryResponseCallback
    ) {
       // val mWordDetailMutableLiveData: MutableLiveData<DictionaryItem?> = MutableLiveData<DictionaryItem?>()

        webservice.getWordDetail(word).enqueue(object : Callback<List<DictionaryItem?>> {
            override fun onResponse(
                call: Call<List<DictionaryItem?>>,
                response: Response<List<DictionaryItem?>>
            ) {

                if (response.isSuccessful) {
                    Log.d(TAG, "success " + java.lang.String.valueOf(response.code()))
                    // mWordDetailMutableLiveData.value = response.body()
                    Log.d(TAG, "success " + response.body()?.get(0))
                    callback.onSuccess(response.body()?.get(0))

                } else {
                    Log.d(TAG, "error " + java.lang.String.valueOf(response.code()))
                    callback.onError("error")
                    /*String error = null;
                try {
                    error = response.errorBody().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "error body "  + error);*/

                    if (response.code() == 500) {
                        Log.d(TAG, "Found Internal Server Error")
                       // mWordDetailMutableLiveData.value = null
                        call.cancel()
                        Toast.makeText(
                            context,
                            "Found Internal Server Error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<DictionaryItem?>>, t: Throwable) {
                call.cancel()
               // mWordDetailMutableLiveData.value = null
                callback.onError("error")
                when (t) {
                    is UnknownHostException -> {
                        // Toast.makeText(context,"Failed to make a network request call. Check Your phone's Internet Connection and try again", Toast.LENGTH_LONG).show()
                    }
                    is SocketTimeoutException -> {
                        // Toast.makeText(context, "Timeout.Your phone Internet Connection is slow down and try again", Toast.LENGTH_LONG).show()
                    }
                    is JsonDataException -> {
                        Log.e(TAG, "onFailure: Couldn't parse the json response.", t)
                        //  Toast.makeText(context, "Couldn't parse the json response.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        //Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                        Log.e(TAG, "onFailure: $t")
                    }
                }
                return
            }
        })
    }

    // these for history Table
    fun getHistoryTable(): TranslationDatabaseDao = historyTable
    fun getAllHistory(): LiveData<List<TranslationItem>> = historyTable.getAllHistory()
    fun getFavourite(): LiveData<List<TranslationItem>> = historyTable.getAllFavourite(true)

    // these for recent Table
    fun getRecentTable(): RecentDatabaseDao = recentTable
    fun getAllRecent(): LiveData<List<RecentItem>> = recentTable.getAllRecent()

    // these for multi-languages table selection
    fun getMultiLangTable(): MultiLanguageDatabaseDao = multiLangTable
    fun getAllLanguages(): LiveData<List<MultiLangItem>> = multiLangTable.getAllLangs()

    // these for multi-translation history table selection
    fun getMultiHistoryTable(): MultiTranslationDatabaseDao = multiHistoryTable
    fun getAllMultiHistory(): LiveData<List<MultiTranslationItem>> = multiHistoryTable.getAllHistory()
    fun getMultiFavourite(): LiveData<List<MultiTranslationItem>> = multiHistoryTable.getAllFavourite(true)


    companion object {
        private const val TAG = "WordDetailRepository"
    }

}