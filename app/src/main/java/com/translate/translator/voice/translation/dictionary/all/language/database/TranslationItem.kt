package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history_table")
data class TranslationItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "source_to_target")
    var srcToTarget: String = "English To Urdu",
    @ColumnInfo(name = "display_src_language")
    var displaySrcName: String = "English",
    @ColumnInfo(name = "source_language")
    var srcLanguage: String = "English",
    @ColumnInfo(name = "source_text")
    var srcText: String = "Hello, How are you?",
    @ColumnInfo(name = "show_src_country")
    var showSrcCountry: String = "United States",
    @ColumnInfo(name = "src_country")
    var srcCountry: String = "United States",
    @ColumnInfo(name = "src_iso3_code")
    var srcIso3: String = "en",
    @ColumnInfo(name = "src_bcp47_code")
    var srcBcp47: String = "en-US",
    @ColumnInfo(name = "display_tar_language")
    var displayTarName: String = "Urdu",
    @ColumnInfo(name = "target_language")
    var tarLanguage: String = "Urdu",
    @ColumnInfo(name = "translate_text")
    var translationText: String = "ہیلو آپ کیسے ہیں؟",
    @ColumnInfo(name = "show_tar_country")
    var showTarCountry: String = "Pakistan",
    @ColumnInfo(name = "tar_country")
    var tarCountry: String = "Pakistan",
    @ColumnInfo(name = "tar_iso3_code")
    var tarIso3: String = "ur",
    @ColumnInfo(name = "tar_bcp47_code")
    var tarBcp47: String = "ur-PK",
    @ColumnInfo(name = "favourite_status")
    var favStatus: Boolean = false)




