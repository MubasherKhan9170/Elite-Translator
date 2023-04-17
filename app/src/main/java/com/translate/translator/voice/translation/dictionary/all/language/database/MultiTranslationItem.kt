package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "multi_translation_history_table")
data class MultiTranslationItem(@PrimaryKey(autoGenerate = true)
                                var id: Long = 0L,
                                @ColumnInfo(name = "source_language")
                                var srcLanguage: String = "English",
                                @ColumnInfo(name = "source_text")
                                var srcText: String = "Hello, How are you?",
                                @ColumnInfo(name = "src_country")
                                var srcCountry: String = "United States",
                                @ColumnInfo(name = "src_iso3_code")
                                var srcIso3: String = "en",
                                @ColumnInfo(name = "src_bcp47_code")
                                var srcBcp47: String = "en-US",
                                @ColumnInfo(name = "target_languages")
                                var tarLanguage: String = "Urdu",
                                @ColumnInfo(name = "translate_texts")
                                var translationText: String = "ہیلو آپ کیسے ہیں؟",
                                @ColumnInfo(name = "favourite_status")
                                var favStatus: Boolean = false
)
