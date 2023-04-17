package com.translate.translator.voice.translation.dictionary.all.language.data

import com.squareup.moshi.Json

data class DictionaryItem(@field:Json(name = "word") var word: String? = null,
                          @field:Json(name = "phonetic") var phonetic: String? = null,
                          @field:Json(name = "phonetics") var phonetics: List<Phonetic>? = null,
                          @field:Json(name = "origin") var origin: String? = null,
                          @field:Json(name = "meanings") var meanings: List<Meaning>? = null,)