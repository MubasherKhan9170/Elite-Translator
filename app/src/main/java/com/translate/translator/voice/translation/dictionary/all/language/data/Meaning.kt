package com.translate.translator.voice.translation.dictionary.all.language.data

import com.squareup.moshi.Json

data class Meaning(
    @field:Json(name = "partOfSpeech")
    var partOfSpeech: String? = null,
    @field:Json(name = "definitions")
    var definitions: List<Definition>? = null)