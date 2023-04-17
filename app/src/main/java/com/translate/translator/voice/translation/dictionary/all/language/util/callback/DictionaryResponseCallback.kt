package com.translate.translator.voice.translation.dictionary.all.language.util.callback

import com.translate.translator.voice.translation.dictionary.all.language.data.DictionaryItem
import java.lang.Exception

interface DictionaryResponseCallback {
    fun onSuccess(response: DictionaryItem?)
    fun onError(error: String?)
    fun onFailure(exception: Exception)
}