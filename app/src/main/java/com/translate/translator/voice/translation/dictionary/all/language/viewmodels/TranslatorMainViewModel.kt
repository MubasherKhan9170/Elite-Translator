package com.translate.translator.voice.translation.dictionary.all.language.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.translate.translator.voice.translation.dictionary.all.language.data.*
import com.translate.translator.voice.translation.dictionary.all.language.database.MultiLangItem
import com.translate.translator.voice.translation.dictionary.all.language.database.MultiTranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.database.RecentItem
import com.translate.translator.voice.translation.dictionary.all.language.database.TranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.DictionaryResponseCallback
import com.translate.translator.voice.translation.dictionary.all.language.work.WebScrappingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.google.gson.JsonParser

import com.google.gson.Gson
import com.translate.translator.voice.translation.dictionary.all.language.R


@HiltViewModel
class TranslatorMainViewModel @Inject constructor(
    private val wordDetailRepository: WordDetailRepository,
    application: Application
) : AndroidViewModel(application) {


    //sharepreference
    val instancePreference = UserPreferencesRepository.getInstance(application.applicationContext)


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


    /*....................................Database Query Tasks.............................................*/
    /*create the instance of the viewModel job
    and assign the job*/
    private var viewModelJob = Job()

    /*Define the UI scope for the coroutines*/
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /* Single Language history and favourite
    Define a variable that get the list from the database and assign it*/
    val historyItems = wordDetailRepository.getAllHistory()
    val favouriteItems = wordDetailRepository.getFavourite()

    fun addToHistory() {
        uiScope.launch {
            val srcWithTarget = _argumentSource.value + " To " + _argumentTarget.value
            val newItem = TranslationItem(
                srcToTarget = srcWithTarget,
                srcLanguage = _argumentSource.value.toString(),
                srcText = _homeText.value.toString(),
                srcCountry = sourceCountry.toString(),
                srcIso3 = sourceIso3Code.toString(),
                srcBcp47 = sourceBcp47Code.toString(),
                tarLanguage = _argumentTarget.value.toString(),
                translationText = _translatedHomeText.value.toString(),
                tarCountry = targetCountry.toString(),
                tarIso3 = targetIso3Code.toString(),
                tarBcp47 = targetBcp47Code.toString(),
                favStatus = false
            )
            insertToHistoryTable(newItem)
        }
    }

    private suspend fun insertToHistoryTable(item: TranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getHistoryTable().insert(item)
        }
    }

    private suspend fun updateToHistoryTable(item: TranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getHistoryTable().update(item)
        }
    }

    private suspend fun deleteToHistoryTable(item: TranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getHistoryTable().delete(item.id)
        }
    }

    fun onSetFavourite(item: TranslationItem) {
        uiScope.launch {
            if (item.favStatus) {
                item.favStatus = false
                updateToHistoryTable(item)
            } else {
                item.favStatus = true
                updateToHistoryTable(item)
            }


        }
    }

    fun onDeleteItem(item: TranslationItem) {
        uiScope.launch {
            deleteToHistoryTable(item)
        }

    }

    /* Multi Language history and favourite
   Define a variable that get the list from the database and assign it*/
    val MultiHistoryItems = wordDetailRepository.getAllMultiHistory()
    val MultiFavouriteItems = wordDetailRepository.getMultiFavourite()

    private suspend fun deleteToMultiHistoryTable(item: MultiTranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getMultiHistoryTable().delete(item.id)
        }
    }

    private suspend fun updateToHistoryTable(item: MultiTranslationItem) {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getMultiHistoryTable().update(item)
        }
    }

    fun onSetMultiFavourite(item: MultiTranslationItem) {
        uiScope.launch {
            if (item.favStatus) {
                item.favStatus = false
                updateToHistoryTable(item)
            } else {
                item.favStatus = true
                updateToHistoryTable(item)
            }


        }
    }

    fun onDeleteItem(item: MultiTranslationItem) {
        uiScope.launch {
            deleteToMultiHistoryTable(item)
        }

    }


    /*Define a variable that get the list from the database and assign it*/
    val recentItemsList = wordDetailRepository.getAllRecent()

    fun insertToRecentTable(list: MutableList<LanguageItem>) {
        uiScope.launch {
            insertListToRecentTable(list)
        }
    }

    private suspend fun insertListToRecentTable(list: List<LanguageItem>) {
        withContext(Dispatchers.IO) {
            list.forEach {
                val item = RecentItem(
                    displayName = it.displayName,
                    langNameEN = it.langNameEN,
                    langNameLocal = it.langNameLocal,
                    showCountryName = it.showCountryName,
                    countryName = it.countryName,
                    countryCode = it.countryCode,
                    bcp47 = it.bcp47,
                    iso3 = it.iso3
                )
                wordDetailRepository.getRecentTable().insert(item)

            }

        }
    }

    fun clearRecentTable() {
        uiScope.launch {
            deleteToRecentTable()
        }
    }

    private suspend fun deleteToRecentTable() {
        withContext(Dispatchers.IO) {
            wordDetailRepository.getRecentTable().clear()
        }
    }


    /*................................Main Fragment Variables..................................*/

    var item: MultiTranslationItem? = null

    fun getItemValueFromJson(text: String): List<String> {
        val gson = Gson()
        val array = JsonParser.parseString(text).asJsonArray
        val list = mutableListOf<String>()
        array.forEach {
            list.add(gson.fromJson(it, String::class.java))
        }

        return list

    }

    fun getLanguageFromJson(text: String): List<LanguageItem> {
        val gson = Gson()
        // get flags class instance
        val countryFlags = CountrySymbols.Builder(getApplication()).build()
        val array = JsonParser.parseString(text).asJsonArray
        val list = mutableListOf<LanguageItem>()
        array.forEachIndexed { index, jsonElement ->
            val element = gson.fromJson(jsonElement, SharedItem::class.java)

            Log.d(TAG, "getLanguageFromJson: " + element)


            list.add(
                LanguageItem(
                    element.displayName,
                    element.langNameEN,
                    element.langNameLocal,
                    element.showCountryName,
                    element.countryName,
                    countryFlags.getCountryFlagIcon(element.countryCode),
                    element.countryCode,
                    element.bcp47,
                    element.iso3
                )
            )
        }
        return list

    }

    fun getLangListFromJson(text: String): MutableList<SharedItem> {
        val gson = Gson()
        val array = JsonParser.parseString(text).asJsonArray
        val list = mutableListOf<SharedItem>()
        array.forEachIndexed { index, jsonElement ->
            list.add(gson.fromJson(jsonElement, SharedItem::class.java))
        }
        Log.d(TAG, "getLanguageFromJson: " + list)
        return list
    }

    fun getResultItemFromJson(language: String, text: String): MutableList<ResultItem> {
        val countryFlags = CountrySymbols.Builder(getApplication()).build()
        val gson = Gson()
        val array = JsonParser.parseString(text).asJsonArray
        val list = mutableListOf<ResultItem>()
        val lang = getLangListFromJson(language)
        array.forEachIndexed { index, jsonElement ->

            list.add(
                ResultItem(
                    lang[index].langNameEN,
                    gson.fromJson(jsonElement, String::class.java),
                    countryFlags.getCountryFlagIcon(lang[index].countryCode)
                )
            )
        }
        Log.d(TAG, "getLanguageFromJson: " + list)
        return list
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


    /*................................Main Fragment Variables..................................*/

    /*    fun identifyLanguage(text: String): String{
            var code = "und"
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    Log.d(TAG, "language code: $languageCode")
                    *//*if (languageCode != "und"){

                    Log.d(TAG, "language identifier: "+ Language(languageCode))


                }else{
                    Log.d(TAG, "language identifier: "+ Language(languageCode))
                }*//*
                code = languageCode
            }
        return code
    }*/
    /*................................Home Fragment Variables..................................*/
    private val _homeText = MutableLiveData<String?>()
    val homeText: LiveData<String?>
        get() = _homeText

    private val _translatedHomeText = MutableLiveData<String?>()
    val translatedHomeText: LiveData<String?>
        get() = _translatedHomeText


    /*................................Camera Fragment Variables..................................*/
    var imageUriOrientation = 0
    var jumpTo: String = ""
    private val _cameraSourceText = MutableLiveData<String>()
    private val _cameraTranslatedText = MutableLiveData<String>()

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?>
        get() = _selectedImageUri

    /*................................Language Selection Fragment Variables..................................*/
    var dataType: String? = null
    var dataList: String? = null
    var autoItemState: Boolean = false
    var camDetect: Boolean = false

    //Source
    private val _argumentSource = MutableLiveData<String?>()
    val argumentSource: LiveData<String?>
        get() = _argumentSource

    val _displaySrcLanguage = MutableLiveData<String?>()
    val displaySource: LiveData<String?>
        get() = _displaySrcLanguage

    private var sourceCountry: String? = null
    private var sourceBcp47Code: String? = null
    private var sourceIso3Code: String? = null

    //Target
    private val _argumentTarget = MutableLiveData<String?>()
    val argumentTarget: LiveData<String?>
        get() = _argumentTarget

    val _displayTarLanguage = MutableLiveData<String?>()
    val displayTarget: LiveData<String?>
        get() = _displayTarLanguage

    private var targetCountry: String? = null
    private var targetBcp47Code: String? = null
    private var targetIso3Code: String? = null

    private val _navigateToLangItemName = MutableLiveData<LanguageItem?>()
    val navigateToLangItemName: LiveData<LanguageItem?>
        get() = _navigateToLangItemName


    val recentlist: MutableList<LanguageItem> =
        Collections.synchronizedList(ArrayList<LanguageItem>())

    /*................................Gallery Fragment Variables..................................*/
    //The internal MutableLiveData String that stores the most recent response
    private val _images = MutableLiveData<List<ImageAttributeMediaStore>>()

    // The external immutable LiveData for the response String
    val images: LiveData<List<ImageAttributeMediaStore>>
        get() = _images

    private var contentObserver: ContentObserver? = null

    /*................................Voice Fragment Variables..................................*/
    var tabSelected: Boolean = false
    var firstPersonFocusState: Boolean = false
    var secondPersonFocusState: Boolean = false
    val voiceItemList = mutableListOf<VoiceDataItem>()

    //The internal MutableLiveData String that stores the most recent response
    val _voice = MutableLiveData<Int>()

    // The external immutable LiveData for the response String
    val voiceList: LiveData<Int>
        get() = _voice

    /*................................Dictionary Fragment Variables..................................*/
    //The internal MutableLiveData String that stores the most recent response
    private val _response = MutableLiveData<DictionaryItem?>()

    // The external immutable LiveData for the response String
    val wordResponse: LiveData<DictionaryItem?>
        get() = _response

    /*................................Main Fragment Functions..................................*/
    /*................................Camera Fragment Functions..................................*/
    fun getCameraSourceString(): Array<String> {
        return getApplication<Application>().resources.getStringArray(R.array.camera_lang_array_name_en)
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun swapAtCameraScreen(array: Array<String>, src: String, tar: String): Boolean {
        var lookUp = false
        viewModelScope.launch {
            array.forEach { item ->
                if (item == tar) {
                    val tempLang = src
                    _argumentSource.value = tar
                    _argumentTarget.value = tempLang


                    val tempCountry = sourceCountry
                    sourceCountry = targetCountry
                    targetCountry = tempCountry

                    val bcp = sourceBcp47Code
                    sourceBcp47Code = targetBcp47Code
                    targetBcp47Code = bcp

                    val iso = sourceIso3Code
                    sourceIso3Code = targetIso3Code
                    targetIso3Code = iso
                    lookUp = true
                }
            }
        }
        return lookUp
    }

    fun setImageExtractedText(text: String) {
        _cameraSourceText.value = text
    }

    fun getImageExtractedText(): String? {
        return _cameraSourceText.value
    }

    fun setImageExtractedTextTranslation(text: String) {
        _cameraTranslatedText.value = text
    }

    fun getImageExtractedTextTranslation(): String? {
        return _cameraTranslatedText.value
    }


    /*................................Home Fragment Functions..................................*/
    fun swapAtHomeScreen(src: String, tar: String): Boolean {

        var bool = false
        uiScope.launch {

            val tempLang = src
            _argumentSource.value = tar
            _argumentTarget.value = tempLang

            val tempDisplay = _displaySrcLanguage.value
            _displaySrcLanguage.value = _displayTarLanguage.value
            _displayTarLanguage.value = tempDisplay

            val tempCountry = sourceCountry
            sourceCountry = targetCountry
            targetCountry = tempCountry

            val bcp = sourceBcp47Code
            sourceBcp47Code = targetBcp47Code
            targetBcp47Code = bcp

            val iso = sourceIso3Code
            sourceIso3Code = targetIso3Code
            targetIso3Code = iso

            if (!_homeText.value.isNullOrEmpty() && !_translatedHomeText.value.isNullOrEmpty()) {
                val text = _homeText.value
                _homeText.value = _translatedHomeText.value
                _translatedHomeText.value = text
                bool = true
            } else {
                _homeText.value = ""
                bool = false
            }

            printSource()
            printTarget()

        }
        return bool
    }

    fun printSource() {
        Log.i(TAG, "printSource: " + _argumentSource.value.toString())
        Log.i(TAG, "printSource: " + sourceCountry.toString())
        Log.i(TAG, "printSource: " + sourceBcp47Code.toString())
        Log.i(TAG, "printSource: " + sourceIso3Code.toString())
    }

    fun printTarget() {
        Log.i(TAG, "printTarget: " + _argumentTarget.value.toString())
        Log.i(TAG, "printTarget: " + targetCountry.toString())
        Log.i(TAG, "printTarget: " + targetBcp47Code.toString())
        Log.i(TAG, "printTarget: " + targetIso3Code.toString())
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

    /*................................Voice Fragment Functions..................................*/
    internal fun doVoiceTranslation(text: String, src: String, tar: String) {
        request = OneTimeWorkRequestBuilder<WebScrappingWorker>()
            .setInputData(createInputVoiceDataForUri(text, src, tar))
            .build()
        workManager.enqueue(request)
    }

    private fun createInputVoiceDataForUri(text: String, src: String, tar: String): Data {
        val builder = Data.Builder()
        builder.putString("SourceLanguageCode", src)
        builder.putString("TargetLanguageCode", tar)
        builder.putString("SourceText", text)
        return builder.build()
    }

    fun addToHistoryViaVoice(src: String, tar: String) {
        uiScope.launch {
            val srcWithTarget = _argumentSource.value + " To " + _argumentTarget.value
            val newItem = TranslationItem(
                srcToTarget = srcWithTarget,
                srcLanguage = _argumentSource.value.toString(),
                srcText = src,
                srcCountry = sourceCountry.toString(),
                srcIso3 = sourceIso3Code.toString(),
                srcBcp47 = sourceBcp47Code.toString(),
                tarLanguage = _argumentTarget.value.toString(),
                translationText = tar,
                tarCountry = targetCountry.toString(),
                tarIso3 = targetIso3Code.toString(),
                tarBcp47 = targetBcp47Code.toString(),
                favStatus = false
            )
            insertToHistoryTable(newItem)
        }
    }


    /*................................Language Selection Fragment Functions..................................*/
    fun onLanguageItemClicked(name: LanguageItem) {
        _navigateToLangItemName.value = name
    }

    fun onLanguageItemNavigated() {
        _navigateToLangItemName.value = null
    }

    fun setSourceItemValue(item: LanguageItem) {
        instancePreference.saveLanguage(SOURCE_LANGUAGE, item)
        _argumentSource.value = item.langNameEN
        _displaySrcLanguage.value = item.displayName.toString()
        sourceCountry = item.countryName
        sourceBcp47Code = item.bcp47
        sourceIso3Code = item.iso3
    }

    fun setTargetItemValue(item: LanguageItem) {
        instancePreference.saveLanguage(TARGET_LANGUAGE, item)
        _argumentTarget.value = item.langNameEN
        _displayTarLanguage.value = item.displayName
        targetCountry = item.countryName
        targetBcp47Code = item.bcp47
        targetIso3Code = item.iso3
    }

    fun getSelectedLang(): String {
        return if (dataType == "source") {
            argumentSource.value.toString()
        } else {
            argumentTarget.value.toString()
        }
    }

    fun getSelectedLangCountry(): String? {
        return if (dataType == "source") {
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


    fun itemInsertWithSingle(item: LanguageItem) {

        recentlist.add(0, item)
        Log.i(TAG, "itemInsertWithSingle: " + recentlist.size)
        val dis = recentlist.distinctBy { it.bcp47 }.toList()
        recentlist.clear()
        Log.i(TAG, "itemInsertWithSingle: " + dis.size)
        recentlist.addAll(dis)
        if (recentlist.size > 5) {
            recentlist.removeLast()
        }
    }

    fun itemCheckUp(lang: String, country: String, bcp: String): Boolean {
        Log.i(TAG, "itemCheckUp: size" + recentlist.size)
        recentlist.forEach { item ->
            if (item.bcp47 == bcp) {
                val pos = recentlist.indexOf(item)
                val pre = recentlist.set(0, item)
                Log.i(TAG, "itemCheckUp: " + pre.langNameEN)
                Log.i(TAG, "itemCheckUp: " + pos)
                recentlist.set(pos, pre)
                return true
            }
        }
        return false
    }

    /*................................Gallery Fragment Functions..................................*/
    /**
     * Performs a one shot load of images from [MediaStore.Images.Media.EXTERNAL_CONTENT_URI] into
     * the [_images] [LiveData] above.
     */
    fun loadImages() {
        viewModelScope.launch {
            val imageList = queryImages()
            _images.postValue(imageList)

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadImages()
                }
            }
        }
    }

    private suspend fun queryImages(): List<ImageAttributeMediaStore> {
        val images = mutableListOf<ImageAttributeMediaStore>()

        /**
         * Working with [ContentResolver]s can be slow, so we'll do this off the main
         * thread inside a coroutine.
         */
        withContext(Dispatchers.IO) {

            /**
             * A key concept when working with Android [ContentProvider]s is something called
             * "projections". A projection is the list of columns to request from the provider,
             * and can be thought of (quite accurately) as the "SELECT ..." clause of a SQL
             * statement.
             *
             * It's not _required_ to provide a projection. In this case, one could pass `null`
             * in place of `projection` in the call to [ContentResolver.query], but requesting
             * more data than is required has a performance impact.
             *
             * For this sample, we only use a few columns of data, and so we'll request just a
             * subset of columns.
             */
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.ORIENTATION
            )

            /**
             * The `selection` is the "WHERE ..." clause of a SQL statement. It's also possible
             * to omit this by passing `null` in its place, and then all rows will be returned.
             * In this case we're using a selection based on the date the image was taken.
             *
             * Note that we've included a `?` in our selection. This stands in for a variable
             * which will be provided by the next variable.
             */
            val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"

            /**
             * The `selectionArgs` is a list of values that will be filled in for each `?`
             * in the `selection`.
             */
            val selectionArgs = arrayOf(
                // Release day of the G1. :)
                dateToTimestamp(day = 22, month = 10, year = 2008).toString()
            )

            /**
             * Sort order to use. This can also be null, which will use the default sort
             * order. For [MediaStore.Images], the default sort order is ascending by date taken.
             */
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                /**
                 * In order to retrieve the data from the [Cursor] that's returned, we need to
                 * find which index matches each column that we're interested in.
                 *
                 * There are two ways to do this. The first is to use the method
                 * [Cursor.getColumnIndex] which returns -1 if the column ID isn't found. This
                 * is useful if the code is programmatically choosing which columns to request,
                 * but would like to use a single method to parse them into objects.
                 *
                 * In our case, since we know exactly which columns we'd like, and we know
                 * that they must be included (since they're all supported from API 1), we'll
                 * use [Cursor.getColumnIndexOrThrow]. This method will throw an
                 * [IllegalArgumentException] if the column named isn't found.
                 *
                 * In either case, while this method isn't slow, we'll want to cache the results
                 * to avoid having to look them up for each row.
                 */
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val orientationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)

                Log.i(TAG, "Found ${cursor.count} images")
                while (cursor.moveToNext()) {

                    // Here we'll use the column index's that we found above.
                    val id = cursor.getLong(idColumn)
                    val dateModified =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val displayName = cursor.getString(displayNameColumn)


                    /**
                     * This is one of the trickiest parts:
                     *
                     * Since we're accessing images (using
                     * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
                     * as the base URI and append the ID of the image to it.
                     *
                     * This is the exact same way to do it when working with [MediaStore.Video] and
                     * [MediaStore.Audio] as well. Whatever `Media.EXTERNAL_CONTENT_URI` you
                     * query to get the items is the base, and the ID is the document to
                     * request there.
                     */
                    /**
                     * This is one of the trickiest parts:
                     *
                     * Since we're accessing images (using
                     * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
                     * as the base URI and append the ID of the image to it.
                     *
                     * This is the exact same way to do it when working with [MediaStore.Video] and
                     * [MediaStore.Audio] as well. Whatever `Media.EXTERNAL_CONTENT_URI` you
                     * query to get the items is the base, and the ID is the document to
                     * request there.
                     */
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val imageOrientation = cursor.getInt(orientationColumn)

                    val image = ImageAttributeMediaStore(
                        id,
                        displayName,
                        dateModified,
                        contentUri,
                        imageOrientation
                    )
                    images += image

                    // For debugging, we'll output the image objects we create to logcat.
                    Log.v(TAG, "Added image: $image")
                }
            }
        }

        Log.v(TAG, "Found ${images.size} images")
        return images
    }

    /**
     * Convenience method to convert a day/month/year date into a UNIX timestamp.
     *
     * We're suppressing the lint warning because we're not actually using the date formatter
     * to format the date to display, just to specify a format to use to parse it, and so the
     * locale warning doesn't apply.
     */
    @Suppress("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("dd.MM.yyyy").let { formatter ->
            TimeUnit.MICROSECONDS.toSeconds(formatter.parse("$day.$month.$year")?.time ?: 0)
        }


    /*................................Dictionary Fragment Functions..................................*/
    // this function is called when search the word for definition
    fun searchWordDefinition(word: String) {
        wordDetailRepository.getWordDetailResponseResult(
            word,
            null,
            object : DictionaryResponseCallback {
                override fun onSuccess(response: DictionaryItem?) {
                    _response.value = response
                }

                override fun onError(error: String?) {
                    Log.e(TAG, "onError: $error")
                    _response.value = null

                }

                override fun onFailure(exception: Exception) {
                    //   TODO("Not yet implemented")
                    _response.value = null
                }
            })
    }

    /*................................View Model Class object and callbacks..................................*/
    // for constant variables and static functions declaration and initialization
    companion object {
        private const val TAG = "TranslatorMainVIewModel"
        const val SOURCE_LANGUAGE = "sourceLanguageItem"
        const val TARGET_LANGUAGE = "targetLanguageItem"

    }

    init {
        _homeText.value = ""
        _translatedHomeText.value = ""

        dataType = "source"
        dataList = "home"


        val defaultSource = instancePreference.getLanguage(SOURCE_LANGUAGE)
        Log.i(TAG, "default: " + defaultSource)
        if (defaultSource == null) {
            _argumentSource.value = "English"
            _displaySrcLanguage.value = "English"
            sourceCountry = "United States"
            sourceBcp47Code = "en-US"
            sourceIso3Code = "en"
        } else {
            _argumentSource.value = defaultSource.langNameEN
            _displaySrcLanguage.value = defaultSource.displayName
            sourceCountry = defaultSource.countryName
            sourceBcp47Code = defaultSource.bcp47
            sourceIso3Code = defaultSource.iso3
        }
        val defaultTarget = instancePreference.getLanguage(TARGET_LANGUAGE)
        if (defaultTarget == null) {
            _argumentTarget.value = "Urdu"
            _displayTarLanguage.value = "Urdu"
            targetCountry = "Pakistan"
            targetBcp47Code = "ur-PK"
            targetIso3Code = "ur"

        } else {
            _argumentTarget.value = defaultTarget.langNameEN
            _displayTarLanguage.value = defaultTarget.displayName
            targetCountry = defaultTarget.countryName
            targetBcp47Code = defaultTarget.bcp47
            targetIso3Code = defaultTarget.iso3
        }

        firstPersonFocusState = true
        secondPersonFocusState = false
        _voice.value = 0

    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: ")
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
        viewModelJob.cancel()
        voiceItemList.clear()

    }


}
/*................................Gallery Fragment Content Observer..................................*/
/**
 * Convenience extension method to register a [ContentObserver] given a lambda.
 */
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}