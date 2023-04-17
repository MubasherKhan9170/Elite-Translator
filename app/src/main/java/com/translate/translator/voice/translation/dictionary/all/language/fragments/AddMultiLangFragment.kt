package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.translate.translator.voice.translation.dictionary.all.language.R

import com.translate.translator.voice.translation.dictionary.all.language.adapters.AutoNightListener
import com.translate.translator.voice.translation.dictionary.all.language.adapters.CountryListAdapter
import com.translate.translator.voice.translation.dictionary.all.language.adapters.DataItem
import com.translate.translator.voice.translation.dictionary.all.language.adapters.SleepNightListener
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.SharedItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentAddMultiLangBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel

import dagger.hilt.android.AndroidEntryPoint


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddMultiLangFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AddMultiLangFragment : Fragment() {

    private lateinit var binding: FragmentAddMultiLangBinding
    private val viewModel: TranslatorMultiViewModel by activityViewModels()
    private lateinit var langListAdapter: CountryListAdapter<ViewModel>
    private lateinit var toolbar: Toolbar

    //val list = mutableListOf<LanguageItem>()
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                viewModel.multiselect_list.clear()
                NavHostFragment.findNavController(this@AddMultiLangFragment).popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.fragment_add_multi_lang, container, false)
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_multi_lang, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // viewModel.multiSelection = true
        toolbar = view.findViewById<Toolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        viewModel.type = "target"

        langListAdapter = CountryListAdapter(SleepNightListener { language ->
            Log.i(TAG, "Select listener: " + language)
            val sharedItem = SharedItem(
                displayName = language.displayName,
                langNameEN = language.langNameEN,
                langNameLocal = language.langNameLocal,
                showCountryName = language.showCountryName,
                countryName = language.countryName,
                countryCode = language.countryCode,
                bcp47 = language.bcp47,
                iso3 = language.iso3
            )
            multi_select(sharedItem)
        }, AutoNightListener { }, viewModel, requireContext())

        binding.fragmentLangRecyclerViewId.adapter = langListAdapter

        // get flags class instance
        val countryFlags = CountrySymbols.Builder(this.requireActivity()).build()

        val displayLanguage = resources.getStringArray(R.array.lang_array_display_name)
        val showCountry = resources.getStringArray(R.array.lang_array_show_country)

        val languageEnString = resources.getStringArray(R.array.lang_array_name_en)
        val languageLocalString = resources.getStringArray(R.array.lang_array_name_local)
        val countryNameString = resources.getStringArray(R.array.lang_array_county_variant)
        val countryCodeString = resources.getStringArray(R.array.lang_array_country_code)
        val bcp47CodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)
        val iso3CodeString = resources.getStringArray(R.array.lang_array_iso_3_code)


        val length = countryCodeString.size
        if (viewModel.list.isNullOrEmpty()) {
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
                viewModel.list.add(item)
            }
        }

        Log.i(TAG, "onViewCreated: " + viewModel.multiselect_list.size)

        langListAdapter.data.clear()
        langListAdapter.data.addAll(addSubmitList(null, viewModel.list))
        langListAdapter.filterItem.addAll(langListAdapter.data)



        toolbar.setNavigationOnClickListener {
            viewModel.multiselect_list.clear()
            it.findNavController().popBackStack()

        }

        viewModel.selectedItemsList.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "onViewCreated: " + it.size)
            if (viewModel.multiselect_list.isEmpty()) {
                it.forEachIndexed { index, multiLangItem ->
                    val item = SharedItem(
                        null,
                        multiLangItem.langNameEN,
                        multiLangItem.langNameLocal,
                        null,
                        multiLangItem.countryName,
                        multiLangItem.countryCode,
                        multiLangItem.bcp47,
                        multiLangItem.iso3
                    )
                    viewModel.multiselect_list.add(index, item)
                }
                refreshAdapter()
            }

        })

    }

    override fun onResume() {
        super.onResume()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.i(TAG, "onCreateOptionsMenu: ")
        inflater.inflate(R.menu.action_bar_multi_add, menu)

        // Associate searchable configuration with the SearchView
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search_id).actionView as SearchView).apply {
            this.queryHint = context.getString(R.string.multi_add_fragment_search_hint)
            this.maxWidth = android.R.attr.width
            this.setIconifiedByDefault(true)
            this.setOnQueryTextListener(object : OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isNotEmpty()) {
                        langListAdapter.filter.filter(query)
                    } else {
                        refreshAdapter()
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE


                    return true
                }

            })

        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected: ")
        return when (item.itemId) {
            R.id.app_bar_search_id -> {
                // navigate to settings screen
                true
            }
            R.id.app_bar_done_id -> {
                viewModel.clearMultiTable()
                viewModel.insertToMultiLangTable(viewModel.multiselect_list)
                NavHostFragment.findNavController(this).popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun multi_select(item: SharedItem) {
        val new = item.copy(
            displayName = null,
            langNameEN = item.langNameEN,
            langNameLocal = item.langNameLocal,
            showCountryName = null,
            countryName = item.countryName,
            countryCode = item.countryCode,
            bcp47 = item.bcp47,
            iso3 = item.iso3
        )
        if (viewModel.multiselect_list.contains(new)) {
            viewModel.multiselect_list.remove(new)
            Toast.makeText(context, "Unselect Language is ${new.langNameEN}", Toast.LENGTH_SHORT)
                .show()
        } else {

            viewModel.multiselect_list.add(new)
            Toast.makeText(context, "Select Language is ${new.langNameEN}", Toast.LENGTH_SHORT)
                .show()
        }
        refreshAdapter()

    }


    fun refreshAdapter() {
        langListAdapter.data.clear()
        langListAdapter.data.addAll(addSubmitList(null, viewModel.list))
        langListAdapter.notifyDataSetChanged()
    }


    private fun addSubmitList(
        recent: List<LanguageItem>?,
        list: List<LanguageItem>?
    ): List<DataItem> {
        langListAdapter.positionSize = recent?.size ?: 0
        return listOf(DataItem.LanguageHeader) + list!!.map { DataItem.SleepNightItem(it) }
    }


    companion object {
        private const val TAG = "AddMultiLangFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddMultiLangFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddMultiLangFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}