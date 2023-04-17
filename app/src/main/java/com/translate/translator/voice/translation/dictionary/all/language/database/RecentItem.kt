package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_language_table")
data class RecentItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "display_name")
    var displayName: String? = "Urdu",
    @ColumnInfo(name = "english_lang_name")
    var langNameEN: String? = "Urdu",
    @ColumnInfo(name = "local_lang_name")
    var langNameLocal: String? = "اردو",
    @ColumnInfo(name = "show_country_name")
    var showCountryName: String? = "Pakistan",
    @ColumnInfo(name = "lang_country")
    var countryName: String? = "Pakistan",
    @ColumnInfo(name = "country_code")
    var countryCode: String? = "PK",
    @ColumnInfo(name = "lang_bcp47_code")
    var bcp47: String? = "en-US",
    @ColumnInfo(name = "lan_iso3_code")
    var iso3: String? = "en"
)
