package com.translate.translator.voice.translation.dictionary.all.language


import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.translate.translator.voice.translation.dictionary.all.language.billing.GBilling
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.view.View


@AndroidEntryPoint
class TranslatorMainActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    val viewModel: TranslatorMainViewModel by viewModels()
    var showExit = false

    lateinit var navView: NavigationView


    // It's called when activity is already exist
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    Log.i(TAG, "onNewIntent: "+ intent?.getStringExtra("SourceText"))
    val sourcetext = intent?.getStringExtra("SourceText")
    val transtext = intent?.getStringExtra("TargetText")
    showExit = intent?.getBooleanExtra("showDialog", false)!!

    if(!sourcetext.isNullOrEmpty()){
        Log.i(TAG, "onCreate: intent text" + sourcetext)
        viewModel.setHomeText(sourcetext)
        viewModel.setTranslatedHomeText(transtext)
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.
        activity_translator_main)


        val sourcetext = intent?.getStringExtra("SourceText")
        val transtext = intent?.getStringExtra("TargetText")
        showExit = intent?.getBooleanExtra("showDialog", false)!!

        if(!sourcetext.isNullOrEmpty()){
            Log.i(TAG, "onCreate: intent text" + sourcetext)
            viewModel.setHomeText(sourcetext)
            viewModel.setTranslatedHomeText(transtext)
        }




        // create tool bar, drawer layout and navigation view by using id
        val toolbar: Toolbar = findViewById(R.id.topAppBar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        // find navigation controller by using navigation view container id
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // set the toolbar as an action bar support
        setSupportActionBar(toolbar)
        val navController = navHostFragment.navController
        // create an appBarConfiguration with navigation graph and drawer layout
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
       // NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        // Hook up the navigation controller up to navigation view.
        navView.setupWithNavController(navController)
        navView.itemIconTintList = null;


        navController.addOnDestinationChangedListener { nc, destination, _ ->

            if (destination.id == nc.graph.startDestinationId) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                toolbar.setNavigationIcon(R.drawable.ic_drawer_icon)

                if(InternetUtils.get(this).value == true){
                    if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(INAPP_Feature) == 1 || UserPreferencesRepository.getInstance(this).getPurchaseStatus(
                            SUBS_Feature) == 1){
                        hideItem()
                    }else{
                        showItem()
                       // GBilling.instance.initBilling(this, "android.test.purchased", false, false, true)
                    }

                }else{
                    Toast.makeText(this, "Please connect with internet Connection", Toast.LENGTH_SHORT).show()
                }

            }

            else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                toolbar.setNavigationIcon(R.drawable.ic_up_back_button_icon)
            }

            if(destination.id == R.id.translation_Fragment){
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.full_screen_bg_color, null)))
            }else{
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent, null)))
            }

            if(destination.id  == R.id.share_fragment_id){
                nc.popBackStack()
                openShareSheet()
            }

            if(destination.id == R.id.feedback_fragment_id){
                nc.popBackStack()
                composeEmail(arrayOf("anasaslamnyminisoft@gmail.com"), "Feedback")
            }

        }


        viewModel.recentItemsList.observe(this, Observer {
            if(viewModel.recentlist.isEmpty()){
                val countryFlags = CountrySymbols.Builder(application.applicationContext).build()
                val displayLanguage = resources.getStringArray(R.array.lang_array_display_name)
                val showCountry = resources.getStringArray(R.array.lang_array_show_country)
                val bcpCodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)


                viewModel.recentlist.clear()
                viewModel.recentItemsList.value!!.forEach {
                    Log.e(TAG, "item: " + it)
                    if(bcpCodeString.contains(it.bcp47)){
                        it.displayName = displayLanguage[bcpCodeString.indexOf(it.bcp47)]
                        it.showCountryName = showCountry[bcpCodeString.indexOf(it.bcp47)]
                    }

                    val item = LanguageItem(
                        it.displayName,
                        it.langNameEN,
                        it.langNameLocal,
                        it.showCountryName,
                        it.countryName,
                        countryFlags.getCountryFlagIcon(it.countryCode),
                        it.countryCode,
                        it.bcp47,
                        it.iso3
                    )


                    viewModel.recentlist.add(item)
                }

            }

        })




        viewModel.argumentSource.observe(this, Observer {
                Log.i(TAG, "Source: $it, " + viewModel.recentlist.size)
            if(viewModel.recentlist.isNotEmpty()){
                viewModel.clearRecentTable()
                viewModel.insertToRecentTable(viewModel.recentlist)
            }

        })


        viewModel.argumentTarget.observe(this, Observer {
                Log.i(TAG, "target: $it")
            if(viewModel.recentlist.isNotEmpty()){
                viewModel.clearRecentTable()
                viewModel.insertToRecentTable(viewModel.recentlist)
            }

        })

        val premium = navView.getHeaderView(0).findViewById<Button>(R.id.button2)
        premium.setOnClickListener {
            if(InternetUtils.get(this).value == true){
                if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(INAPP_Feature) == 0 && UserPreferencesRepository.getInstance(this).getPurchaseStatus(
                        SUBS_Feature) == 0){
                    val intent = Intent(this, TranslatorPremiumActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    GBilling.instance.initBilling(this, InApp_ID, false, false, true, false)
                    if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(INAPP_Feature) == 1 || UserPreferencesRepository.getInstance(this).getPurchaseStatus(
                            SUBS_Feature) == 1){
                        launchMarket()
                    }

                }

            }else{
                Toast.makeText(this, "Please connect with internet Connection", Toast.LENGTH_SHORT).show()
            }

        }

    }


    override fun onResume() {
        super.onResume()
        InternetUtils.get(this).observe(this, Observer {
            if(it){
                if(UserPreferencesRepository.getInstance(this).getPurchaseStatus(INAPP_Feature) == 1 || UserPreferencesRepository.getInstance(this).getPurchaseStatus(
                        SUBS_Feature) == 1){
                    hideItem()
                    navView.getHeaderView(0).findViewById<Button>(R.id.button2).text = "More Apps Ads"
                }else{
                    showItem()
                    navView.getHeaderView(0).findViewById<Button>(R.id.button2).text = getText(R.string.drawer_header_premium)
                    // GBilling.instance.initBilling(this, "android.test.purchased", false, false, true)
                }
            }else{
                hideItem()
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        /*
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)*/

        when (item.itemId) {
            R.id.multi_translate_activity_id -> {
               /* val itemBundle = Bundle()
                itemBundle.putLong("itemId", -1L)*/
                this.findNavController(R.id.nav_host_fragment).navigate(R.id.multi_translate_activity_id)
                return true
            }
            else -> return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    private fun openShareSheet() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=$packageName")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun launchMarket() {
        val uri: Uri = Uri.parse("https://play.google.com/store/apps/developer?id=NY+Minisoft+Inc")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.not_find_app), Toast.LENGTH_LONG).show()
        }
    }


    private fun composeEmail(addresses: Array<String?>?, subject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

        try {
            startActivity(intent)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Couldn't find any email app for sending feedback", Toast.LENGTH_SHORT).show()

        }

    }

    private fun hideItem() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView

        val nav_Menu: Menu = navigationView.getMenu()
        nav_Menu.findItem(R.id.remove_ad_id).isVisible = false
    }

    private fun showItem() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView

        val nav_Menu: Menu = navigationView.getMenu()
        nav_Menu.findItem(R.id.remove_ad_id).isVisible = true
    }


    override fun onSupportNavigateUp(): Boolean {
        val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    companion object {
        private const val TAG = "TranslatorMainActivity"
        private const val SOURCE_LANGUAGE = "sourceLanguageItem"
        private const val TARGET_LANGUAGE = "targetLanguageItem"
        const val INAPP_Feature = "InAppItem"
        const val SUBS_Feature = "SubsItem"

        const val InApp_ID = "com.translate.translator.voice.translation.dictionary.all.language"
    }

/*    override fun onBackPressed() {
        if(showExit){
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.add(LogoutFragment.newInstance(), "Logout")
            transaction.addToBackStack(null)
            transaction.commit()
        }else{
            super.onBackPressed()
        }
    }*/


}