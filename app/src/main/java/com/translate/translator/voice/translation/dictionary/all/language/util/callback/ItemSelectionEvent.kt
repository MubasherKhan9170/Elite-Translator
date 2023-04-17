package com.translate.translator.voice.translation.dictionary.all.language.util.callback

import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem

interface ItemSelectionEvent {

    fun setLanguageToTable()
    fun setResult(): MutableList<ResultItem>
    fun setSourceText(): String
    fun update():Boolean
    fun default()

}