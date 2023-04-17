package com.translate.translator.voice.translation.dictionary.all.language

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.translate.translator.voice.translation.dictionary.all.language.billing.DashBoardActivityInterface
import com.translate.translator.voice.translation.dictionary.all.language.billing.GBilling
import com.translate.translator.voice.translation.dictionary.all.language.billing.GoogleBilling
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils

class TranslatorSubscriptionActivity : BaseActivity(), DashBoardActivityInterface {

    private lateinit var closeView: ImageView
    private lateinit var privacyLabel: TextView
    private lateinit var termsLabel: TextView
    private lateinit var basicButton: CardView
    private lateinit var advanceButton: CardView
    private lateinit var premiumButton: CardView
    private lateinit var proButton: CardView

    private lateinit var basicIcon: ImageView
    private lateinit var advanceIcon: ImageView
    private lateinit var premiumIcon: ImageView

    private lateinit var basicText: TextView
    private lateinit var advanceText: TextView
    private lateinit var premiumText: TextView

    private lateinit var proSubText: TextView

    private lateinit var billing: GoogleBilling

    private var _mBasic = false
    private var _mAdvance = true
    private var _mPremium = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translator_subsription)

        closeView = findViewById(R.id.premium_toolbar_close_btn_id)
        privacyLabel = findViewById(R.id.premium_privacy_label_id)
        termsLabel = findViewById(R.id.premium_terms_label_id)

        basicButton = findViewById(R.id.weekly_card_id)
        advanceButton = findViewById(R.id.monthly_card_id)
        premiumButton = findViewById(R.id.n_months_card_id)
        proButton = findViewById(R.id.premium_card_id)

        basicIcon = findViewById(R.id.weekly_card_icon_id)
        advanceIcon = findViewById(R.id.monthly_card_icon_id)
        premiumIcon = findViewById(R.id.n_months_card_icon_id)

        basicText = findViewById(R.id.price_label_id)
        advanceText = findViewById(R.id.monthly_price_label_id)
        premiumText = findViewById(R.id.n_months_price_label_id)

        proSubText = findViewById(R.id.premium_card_text_id)

        billing = GBilling.instance








        Handler(Looper.getMainLooper()).postDelayed({
            closeView.visibility = View.VISIBLE
        }, 1000)



        closeView.setOnClickListener(View.OnClickListener {
            billing.purchasesUpdatedListener = null
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


        basicButton.setOnClickListener {
            basicButton.setCardBackgroundColor(resources.getColor(R.color.primaryLightColor, null))
            basicIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.offColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            advanceButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            advanceIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            premiumButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            premiumIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            _mBasic = true
            _mAdvance = false
            _mPremium = false


            if (InternetUtils.get(this).value == true) {
                billing.subsSkuList?.let {
                    proSubText.text = "Weekly Plan ${billing.subsSkuList!![2].currency} ${billing.subsSkuList!![2].price}"
                }

            } else {
                proSubText.text = "Weekly Plan Rs 200"
            }

        }


        advanceButton.setOnClickListener {
            basicButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            basicIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            advanceButton.setCardBackgroundColor(
                resources.getColor(
                    R.color.primaryLightColor,
                    null
                )
            )
            advanceIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.offColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            premiumButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            premiumIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            _mBasic = false
            _mAdvance = true
            _mPremium = false

            if (InternetUtils.get(this).value == true) {
                billing.subsSkuList?.let {
                    proSubText.text = "Monthly Plan ${billing.subsSkuList[0].currency} ${billing.subsSkuList[0].price}"
                }

            } else {
                proSubText.text = "Monthly Plan Rs 1200"
            }

        }

        premiumButton.setOnClickListener {
            basicButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            basicIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            advanceButton.setCardBackgroundColor(resources.getColor(R.color.offColor, null))
            advanceIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            premiumButton.setCardBackgroundColor(
                resources.getColor(
                    R.color.primaryLightColor,
                    null
                )
            )
            premiumIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.offColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            _mBasic = false
            _mAdvance = false
            _mPremium = true

            if (InternetUtils.get(this).value == true) {
                billing.subsSkuList?.let {
                    proSubText.text = "3 Months Plan ${billing.subsSkuList[1].currency} ${billing.subsSkuList[1].price}"
                }

            } else {
                proSubText.text = "3 Months Plan Rs 12000"
            }


        }



        proButton.setOnClickListener {
            if (InternetUtils.get(this).value == true) {
                if(_mBasic){
                    billing.initBilling(this, Weekly, true, false, false, false)
                }
                if(_mAdvance){
                    billing.initBilling(this, Monthly, true, false, false, false)
                }
                if(_mPremium){
                    billing.initBilling(this, Three_Months, true, false, false, false)
                }

            } else {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onResume() {
        super.onResume()

        InternetUtils.get(this).observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                Log.i(Companion.TAG, "Internet Connected")

                billing.subsSkuList?.let {

                    basicText.text = billing.subsSkuList[2].price
                    advanceText.text = billing.subsSkuList[0].price
                    premiumText.text = billing.subsSkuList[1].price

                    if (_mBasic) {
                        proSubText.text = "Weekly Plan ${billing.subsSkuList[2].currency} ${billing.subsSkuList[2].price}"

                    } else if (_mAdvance) {
                        proSubText.text = "Monthly Plan ${billing.subsSkuList[0].currency} ${billing.subsSkuList[0].price}"

                    } else{
                        proSubText.text = "3 Months Plan ${billing.subsSkuList[1].currency} ${billing.subsSkuList[1].price}"
                    }
                }

            } else {
                //Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()

                basicText.text = "Rs 200"
                advanceText.text = "Rs 1200"
                premiumText.text = "Rs 12000"

                if (_mBasic) {
                    proSubText.text = "Weekly Plan ${basicText.text}"

                } else if (_mAdvance) {
                    proSubText.text = "Monthly Plan ${advanceText.text}"

                } else{
                    proSubText.text = "3 Months Plan ${premiumText.text}"
                }
            }
        })
    }

    override fun onBackPressed() {
        if (closeView.isVisible) {
            // super.onBackPressed()
            val intent = Intent(this, TranslatorMainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    override fun updateUi() {
        // TODO("Not yet implemented")

    }

    override fun openImageEncoder() {
        val intent = Intent(this, TranslatorMainActivity::class.java)
        startActivity(intent)
        finish()
/*        runOnUiThread {

            *//* dissableButtons(mBinding?.imageEncoder!!)
             isImageEncoder = false
             mBinding?.pickerView?.visibility = View.GONE
             setItemVisibility(false)
             setIcons(mBinding?.imageEncoder!!, R.drawable.ic_selected_image_encoder, getString(R.string.image_encoder), R.drawable.ic_image_encoder_icon);*//*
        }*/
        // mBinding?.selectedScaningOption?.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake_btn))
    }


    companion object {
        private const val TAG = "TranslatorSubscriptionActivity"
        const val Weekly = "subsweekly"
        const val Monthly = "subsmonthly"
        const val Three_Months = "substhreemonth"

    }
}