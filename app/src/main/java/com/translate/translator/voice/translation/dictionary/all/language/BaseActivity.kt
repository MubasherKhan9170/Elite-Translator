package com.translate.translator.voice.translation.dictionary.all.language

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.translate.translator.voice.translation.dictionary.all.language.util.LanguageManager

/*open: In Kotlin all classes, functions, and variables are by defaults final, and by inheritance
property, we cannot inherit the property of final classes, final functions, and data members.
So we use the open keyword before the class or function or variable to make inheritable that.*/

open class BaseActivity : AppCompatActivity() {


    override fun attachBaseContext(base: Context) {
        Log.d(Companion.TAG, "attachBaseContext: called at base class")
        super.attachBaseContext(LanguageManager.setLocale(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       // initFirebaseRemoteConfig()

    }

/*    fun initFirebaseRemoteConfig() {
        *//*remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1)
            .build()*//*

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1000
        }
        BaseActivity.Remote.remoteConfig.setConfigSettingsAsync(configSettings)

        BaseActivity.Remote.remoteConfig.setDefaultsAsync(R.xml.remote_config)
*//*        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    remoteConfig!!.activate()
                    Toast.makeText(this, remoteConfig!!.getString("weekly"),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Fetch failed",
                        Toast.LENGTH_SHORT).show()
                }
                // displayWelcomeMessage()
            }*//*
    }*/



    companion object {
        private const val TAG = "BaseActivity"
    }

/*    object Remote{
        var remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    }*/
}