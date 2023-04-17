package com.translate.translator.voice.translation.dictionary.all.language.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import java.util.*

object LanguageManager {

    const val LANGUAGE_KEY = "AppLanguageKey"
    @JvmStatic
    fun setLocale(mContext: Context): Context {
        return updateResources(mContext,  UserPreferencesRepository.getInstance(mContext).getAppLanguage(LANGUAGE_KEY))
    }

    @JvmStatic
    fun setNewLocale(mContext: Context, mLocaleKey: String): Context {
        UserPreferencesRepository.getInstance(mContext).setAppLanguage(LANGUAGE_KEY, mLocaleKey)
        return updateResources(mContext, mLocaleKey)
    }

/*    fun getLanguagePref(mContext: Context?): String? {
        val mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return mPreferences.getString(LANGUAGE_KEY, "")
    }

    private fun setLanguagePref(mContext: Context, localeKey: String) {
        val mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        mPreferences.edit().putString(LANGUAGE_KEY, localeKey).commit()
    }*/

    private fun updateResources(context: Context, language: String?): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        Log.e(TAG, "updateResources: "+ config.densityDpi)
        when(config.densityDpi){
            in 0..120 -> config.fontScale = 0.82f
            in 120..160 -> config.fontScale = 0.82f
            in 160..240 -> config.fontScale = 0.88f
            in 240..320 -> config.fontScale = 0.88f
            in 320..480 -> config.fontScale = 0.88f
            in 480..640 -> config.fontScale = 0.88f
            else ->{config.fontScale = 1.0f}
        }

        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= 24) config.locales[0] else config.locale
    }


    private const val TAG = "LanguageManager"

}