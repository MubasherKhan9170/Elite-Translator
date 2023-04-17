package com.translate.translator.voice.translation.dictionary.all.language.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem
import com.translate.translator.voice.translation.dictionary.all.language.data.SharedItem
import com.translate.translator.voice.translation.dictionary.all.language.data.WordDetailRepository
import com.translate.translator.voice.translation.dictionary.all.language.database.MultiLangItem
import com.translate.translator.voice.translation.dictionary.all.language.database.MultiTranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.work.WebScrappingWorker
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import com.google.gson.GsonBuilder

@HiltViewModel
class TranslatorMultiViewModel @Inject constructor(
    private val wordDetailRepository: WordDetailRepository,
    application: Application
) : AndroidViewModel(application) {

    /*....................................global variable for all destinations.............................................*/

    //this variable is used to have the selected languages values
    var multiselect_list: ArrayList<SharedItem> = ArrayList()

    //this variable is used to display the available languages in adapter
    val list = mutableListOf<LanguageItem>()

    /* this variable contain the translation results
    * It is used to display the result in languages in adapter */
    var resultList = mutableListOf<ResultItem>()

    // this variable is used to identify the language selection type mode
    var type: String? = "source"

    // this variable is used to check the auto detection state in case of source type mode
    var autoItemState: Boolean = false

    // this variable is used only for microphone in source input field
    var mircoLangCode: String = "None"

    /*this variable is used to have the json object of selected translation languages
    * this provides easy to add the list of items in single database table attribute*/
    var langJsonOutput: String? = null

    /*this variable is used to have the json object of getting results
    * this provides easy to add the list of items in single database table attribute*/
    var resultJsonOutput: String? = null

    /*....................................share preference.............................................*/

    // this is a singleton object to access the preferences
    val instancePreference = UserPreferencesRepository.getInstance(application.applicationContext)

    /*....................................Database Query Tasks.............................................*/
    /*create the instance of the viewModel job
    and assign the job*/
    private var viewModelJob = Job()

    /*Define the UI scope for the coroutines*/
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /* Database Table = multi_translation_history_table
    Data Model =  MultiTranslationItem
    GET Multi Language history and favourite Item*/

   /* Live Data variable of multi_translation_history_table
   this variable that would get the list from the table and assign it*/
    val MultiHistoryItems = wordDetailRepository.getAllMultiHistory()


    /*this function is used to add the translation into the multi_translation_history_table*/
    fun addToHistory() {
        uiScope.launch {
            val newItem = MultiTranslationItem(
                srcLanguage = _argumentSource.value.toString(),
                srcText = _homeText.value.toString(),
                srcCountry = sourceCountry.toString(),
                srcIso3 = sourceIso3Code.toString(),
                srcBcp47 = sourceBcp47Code.toString(),
                tarLanguage = langJsonOutput!!,
                translationText = resultJsonOutput!!,
                favStatus = false
            )
            insertToHistoryTable(newItem)
        }
    }

    // this function is used to insert the item in the table in UI coroutine scope
    private suspend fun insertToHistoryTable(item: MultiTranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getMultiHistoryTable().insert(item)
        }
    }

    /* Multi selected languages
    Define a variable that get the list from the database and assign it*/
    val selectedItemsList = wordDetailRepository.getAllLanguages()

    fun insertToMultiLangTable(list: MutableList<SharedItem>) {
        uiScope.launch {
            insertListToMultiTable(list)
        }
    }

    private suspend fun insertListToMultiTable(list: List<SharedItem>) {
        withContext(Dispatchers.IO) {
            list.forEach {
                val item = MultiLangItem(
                    langNameEN = it.langNameEN,
                    langNameLocal = it.langNameLocal,
                    countryName = it.countryName,
                    countryCode = it.countryCode,
                    bcp47 = it.bcp47,
                    iso3 = it.iso3
                )
                wordDetailRepository.getMultiLangTable().insert(item)

            }

        }
    }

    fun clearMultiTable() {
        uiScope.launch {
            deleteToMultiTable()
        }
    }

    private suspend fun deleteToMultiTable() {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getMultiLangTable().clear()
        }
    }

    /*....................................MultiTransMainFragment.............................................*/
    // Variables to be used in

    // Functions to be Called

    fun builtJsonObject() {
        Log.d(TAG, "builtJsonObject: " + multiselect_list.size)
        val gson = GsonBuilder().setPrettyPrinting().create()
        langJsonOutput = gson.toJson(multiselect_list)
        Log.d(TAG, "builtJsonObject: " + langJsonOutput)
    }

    fun builtResultJson() {
        Log.d(TAG, "builtResultJson: " + resultList.size)
        val list = mutableListOf<String>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        resultList.forEach {
            list.add(it.text!!)
        }
        resultJsonOutput = gson.toJson(list)
        Log.d(TAG, "builtResultJson" + resultJsonOutput)

    }


    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: ")
        multiselect_list.clear()
    }
    /*..............................................Language Identification.........................*/

    // get the instance of workManager instance for background work
    val workManager = WorkManager.getInstance()

    // create the instance of worker class request
    lateinit var request: OneTimeWorkRequest

    /*..............................................Language Identification.........................*/
    val languageIdentifier = LanguageIdentification
        .getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.01f)
                .build()
        )
    var langCode = "und"

    /*................................Main Fragment Variables..................................*/
    private val _homeText = MutableLiveData<String?>()
    val homeText: LiveData<String?>
        get() = _homeText

    private val _translatedHomeText = MutableLiveData<String?>()
    val translatedHomeText: LiveData<String?>
        get() = _translatedHomeText

    //Source
    private val _argumentSource = MutableLiveData<String?>()
    val argumentSource: LiveData<String?>
        get() = _argumentSource
    private var sourceCountry: String? = null
    private var sourceBcp47Code: String? = null
    var sourceIso3Code: String? = null

    //Target
    private val _argumentTarget = MutableLiveData<String?>()
    val argumentTarget: LiveData<String?>
        get() = _argumentTarget
    private var targetCountry: String? = null
    private var targetBcp47Code: String? = null
    private var targetIso3Code: String? = null

    private val _navigateToLangItemName = MutableLiveData<LanguageItem?>()
    val navigateToLangItemName: LiveData<LanguageItem?>
        get() = _navigateToLangItemName

    /*................................Language Selection Fragment Functions..................................*/
    fun onLanguageItemClicked(name: LanguageItem) {
        _navigateToLangItemName.value = name
    }

    fun onLanguageItemNavigated() {
        _navigateToLangItemName.value = null
    }

    fun setSourceItemValue(item: LanguageItem) {
        instancePreference.saveLanguage(TranslatorMultiViewModel.SOURCE_LANGUAGE, item)
        _argumentSource.value = item.langNameEN
        sourceCountry = item.countryName
        sourceBcp47Code = item.bcp47
        sourceIso3Code = item.iso3
    }

/*    fun setTargetItemValue(item: LanguageItem) {
        instancePreference.saveLanguage(TranslatorMainViewModel.TARGET_LANGUAGE, item)
        _argumentTarget.value = item.langNameEN
        targetCountry = item.countryName
        targetBcp47Code = item.bcp47
        targetIso3Code = item.iso3
    }*/

    fun getSelectedLang(): String {
        return if (type == "source") {
            argumentSource.value.toString()
        } else {
            argumentTarget.value.toString()
        }
    }

    fun getSelectedLangCountry(): String? {
        return if (type == "source") {
            sourceCountry
        } else {
            targetCountry
        }
    }

    fun getSourceLangBcp47Code(): String? {
        return sourceBcp47Code
    }

    fun getTargetLangBcp47Code(): String? {
        return targetBcp47Code
    }

    fun getSourceLangIso3Code(): String? {
        return sourceIso3Code
    }

    fun getTargetLangIso3Code(): String? {
        return targetIso3Code
    }

    fun setHomeText(text: String?) {
        _homeText.value = text
    }

    fun setHomeTextNull() {
        _homeText.value = ""
    }

    fun setTranslatedHomeText(text: String?) {
        _translatedHomeText.value = text
    }

    fun setTranslatedHomeTextNull() {
        _translatedHomeText.value = ""
    }

    internal fun doTranslation(text: String, sourceIso: String, targetIso: String) {
        request = OneTimeWorkRequestBuilder<WebScrappingWorker>()
            .setInputData(createInputDataForUri(text, sourceIso, targetIso))
            .build()
        workManager.enqueue(request)
    }

    private fun createInputDataForUri(text: String, sourceIso: String, targetIso: String): Data {
        val builder = Data.Builder()
        builder.putString("SourceLanguageCode", sourceIso)
        builder.putString("TargetLanguageCode", targetIso)
        builder.putString("SourceText", text)
        return builder.build()
    }

    fun calculateOffset(count: Int): Int {
        return (100.div(count)).toInt()
    }


/*    fun itemInsertWithSingle(item: LanguageItem) {

        Log.i(TranslatorMainViewModel.TAG, "itemInsertWithSingle: " + recentlist.size)
        recentlist.add(0, item)
        val dis = recentlist.distinctBy { it.bcp47 }.toList()
        recentlist.clear()
        recentlist.addAll(dis)
        if (recentlist.size > 5) {
            recentlist.removeLast()
        }
    }

    fun itemCheckUp(lang: String, country: String, bcp: String): Boolean {
        recentlist.forEach { item ->
            if (item.bcp47 == bcp) {
                val pos = recentlist.indexOf(item)
                val pre = recentlist.set(0, item)
                Log.i(TranslatorMainViewModel.TAG, "itemCheckUp: " + pre.langNameEN)
                Log.i(TranslatorMainViewModel.TAG, "itemCheckUp: " + pos)
                recentlist.set(pos, pre)
                return true
            }
        }
        return false
    }*/
/*..................................................Initial Block..............................*/

    init {
        autoItemState = true
        _argumentSource.value = "Auto Detected"
        sourceCountry = "Auto"
        sourceBcp47Code = "en-US"
        sourceIso3Code = "auto"


/*        val defaultSource = instancePreference.getLanguage(TranslatorMultiViewModel.SOURCE_LANGUAGE)
        Log.i(TAG, "default: "+ defaultSource)
        if(defaultSource == null){
            _argumentSource.value = "English"
            sourceCountry = "United States"
            sourceBcp47Code = "en-US"
            sourceIso3Code = "en"
        }else{
            //check auto
            autoItemState = defaultSource.langNameEN == "Auto Detected"

            _argumentSource.value = defaultSource.langNameEN
            sourceCountry = defaultSource.countryName
            sourceBcp47Code = defaultSource.bcp47
            sourceIso3Code = defaultSource.iso3
        }*/

    }


    companion object {
        private const val TAG = "TranslatorMultiViewModel"
        private const val SOURCE_LANGUAGE = "sourceLanguageItem"
    }
}