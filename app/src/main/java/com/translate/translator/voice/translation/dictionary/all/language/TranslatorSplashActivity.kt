package com.translate.translator.voice.translation.dictionary.all.language

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.translate.translator.voice.translation.dictionary.all.language.billing.GBilling
import com.translate.translator.voice.translation.dictionary.all.language.billing.GoogleBilling
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository


@SuppressLint("CustomSplashScreen")
class TranslatorSplashActivity : BaseActivity() {

    private lateinit var billing: GoogleBilling

    private val idList = listOf<Int>(1,2,3)




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Handle the splash screen transition.
        /*val splashScreen = installSplashScreen()*/
        setContentView(R.layout.activity_translator_splash)
        billing = GBilling.instance
        billing.initBilling(this, Monthly, true, true, false, true)


        val translateButton = findViewById<Button>(R.id.splash_translate_button_id)
        val privacyLabel = findViewById<TextView>(R.id.splash_privacy_label_id)
        val termsLabel = findViewById<TextView>(R.id.splash_terms_label_id)





        translateButton.setOnClickListener(View.OnClickListener {
            if(InternetUtils.get(this).value == true){
                //for inApp

                if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(INAPP_Feature) == 1){
                    val intentToMain: Intent = Intent(this, TranslatorMainActivity::class.java)
                    startActivity(intentToMain)
                    finish()
                }else{
                    //for subs
                        if(billing.subsSkuList.isNullOrEmpty()){
                            val intentToMain: Intent = Intent(this, TranslatorMainActivity::class.java)
                            startActivity(intentToMain)
                            finish()
                        }else {

                            idList.forEach {
                                if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(SUBS_Feature) == 0){
                                if(it == 1){
                                    billing.initBilling(this, Weekly, false, true, false, false)
                                }
                                if(it == 2){
                                    billing.initBilling(this, Monthly, false, true, false, false)
                                }
                                if (it == 3){
                                    billing.initBilling(this, Three_Months, false, true, false, false)
                                }
                                }

                            }
                        }

                }

            }else{
                val intentToMain: Intent = Intent(this, TranslatorMainActivity::class.java)
                startActivity(intentToMain)
                finish()
            }
        })

        privacyLabel.setOnClickListener {
            val intentTOPrivacyPage: Intent = Intent(this, WebActivity::class.java)
            intentTOPrivacyPage.putExtra("key", "privacy")
            startActivity(intentTOPrivacyPage)
        }

        termsLabel.setOnClickListener {
            val intentTOTermsPage: Intent = Intent(this, WebActivity::class.java)
            intentTOTermsPage.putExtra("key", "term")
            startActivity(intentTOTermsPage)
        }

      //  initFirebaseRemoteConfig()
    }

    override fun onStart() {
        super.onStart()
        GBilling.instance.initBilling(this, InApp_ID, false, false, true, false)

    }

/*    fun initFirebaseRemoteConfig() {
        *//*remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1)
            .build()*//*

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1000
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config)
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

    override fun onResume() {
        super.onResume()
        InternetUtils.get(this).observe(this, Observer {

        })
    }



    companion object {
        private const val TAG = "TranslatorSplashActivity"
        const val INAPP_Feature = "InAppItem"
        const val SUBS_Feature = "SubsItem"

        const val Weekly = "subsweekly"
        const val Monthly = "subsmonthly"
        const val Three_Months = "substhreemonth"
        const val InApp_ID = "com.translate.translator.voice.translation.dictionary.all.language"

        /*const val Weekly = "android.test.purchased"
        const val Monthly = "android.test.purchased"
        const val Three_Months = "android.test.purchased"*/



    }

/*    object Remote{
        var remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    }*/
}