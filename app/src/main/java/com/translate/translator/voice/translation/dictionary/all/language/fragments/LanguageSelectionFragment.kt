package com.translate.translator.voice.translation.dictionary.all.language.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.adapters.AutoNightListener
import com.translate.translator.voice.translation.dictionary.all.language.adapters.CountryListAdapter
import com.translate.translator.voice.translation.dictionary.all.language.adapters.DataItem
import com.translate.translator.voice.translation.dictionary.all.language.adapters.SleepNightListener
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentLanguageSelectionBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
@AndroidEntryPoint
class LanguageSelectionFragment : Fragment() {

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: FragmentLanguageSelectionBinding
    private val args: LanguageSelectionFragmentArgs by navArgs()
    private lateinit var adapter: CountryListAdapter<ViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_language_selection,
            container,
            false
        )
        // return inflater.inflate(R.layout.fragment_language_selection, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")
        viewModel.dataType = args.type
        viewModel.dataList = args.list
        adapter = CountryListAdapter(SleepNightListener {
            if (viewModel.dataType == "source") {
                viewModel.autoItemState = false
            }

            viewModel.onLanguageItemClicked(it)


        }, AutoNightListener {
            viewModel.autoItemState = true

            viewModel.onLanguageItemClicked(
                LanguageItem(
                    getString(R.string.auto_detected),
                    "Auto Detected",
                    "Auto",
                    "Auto",
                    "Auto",
                    null,
                    "US",
                    "en-US",
                    "auto"
                )
            )

        }, viewModel, requireContext())
        binding.fragmentLangRecyclerViewId.adapter = adapter

        // get flags class instance
        val countryFlags = CountrySymbols.Builder(requireContext()).build()

        val list = mutableListOf<LanguageItem>()

        val displayLanguage = resources.getStringArray(R.array.lang_array_display_name)
        val showCountry = resources.getStringArray(R.array.lang_array_show_country)

        val languageEnString = resources.getStringArray(R.array.lang_array_name_en)
        val languageLocalString = resources.getStringArray(R.array.lang_array_name_local)
        val countryNameString = resources.getStringArray(R.array.lang_array_county_variant)
        val countryCodeString = resources.getStringArray(R.array.lang_array_country_code)
        val bcp47CodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)
        val iso3CodeString = resources.getStringArray(R.array.lang_array_iso_3_code)
        val cameraLanguageEnString = resources.getStringArray(R.array.camera_lang_array_name_en)

        val length = countryCodeString.size
        val arraySize = cameraLanguageEnString.size
        var count = 0


        for (index in 0 until length) {
            val item: LanguageItem

            if (viewModel.dataList == "camera" && viewModel.dataType == "source") {
                if (count in 0 until arraySize) {
                    if (cameraLanguageEnString[count] == languageEnString[index]) {
                        item = LanguageItem(
                            displayLanguage[index],
                            cameraLanguageEnString[count],
                            languageLocalString[index],
                            showCountry[index],
                            countryNameString[index],
                            countryFlags.getCountryFlagIcon(countryCodeString[index]),
                            countryCodeString[index],
                            bcp47CodeString[index],
                            iso3CodeString[index]
                        )
                        list.add(item)
                        count++
                    }
                }


            }else if(viewModel.dataList == "screen" && viewModel.dataType == "source"){
                if (count in 0 until arraySize) {
                    if (cameraLanguageEnString[count] == languageEnString[index]) {
                        item = LanguageItem(
                            displayLanguage[index],
                            cameraLanguageEnString[count],
                            languageLocalString[index],
                            showCountry[index],
                            countryNameString[index],
                            countryFlags.getCountryFlagIcon(countryCodeString[index]),
                            countryCodeString[index],
                            bcp47CodeString[index],
                            iso3CodeString[index]
                        )
                        list.add(item)
                        count++
                    }
                }

            } else {
                item = LanguageItem(
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
                list.add(item)
            }
        }

        adapter.data.clear()

        adapter.data.addAll(addSubmitList(viewModel.recentlist, list))
        adapter.filterItem.addAll(adapter.data)

        if (!viewModel.recentlist.isNullOrEmpty()) {
            if(viewModel.dataList == "camera"){
                if (viewModel.dataType == "source") {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentSource.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getSourceLangBcp47Code()!!
                    )
                    if (flag) {
                        val supportList = removeUnsupportedLanguages(viewModel.recentlist, list)
                        supportList.forEach {
                            Log.i(TAG, "supported list: "+ it)
                        }
                        adapter.updateList(viewModel.dataType, supportList, list)
                    }
                } else {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentTarget.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getTargetLangBcp47Code()!!
                    )
                    if (flag) {
                        adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                    }
                }

            }else if(viewModel.dataList == "screen"){

                if (viewModel.dataType == "source") {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentSource.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getSourceLangBcp47Code()!!
                    )
                    if (flag) {
                        val supportList = removeUnsupportedLanguages(viewModel.recentlist, list)
                        supportList.forEach {
                            Log.i(TAG, "supported list: "+ it)
                        }
                        adapter.updateList(viewModel.dataType, supportList, list)
                    }
                } else {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentTarget.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getTargetLangBcp47Code()!!
                    )
                    if (flag) {
                        adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                    }
                }


            } else{
                if (viewModel.dataType == "source") {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentSource.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getSourceLangBcp47Code()!!
                    )
                    if (flag) {
                        adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                    }
                } else {
                    val flag = viewModel.itemCheckUp(
                        viewModel.argumentTarget.value.toString(),
                        viewModel.getSelectedLangCountry()!!, viewModel.getTargetLangBcp47Code()!!
                    )
                    if (flag) {
                        adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                    }

                }
            }

        }


        /* if(viewModel.dataType == "source" && !viewModel.recentlist.isNullOrEmpty()){
             val flag = viewModel.itemCheckUp(viewModel.argumentSource.value.toString(),
                 viewModel.getSelectedLangCountry()!!
             )
              //Log.i(TAG, "source flag: " + flag)
             if(flag){
                 val singleItem = viewModel.recentlist.distinctBy { it.langNameEN to it.countryName }.toList()
                 adapter.updateList(viewModel.dataType, singleItem, list)
             }
            // adapter.notifyDataSetChanged()
         }else{
             val flag = viewModel.itemCheckUp(viewModel.argumentTarget.value.toString(),
                 viewModel.getSelectedLangCountry()!!
             )
            // Log.i(TAG, "target flag: " + flag)
             if(flag){
                 val singleItem = viewModel.recentlist.distinctBy { it.langNameEN to it.countryName }.toList()
                 adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
             }

         }*/
        viewModel.navigateToLangItemName.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                if (viewModel.dataList == "camera") {

                    if (it.langNameEN == "Auto Detected") {
                        Log.i(TAG, "Auto Detect Language: " + night.langNameEN)
                        viewModel.setSourceItemValue(night)
                        this.findNavController()
                            .navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToCameraFragmentId(
                                    "nothing"
                                )
                            )
                    } else {
                        viewModel.itemInsertWithSingle(night)
                        if (args.type == "source") {

                            viewModel.setSourceItemValue(night)
                            this.findNavController()
                                .navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToCameraFragmentId(
                                        "nothing"
                                    )
                                )
                        } else {
                            viewModel.setTargetItemValue(night)
                            this.findNavController()
                                .navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToCameraFragmentId(
                                        "nothing"
                                    )
                                )
                        }

                    }

                }else if(viewModel.dataList == "screen"){
                    if (it.langNameEN == "Auto Detected") {
                        Log.i(TAG, "Auto Detect Language: " + night.langNameEN)
                        viewModel.setSourceItemValue(night)
                        //this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId().setTab(4))
                        NavHostFragment.findNavController(this).popBackStack()
                    } else {
                        viewModel.itemInsertWithSingle(night)
                        if (args.type == "source") {

                            viewModel.setSourceItemValue(night)
                           // this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId().setTab(4))
                            NavHostFragment.findNavController(this).popBackStack()
                        } else {
                            viewModel.setTargetItemValue(night)
                           // this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId().setTab(4))
                            NavHostFragment.findNavController(this).popBackStack()
                        }

                    }
                }
                else {
                    if (it.langNameEN == "Auto Detected") {
                        Log.i(TAG, "Auto Detect Language: " + night.langNameEN)
                        viewModel.setSourceItemValue(night)
                        //this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId())
                        this.findNavController().navigate(R.id.action_language_selection_fragment_id_to_main_fragment_id)
                    } else {
                        viewModel.itemInsertWithSingle(night)
                        if (args.type == "source") {

                            viewModel.setSourceItemValue(night)
                         //   this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId())
                            this.findNavController().navigate(R.id.action_language_selection_fragment_id_to_main_fragment_id)

                        } else {
                            viewModel.setTargetItemValue(night)
                           // this.findNavController().navigate(LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentIdToMainFragmentId())
                            this.findNavController().navigate(R.id.action_language_selection_fragment_id_to_main_fragment_id)
                        }

                    }
                }
                viewModel.onLanguageItemNavigated()
            }
        })
        binding.searchWidgetLayoutId.searchFieldId.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    binding.searchWidgetLayoutId.searchFieldId.isCursorVisible = true
                    binding.searchWidgetLayoutId.searchButtonId.setImageResource(R.drawable.ic_close_black_icon)
                    binding.searchWidgetLayoutId.searchButtonId.isClickable = true
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        /* binding.searchWidgetLayoutId.searchFieldId.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 val text = binding.searchWidgetLayoutId.searchFieldId.text.toString()
                 if(text.isNotEmpty()){
                     if(adapter.data.count() == 0){
                         binding.emptyStateLayoutId.visibility = View.VISIBLE
                     }
                     hideKeyboard(v)

                 }
                 return@OnEditorActionListener true
             }
             false
         })
 */
        binding.searchWidgetLayoutId.searchButtonId.setOnClickListener {
            if (it.isClickable) {
                hideKeyboard(it)
                binding.searchWidgetLayoutId.searchButtonId.setImageResource(R.drawable.ic_search_black_icon)
                binding.searchWidgetLayoutId.searchFieldId.text.clear()
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.searchWidgetLayoutId.searchFieldId.isCursorVisible = false
                binding.searchWidgetLayoutId.searchButtonId.isClickable = false
                adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                adapter.notifyDataSetChanged()
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
                        binding.emptyStateLayoutId.visibility = View.VISIBLE
                        binding.fragmentLangRecyclerViewId.visibility = View.GONE
                    } else {
                        Log.i(TAG, "count " + adapter.data.count())
                        binding.emptyStateLayoutId.visibility = View.GONE
                        binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                    }
                } else {
                    binding.emptyStateLayoutId.visibility = View.GONE
                    binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                    adapter.updateList(viewModel.dataType, viewModel.recentlist, list)
                    //adapter.notifyDataSetChanged()
                }


            }

        })


    }

    fun addSubmitList(recent: List<LanguageItem>?, list: List<LanguageItem>?): List<DataItem> {
        val items: List<DataItem>
        val camSrcList: MutableList<LanguageItem>? = ArrayList<LanguageItem>()
        adapter.positionSize = recent?.size ?: 0
        if (viewModel.dataList == "camera") {
            if (viewModel.dataType == "source") {
                camSrcList?.addAll(removeUnsupportedLanguages(recent, list)!!)
                items = when (camSrcList) {
                    null -> listOf(DataItem.AutoDetect) + list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.AutoDetect) + listOf(DataItem.Recent) + camSrcList.map {
                        DataItem.RecentNightItem(
                            it
                        )
                    } + listOf(DataItem.LanguageHeader) + list!!.map { DataItem.SleepNightItem(it) }
                }
            } else {
                items = when (recent) {
                    null -> list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.Recent) + recent.map { DataItem.RecentNightItem(it) } + listOf(
                        DataItem.LanguageHeader
                    ) + list!!.map { DataItem.SleepNightItem(it) }
                }
            }
        }else if(viewModel.dataList == "screen"){
            if (viewModel.dataType == "source") {
                camSrcList?.addAll(removeUnsupportedLanguages(recent, list)!!)
                items = when (camSrcList) {
                    null -> listOf(DataItem.AutoDetect) + list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.AutoDetect) + listOf(DataItem.Recent) + camSrcList.map {
                        DataItem.RecentNightItem(
                            it
                        )
                    } + listOf(DataItem.LanguageHeader) + list!!.map { DataItem.SleepNightItem(it) }
                }
            } else {
                items = when (recent) {
                    null -> list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.Recent) + recent.map { DataItem.RecentNightItem(it) } + listOf(
                        DataItem.LanguageHeader
                    ) + list!!.map { DataItem.SleepNightItem(it) }
                }
            }
        } else {
            if (viewModel.dataType == "source") {
                items = when (recent) {
                    null -> listOf(DataItem.AutoDetect) + list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.AutoDetect) + listOf(DataItem.Recent) + recent.map {
                        DataItem.RecentNightItem(
                            it
                        )
                    } + listOf(DataItem.LanguageHeader) + list!!.map { DataItem.SleepNightItem(it) }
                }
            } else {
                items = when (recent) {
                    null -> list!!.map { DataItem.SleepNightItem(it) }
                    else -> listOf(DataItem.Recent) + recent.map { DataItem.RecentNightItem(it) } + listOf(
                        DataItem.LanguageHeader
                    ) + list!!.map { DataItem.SleepNightItem(it) }
                }
            }
        }
        return items
    }

    private fun removeUnsupportedLanguages(
        recent: List<LanguageItem>?,
        list: List<LanguageItem>?
    ): MutableList<LanguageItem> {
        val listOut: MutableList<LanguageItem> = mutableListOf()
        recent?.forEach { r ->
            list?.forEach { l ->
                if (l.langNameEN == r.langNameEN && l.countryName == r.countryName) {
                    listOut.add(r)
                    Log.i(TAG, "addSubmitList: ${list}")
                }
            }
        }
        return listOut
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        imm.hideSoftInputFromWindow(activity!!.currentFocus?.windowToken, 0);
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        view?.let { hideKeyboard(it) }

    }

    companion object {
        private const val TAG = "LanguageSelectionFragment"
    }


}
