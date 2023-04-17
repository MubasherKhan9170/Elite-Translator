package com.translate.translator.voice.translation.dictionary.all.language.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.SharedItem
import java.lang.ClassCastException
import java.lang.IllegalArgumentException


class UserPreferencesRepository {

    companion object {
        private const val TAG = "UserPreferencesReposito"
        private val PREFS_NAME = "kotlincodes"
        private var INSTANCE: UserPreferencesRepository? = null
        private lateinit var sharedPref: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor

        fun getInstance(context: Context): UserPreferencesRepository {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    editor = sharedPref.edit()
                    instance = UserPreferencesRepository()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }


    fun save(KEY_NAME: String, status: Boolean) {

        editor.putBoolean(KEY_NAME, status)

        editor.apply()
    }


    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(KEY_NAME, defaultValue)

    }


    fun saveLanguage(KEY_NAME: String, language: LanguageItem) {
        val sharedItem = SharedItem(
            displayName = language.displayName,
            langNameEN = language.langNameEN,
            langNameLocal = language.langNameLocal,
            showCountryName = language.showCountryName,
            countryName = language.countryName,
            countryCode = language.countryCode,
            bcp47 = language.bcp47,
            iso3 = language.iso3
        )


        val gson = Gson()
        val json = gson.toJson(sharedItem)
        Log.i(TAG, "saveLanguage: $json")
        editor.putString(KEY_NAME, json)
        editor.apply()

    }


    fun getLanguage(KEY_NAME: String): LanguageItem? {
        var json: String? = null
        try {
            json = sharedPref.getString(KEY_NAME, null)
        } catch (e: ClassCastException) {
            Log.e(TAG, "getLanguage: ", e)
        }

        return if (json == null) {
            null
        } else {
            val gson = Gson()
            val obj = gson.fromJson(json, SharedItem::class.java)
            LanguageItem(
                obj.displayName,
                obj.langNameEN,
                obj.langNameLocal,
                obj.showCountryName,
                obj.countryName,
                null,
                obj.countryCode,
                obj.bcp47,
                obj.iso3
            )
        }

    }

    fun getAppLanguage(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, "en")
    }

    fun setAppLanguage(KEY_NAME: String, b: String?) {
        sharedPref.edit().putString(KEY_NAME, b).apply()
    }

    fun getPurchaseStatus(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun setPurchaseStatus(KEY_NAME: String, b: Int) {
        sharedPref.edit().putInt(KEY_NAME, b).apply()
    }



    fun getAppLanguageObject(KEY_NAME: String): LanguageItem? {
        var json: String? = null
        try {
            json = sharedPref.getString(KEY_NAME, null)
        } catch (e: ClassCastException) {
            Log.e(TAG, "getLanguage: ", e)
        }

        return if (json == null) {
            null
        } else {
            val gson = Gson()
            val obj = gson.fromJson(json, SharedItem::class.java)
            LanguageItem(
                obj.displayName,
                obj.langNameEN,
                obj.langNameLocal,
                obj.showCountryName,
                obj.countryName,
                null,
                obj.countryCode,
                obj.bcp47,
                obj.iso3
            )
        }
    }

    fun setAppLanguageObject(KEY_NAME: String, language: LanguageItem) {
        val sharedItem = SharedItem(
            displayName = language.displayName,
            langNameEN = language.langNameEN,
            langNameLocal = language.langNameLocal,
            showCountryName = language.showCountryName,
            countryName = language.countryName,
            countryCode = language.countryCode,
            bcp47 = language.bcp47,
            iso3 = language.iso3
        )
        val gson = Gson()
        val json = gson.toJson(sharedItem)
        Log.i(TAG, "saveLanguage: $json")

        editor.putString(KEY_NAME, json)

        editor.apply()

    }

    fun getLiveAppLanguage(KEY_NAME: String): LiveData<String?> {
        val result = MutableLiveData<String>()
        result.value = sharedPref.getString(KEY_NAME, "en")
        return result
    }


    fun clearSharedPreference() {
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {
        editor.remove(KEY_NAME)
        editor.apply()
    }
}