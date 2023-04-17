package com.translate.translator.voice.translation.dictionary.all.language

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.translate.translator.voice.translation.dictionary.all.language.billing.GBilling
import com.translate.translator.voice.translation.dictionary.all.language.billing.GoogleBilling
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils


class TranslatorPremiumActivity : BaseActivity() {

    private lateinit var closeView: ImageView
    private lateinit var privacyLabel: TextView
    private lateinit var termsLabel: TextView

    private lateinit var proInAppButton: CardView

    private lateinit var billing: GoogleBilling


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translator_premium)

        closeView = findViewById(R.id.premium_toolbar_close_btn_id)
        privacyLabel = findViewById(R.id.premium_privacy_label_id)
        termsLabel = findViewById(R.id.premium_terms_label_id)

        proInAppButton = findViewById(R.id.premium_card_id)

        billing = GBilling.instance


        Handler(Looper.getMainLooper()).postDelayed({
            closeView.visibility = View.VISIBLE
        }, 1000)



        closeView.setOnClickListener(View.OnClickListener {
            val intentToHome: Intent = Intent(this, TranslatorMainActivity::class.java)
            startActivity(intentToHome)
            finish()
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

        proInAppButton.setOnClickListener {
            if (InternetUtils.get(this).value == true) {
                billing.initBilling(this, InApp_ID, false, false, true, false)
            } else {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onBackPressed() {
        if (closeView.isVisible) {
            // super.onBackPressed()
            val intent = Intent(this, TranslatorMainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    companion object {
        const val InApp_ID = "com.translate.translator.voice.translation.dictionary.all.language"
    }

}