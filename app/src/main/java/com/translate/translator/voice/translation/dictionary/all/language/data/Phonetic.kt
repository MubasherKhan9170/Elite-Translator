package com.translate.translator.voice.translation.dictionary.all.language.data

import com.squareup.moshi.Json

class Phonetic (@field:Json(name = "text") var text: String? = null,
                @field:Json(name = "audio") var audio: String? = null)

