package com.translate.translator.voice.translation.dictionary.all.language.data

import com.squareup.moshi.Json

data class Definition(
    @field:Json(name = "definition") var definition: String? = null,
    @field:Json(name = "example") var example: String? = null,
    @field:Json(name = "synonyms") var synonyms: List<Any>? = null,
    @field:Json(name = "antonyms") var antonyms: List<Any>? = null)