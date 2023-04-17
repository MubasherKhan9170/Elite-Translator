package com.translate.translator.voice.translation.dictionary.all.language.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.work.WebScrappingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TranslatorDrawOverViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    var sourceIso3Code: String?
    private var sourceBcp47Code: String?
    private var sourceCountry: String?
    private var argumentSource: String?

    private var targetIso3Code: String?
    private var targetBcp47Code: String?
    private var targetCountry: String?
    private var argumentTarget: String?



    //sharepreference
    val instancePreference = UserPreferencesRepository.getInstance(application.applicationContext)
    // get the instance of workManager instance for background work
    val workManager = WorkManager.getInstance()

    // create the instance of worker class request
    lateinit var request: OneTimeWorkRequest

    internal fun doTranslation(text: String) {
        request = OneTimeWorkRequestBuilder<WebScrappingWorker>()
            .setInputData(createInputDataForUri(text))
            .build()
        workManager.enqueue(request)
    }

    private fun createInputDataForUri(text: String): Data {
        val builder = Data.Builder()
        builder.putString("SourceLanguageCode", sourceIso3Code)
        builder.putString("TargetLanguageCode", targetIso3Code)
        builder.putString("SourceText", text)
        return builder.build()
    }

    init {
        val defaultSource = instancePreference.getLanguage(SOURCE_LANGUAGE)
        Log.i(TAG, "default: "+ defaultSource)
        if(defaultSource == null){
            argumentSource = "English"
            sourceCountry = "United States"
            sourceBcp47Code = "en-US"
            sourceIso3Code = "en"
        }else{
            argumentSource = defaultSource.langNameEN
            sourceCountry = defaultSource.countryName
            sourceBcp47Code = defaultSource.bcp47
            sourceIso3Code = defaultSource.iso3
        }
        val defaultTarget = instancePreference.getLanguage(TARGET_LANGUAGE)
        if(defaultTarget ==  null){
            argumentTarget = "Urdu"
            targetCountry = "Pakistan"
            targetBcp47Code = "ur-PK"
            targetIso3Code = "ur"

        }else{
            argumentTarget = defaultTarget.langNameEN
            targetCountry = defaultTarget.countryName
            targetBcp47Code = defaultTarget.bcp47
            targetIso3Code = defaultTarget.iso3
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: ")

    }

    companion object {
        private const val TAG = "TranslatorDrawOverViewM"
        private const val SOURCE_LANGUAGE = "sourceLanguageItem"
        private const val TARGET_LANGUAGE = "targetLanguageItem"
    }
}