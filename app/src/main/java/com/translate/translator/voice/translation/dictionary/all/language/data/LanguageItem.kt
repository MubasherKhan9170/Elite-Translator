package com.translate.translator.voice.translation.dictionary.all.language.data

import android.graphics.drawable.BitmapDrawable

data class LanguageItem(
    val displayName: String? = "Urdu",
    val langNameEN: String? = "Urdu",
    val langNameLocal: String? = "اردو",
    val showCountryName: String? = "Pakistan",
    val countryName: String? = "Pakistan",
    var flagIcon: BitmapDrawable? = null,
    val countryCode: String? = "PK",
    val bcp47: String? = "en-US",
    val iso3: String? = "en"
)
