package com.translate.translator.voice.translation.dictionary.all.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import com.translate.translator.voice.translation.dictionary.all.language.adapters.*
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.ActivityAppLanguageBinding
import com.translate.translator.voice.translation.dictionary.all.language.services.ScreenCaptureService
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.util.LanguageManager
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppLanguageActivity.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AppLanguageActivity : BaseActivity(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: TranslatorMainViewModel by viewModels()
    private lateinit var binding: ActivityAppLanguageBinding
    private lateinit var langListAdapter: CountryListAdapter<ViewModel>
    private val codeList = mutableListOf<LanguageItem>()
    private lateinit var  toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_language)
        binding.lifecycleOwner = this
        toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        UserPreferencesRepository.getInstance(this).getAppLanguageObject(LANG_OBJECT)?.let{
            viewModel.setSourceItemValue(it)
        }


        langListAdapter = CountryListAdapter(SleepNightListener { language ->
            Log.i(TAG, "Select listener: " + language)

            UserPreferencesRepository.getInstance(this).setAppLanguageObject(LANG_OBJECT,language)

            Log.d(TAG, "onCreate: " + language.iso3)
            LanguageManager.setNewLocale(baseContext, language.iso3.toString())
            //stop the service if it is running within our app visibility session
            this.stopService(ScreenCaptureService.getStopIntent(baseContext))

            val intentToHome: Intent = Intent(this, TranslatorMainActivity::class.java)
            startActivity(intentToHome)
            finishAffinity()

        }, AutoNightListener { }, viewModel, this)

        binding.fragmentAppLangRecyclerViewId.adapter = langListAdapter

        // get flags class instance
        val countryFlags = CountrySymbols.Builder(this).build()

        val displayLanguage = resources.getStringArray(R.array.lang_array_display_name)
        val showCountry = resources.getStringArray(R.array.lang_array_show_country)

        val languageEnString = resources.getStringArray(R.array.lang_array_name_en)
        val languageLocalString = resources.getStringArray(R.array.lang_array_name_local)
        val countryNameString = resources.getStringArray(R.array.lang_array_county_variant)
        val countryCodeString = resources.getStringArray(R.array.lang_array_country_code)
        val bcp47CodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)
        val iso3CodeString = resources.getStringArray(R.array.lang_array_iso_3_code)


        val length = countryCodeString.size
        if (codeList.isNullOrEmpty()) {
            for (index in 0 until length) {
                val item = LanguageItem(
                    displayLanguage[index],
                    languageEnString[index],
                    languageLocalString[index],
                    showCountry[index],
                    countryNameString[index],
                    countryFlags.getCountryFlagIcon(countryCodeString[index]),
                    countryCodeString[index],
                    bcp47CodeString[index],
                    iso3CodeString[index]
                )
                codeList.add(item)
            }
        }

        Log.i(TAG, "onViewCreated: " + codeList.size)

        langListAdapter.data.clear()
        langListAdapter.data.addAll(addSubmitList(null, codeList))
        langListAdapter.filterItem.addAll(langListAdapter.data)





      /*  val languageEnString = resources.getStringArray(R.array.lang_array_display_name)
        val languageCodeString = resources.getStringArray(R.array.lang_array_iso_3_code)
        var count = 0

        languageCodeString.forEach {
            if(!codeList.contains(it)){
                if(it == "my" || it == "km" || it == "lo" || it == "pt" || it == "si"){

                }else{
                    codeList.add(it)
                }

            }
            else{
                if(it == "zh" && count == 0){
                    codeList.add(it)
                    count++
                }
            }
        }
        Log.d(TAG, "onCreate: size a"+ codeList.size)

        adapter = AppLanguageAdapter(ItemListener { item, code, pos ->
            Log.d(TAG, "onCreate: " + code)
            LanguageManager.setNewLocale(baseContext, code)
            //stop the service if it is running within our app visibility session
            this.stopService(ScreenCaptureService.getStopIntent(baseContext))

            val intentToHome: Intent = Intent(this, TranslatorMainActivity::class.java)
            startActivity(intentToHome)
            finishAffinity()

        }, viewModel)
        binding.fragmentAppLangRecyclerViewId.adapter = adapter
        adapter.data.clear()
        getAvailableLang(languageEnString)
        adapter.filterItem.addAll(adapter.data)
        Log.d(TAG, "onCreate: size b"+ adapter.data.size)
        adapter.notifyDataSetChanged()*/

        toolbar.setNavigationOnClickListener {
            finish()

        }

/*        binding.searchWidgetLayoutId.searchButtonId.setOnClickListener {
            if (it.isClickable) {
                hideKeyboard(it)
                binding.searchWidgetLayoutId.searchButtonId.setImageResource(R.drawable.ic_search_black_icon)
                binding.searchWidgetLayoutId.searchFieldId.text.clear()
                // binding.emptyStateLayoutId.visibility = View.GONE
                //binding.searchWidgetLayoutId.searchFieldId.isCursorVisible = false
                //binding.searchWidgetLayoutId.searchButtonId.isClickable = false
                // adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                //adapter.notifyDataSetChanged()
            }
        }

        binding.searchWidgetLayoutId.searchFieldId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //  TODO("Not yet implemented")
                // Log.i(TAG, "beforeTextChanged: ")

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //  TODO("Not yet implemented")
                // Log.i(TAG, "onTextChanged: ")
            }


            override fun afterTextChanged(p0: Editable?) {
                //  TODO("Not yet implemented")
                // Log.i(TAG, "afterTextChanged: ")
                val text = binding.searchWidgetLayoutId.searchFieldId.text.toString().trim()
                if (!text.isNullOrEmpty()) {
                    adapter.filter.filter(text)
                    if (adapter.data.count() == 0) {
                        Log.i(TAG, "count " + adapter.data.count())
                        // binding.emptyStateLayoutId.visibility = View.VISIBLE
                        //binding.fragmentLangRecyclerViewId.visibility = View.GONE
                    } else {
                        Log.i(TAG, "count " + adapter.data.count())
                        //binding.emptyStateLayoutId.visibility = View.GONE
                        //binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                    }
                } else {
                    // binding.emptyStateLayoutId.visibility = View.GONE
                    //binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                    adapter.data.clear()
                    getAvailableLang(languageEnString)
                    adapter.notifyDataSetChanged()
                }


            }

        })*/


    }

    private fun addSubmitList(
        recent: List<LanguageItem>?,
        list: List<LanguageItem>?
    ): List<DataItem> {
        langListAdapter.positionSize = recent?.size ?: 0
        return listOf(DataItem.LanguageHeader) + list!!.map { DataItem.SleepNightItem(it) }
    }

/*    private fun getAvailableLang(languageEnString: Array<out String>) {
        languageEnString.forEach {

            if (!adapter.data.contains(AppLangItem(it,  resources.getStringArray(R.array.lang_array_iso_3_code)[languageEnString.indexOf(it)]))) {
                if (it == "Burmese" || it == "Khmer" || it == "Lao" || it == "Portuguese" || it == "Sinhala") {

                } else {
                    adapter.data.add(AppLangItem(it,  resources.getStringArray(R.array.lang_array_iso_3_code)[languageEnString.indexOf(it)]))
                }

            }
        }
    }*/

    private fun hideKeyboard(view: View) {
        val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        imm.hideSoftInputFromWindow(this!!.currentFocus?.windowToken, 0)
    }

    companion object {
        private const val TAG = "AppLanguageActivity"
        const val LANG_OBJECT= "AppLangObject"
    }


    override fun onPause() {
        super.onPause()
        this.let { hideKeyboard(binding.root) }
    }
}