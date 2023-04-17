package com.translate.translator.voice.translation.dictionary.all.language

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.translate.translator.voice.translation.dictionary.all.language.util.LanguageManager
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp




@HiltAndroidApp
class EliteTranslatorApplication: Application(){


    override fun onCreate() {
/*        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectAll() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }*/
        if(LanguageManager.getLocale(resources).language != UserPreferencesRepository.getInstance(this).getAppLanguage(LanguageManager.LANGUAGE_KEY)!!){
            Log.d(TAG, "onCreate: "+ LanguageManager.getLocale(resources).language)
            LanguageManager.setNewLocale(this, LanguageManager.getLocale(resources).language)
        }else{
            LanguageManager.setNewLocale(this, UserPreferencesRepository.getInstance(this).getAppLanguage(LanguageManager.LANGUAGE_KEY)!!)
        }

        super.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Log.d(Companion.TAG, "onConfigurationChanged: "+ newConfig.locales[0].language)
            LanguageManager.setNewLocale(this, newConfig.locales[0].language)
        }else{
            newConfig.locale
            Log.d(Companion.TAG, "onConfigurationChanged: "+ newConfig.locale)
            LanguageManager.setNewLocale(this, newConfig.locale.language)
        }
        super.onConfigurationChanged(newConfig)
    }


    companion object {
        private const val TAG = "EliteTranslatorApplicat"
    }
}