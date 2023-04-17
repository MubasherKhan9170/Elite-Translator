package com.translate.translator.voice.translation.dictionary.all.language.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*

import com.android.billingclient.api.BillingClient

import com.android.billingclient.api.BillingResult

import com.android.billingclient.api.Purchase
import com.translate.translator.voice.translation.dictionary.all.language.TranslatorMainActivity
import com.translate.translator.voice.translation.dictionary.all.language.TranslatorSubscriptionActivity
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository


class GoogleBilling {
    private var billingClient : BillingClient? = null
    var skuDetails : SkuDetails?=null
    var productID : String? = null
    var mContext : Context? = null
    var subsSkuList = mutableListOf<Subs>()


    var dashBoardActivityInterface : DashBoardActivityInterface? = null
    var purchasesUpdatedListener : PurchasesUpdatedListener? = null
    var ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            //if purchase is acknowledged
            // Grant entitlement to the user. and restart activity
            // SharedPref.instance.
            dashBoardActivityInterface?.openImageEncoder()
            //  recreate(mContext as Activity)
        }
    }

    init {

        purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            Log.v("GoogleBilling", "billingResult responseCode : ${billingResult.responseCode}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                    Toast.makeText(mContext, "Item Already Purchased", Toast.LENGTH_SHORT)
                        .show()
                }
                else{

                    if(purchases.size > 1){
                        for (purchase in purchases) {
                            //if item is purchased
                            if (Weekly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                || Monthly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                || Three_Months == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                            ) {

                                (mContext as Activity).runOnUiThread {
                                    Toast.makeText(mContext, "Subscription has been Purchased", Toast.LENGTH_SHORT)
                                        .show()
                                }

                                UserPreferencesRepository.getInstance(mContext as Activity).setPurchaseStatus(
                                    SUBS_Feature,
                                    1)

                                val intentToMain: Intent =
                                    Intent(mContext, TranslatorMainActivity::class.java)
                                mContext?.startActivity(intentToMain)
                                (mContext as Activity).finish()
                            }
                        }
                    }else{
                        if(purchases[0].skus[0]== InApp_ID){
                            (mContext as Activity).runOnUiThread {
                                Toast.makeText(mContext, "Features have been Purchased", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            UserPreferencesRepository.getInstance(mContext as Activity).setPurchaseStatus(
                                INAPP_Feature,
                                1)
                            (mContext as Activity).finish()
                        }
                    }

                    //dashBoardActivityInterface?.openImageEncoder()
                }

            }
            else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

                Toast.makeText(mContext, "Item Purchase Canceled", Toast.LENGTH_SHORT).show()
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                try {
                    Toast.makeText(mContext, "Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                }


                // Handle any other error codes.
            }
        }
    }


    fun initBilling(context : Context,product_id : String?,isSelectedSubscription : Boolean, splash: Boolean, inApp: Boolean, detail: Boolean){
        productID = product_id;
        mContext  = context;
        if(billingClient == null){
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener!!)
                .enablePendingPurchases()
                .build()


        }
        startConnection(context,isSelectedSubscription, splash, inApp, detail)

    }
    fun handleNonConcumablePurchase(purchase: Purchase) {
        Log.v("GoogleBilling", "handlePurchase : ${purchase}")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    val billingResponseCode = billingResult.responseCode
                    val billingDebugMessage = billingResult.debugMessage

                    Log.v("GoogleBilling", "response code: $billingResponseCode")
                    Log.v("GoogleBilling", "debugMessage : $billingDebugMessage")

                }
            }
        }
    }

    fun skuDetails(context: Context){
        skuDetails?.let {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(context as Activity, billingFlowParams)?.responseCode }?:noSKUMessage()
    }


    private  fun startConnection(context: Context,isSelectedSubscription : Boolean, splash: Boolean, inApp: Boolean, detail: Boolean) {
//        var productIds = ArrayList<String>()
//        productIds.add("encoderbronze")
//        productIds.add("encodersilver")
//        productIds.add("encodergold")
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if(isSelectedSubscription){
                    Log.v("GoogleBilling", "connect" + billingResult.responseCode)
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.v("GoogleBilling", "Setup Billing Done")
                        // The BillingClient is ready. You can query purchases here.
                        queryAvaliableProducts(context, inApp,detail)
                    }
                }
                else {
                    if(inApp){
                        checkUserInAppPurchaseItem(billingResult, context, splash, inApp)
                    }else{
                        checkUserSubsPurchaseItem(billingResult, context, splash, inApp)
                    }




                }
            }

            override fun onBillingServiceDisconnected() {
                Log.v("GoogleBilling", "Billing client Disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun checkUserSubsPurchaseItem(
        billingResult: BillingResult,
        context: Context,
        splash: Boolean,
        inApp: Boolean
    ) {
        billingClient!!.queryPurchasesAsync(BillingClient.SkuType.SUBS,
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    p0: BillingResult,
                    p1: MutableList<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && p1.isNotEmpty()) {


                        for (purchase in p1) {
                            //if item is purchased
                            if (Weekly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                || Monthly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                || Three_Months == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                            ) {


                                //if item is purchased and not acknowledged
                                if (!purchase.isAcknowledged) {
                                    val acknowledgePurchaseParams =
                                        AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.purchaseToken)
                                            .build()
                                    billingClient!!.acknowledgePurchase(
                                        acknowledgePurchaseParams,
                                        ackPurchase
                                    )
                                } else {
                                    // dashBoardActivityInterface?.openImageEncoder()
                                    UserPreferencesRepository.getInstance(context).setPurchaseStatus(
                                        SUBS_Feature,
                                        purchase.purchaseState)
                                    if(splash){
                                        val intentToMain: Intent =
                                            Intent(mContext, TranslatorMainActivity::class.java)
                                        mContext?.startActivity(intentToMain)
                                        (mContext as Activity).finish()
                                    }



                                    /*(context as Activity).runOnUiThread {
                                                Toast.makeText(context, "Item Purchased", Toast.LENGTH_SHORT).show()
                                            }*/


                                }

                            } else if (Weekly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING
                                || Monthly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING
                                || Three_Months == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING
                            ) {

                                (context as Activity).runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Purchase is Pending. Please complete Transaction",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            } else if (Weekly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
                                || Monthly == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
                                || Three_Months == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
                            ) {

                                (context as Activity).runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Purchase Status Unknown",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // dashBoardActivityInterface?.openImageEncoder()
                            }
                        }
                    } else {
                        if (splash) {
                            val intentToPremium: Intent = Intent(context, TranslatorSubscriptionActivity::class.java)
                            context.startActivity(intentToPremium)
                            (context as Activity).finish()
                        } else {
                            queryAvaliableProducts(context, inApp, detail = false)
                        }

                        /*(context as Activity).runOnUiThread {
                                 //   Alert.instance.imageEncoderInappAlert(context)
                                    // The BillingClient is ready. You can query purchases here.

                                }*/


                    }
                }

            })
    }


    private fun checkUserInAppPurchaseItem(
        billingResult: BillingResult,
        context: Context,
        splash: Boolean,
        inApp: Boolean
    ) {
        billingClient!!.queryPurchasesAsync(BillingClient.SkuType.INAPP,
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    p0: BillingResult,
                    p1: MutableList<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && p1.isNotEmpty()) {


                        for (purchase in p1) {
                            //if item is purchased
                            if (InApp_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {


                                //if item is purchased and not acknowledged
                                if (!purchase.isAcknowledged) {
                                    val acknowledgePurchaseParams =
                                        AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.purchaseToken)
                                            .build()
                                    billingClient!!.acknowledgePurchase(
                                        acknowledgePurchaseParams,
                                        ackPurchase
                                    )
                                } else {
                                    // dashBoardActivityInterface?.openImageEncoder()

                                    /*val intentToMain: Intent =
                                        Intent(mContext, TranslatorMainActivity::class.java)
                                    mContext?.startActivity(intentToMain)
                                    (mContext as Activity).finish()*/
                                            if(splash){
                                                UserPreferencesRepository.getInstance(context)
                                                    .setPurchaseStatus(
                                                        INAPP_Feature,
                                                        purchase.purchaseState
                                                    )
                                               /* (context as Activity).runOnUiThread {
                                                    Toast.makeText(
                                                        context,
                                                        UserPreferencesRepository.getInstance(context)
                                                            .getPurchaseStatus(
                                                                INAPP_Feature
                                                            ).toString(),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }*/

                                            }else{
                                                UserPreferencesRepository.getInstance(context)
                                                    .setPurchaseStatus(
                                                        INAPP_Feature,
                                                        purchase.purchaseState
                                                    )
                                                (context as Activity).runOnUiThread {
                                                    Toast.makeText(context, "Item Purchased", Toast.LENGTH_SHORT).show()
                                                }
                                            }




                                }

                            } else if (InApp_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING) {

                                (context as Activity).runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Purchase is Pending. Please complete Transaction",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            } else if (InApp_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {

                                (context as Activity).runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "Purchase Status Unknown",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // dashBoardActivityInterface?.openImageEncoder()
                            }
                        }
                    } else {
                        if (splash) {
                            UserPreferencesRepository.getInstance(context)
                                .setPurchaseStatus(
                                    INAPP_Feature,
                                    0
                                )
                        } else {
                            queryAvaliableProducts(context, inApp, detail = false)
                        }

                        /*(context as Activity).runOnUiThread {
                                 //   Alert.instance.imageEncoderInappAlert(context)
                                    // The BillingClient is ready. You can query purchases here.

                                }*/


                    }
                }

            })
    }

    private  fun queryAvaliableProducts(context: Context, inApp: Boolean, detail: Boolean) {
        val skuList = ArrayList<String>()
        if(detail){
            skuList.add(Weekly)
            skuList.add(Monthly)
            skuList.add(Three_Months)
        }else{
            Log.i(TAG, "queryAvaliableProducts: " + productID)
            skuList.add(productID!!)
        }

        val params = SkuDetailsParams.newBuilder()
        if(inApp){
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        }else{
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS) // change to subs
        }

        val skuDetailsResult = billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            // Process the result.
            Log.v(TAG, "skuDetailsList : ${skuDetailsList?.size}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {

                    Log.v(TAG, "skuDetailsList : ${skuDetailsList}")
                    //This list should contain the products added above
                    updateSkudetails(skuDetails,context, detail)

                }
                /*if(detail){
                    val intentToSubs: Intent =
                        Intent(mContext, TranslatorSubscriptionActivity::class.java)
                    mContext?.startActivity(intentToSubs)
                    (mContext as Activity).finish()
                }*/
            }
            else{
                (context as Activity).runOnUiThread {
                   // Toast.makeText(mContext,"Upgrade to premium",Toast.LENGTH_LONG).show()
                }
                /*if(detail){
                    val intentToMain: Intent =
                        Intent(mContext, TranslatorMainActivity::class.java)
                    mContext?.startActivity(intentToMain)
                    (mContext as Activity).finish()
                }*/



//                    dashBoardActivityInterface?.openImageEncoder()
            }
        }



    }


    private fun noSKUMessage() {

    }
    private fun updateSkudetails(skuDetails: SkuDetails?,context: Context, detail: Boolean) {
        if(detail){
            skuDetails?.let {
                this.skuDetails = it
                Log.i(TAG, "updateSkudetails: "+ it.sku +" "+ it.price)
                this.subsSkuList.add(Subs(it.type, it.sku, it.price))
            }
        }else{
            skuDetails?.let {
                this.skuDetails = it
                this.subsSkuList.add(Subs(it.type, it.sku, it.price))
            }
            skuDetails(context)
        }

    }

    companion object {
        private const val TAG = "GoogleBilling"
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


}
object GBilling {
    val instance = GoogleBilling()
}