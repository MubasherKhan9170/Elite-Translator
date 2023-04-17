package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.translate.translator.voice.translation.dictionary.all.language.adapters.*
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.NavHostFragment
import androidx.work.WorkInfo
import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem
import com.translate.translator.voice.translation.dictionary.all.language.data.SharedItem
import com.translate.translator.voice.translation.dictionary.all.language.util.Language
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.ItemSelectionEvent
import com.google.android.material.snackbar.Snackbar
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentMultiTransMainBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MultiTransMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class MultiTransMainFragment : Fragment() {
    private lateinit var binding: FragmentMultiTransMainBinding
    private val viewModel: TranslatorMultiViewModel by activityViewModels()
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var  callback: OnBackPressedCallback
    private lateinit var langListAdapter: CountryListAdapter<ViewModel>
    private lateinit var flagListAdapter: FlagIconsAdapter
    private var multiResultAdapter: MultiResultAdapter? = null
    private lateinit var dialog: Dialog
    var mConstraintSet1 = ConstraintSet() // create a Constraint Set
    var mConstraintLayout: ConstraintLayout? = null // cache the ConstraintLayout
    private lateinit var  toolbar: Toolbar
    

    //val list = mutableListOf<LanguageItem>()

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            context
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder().setLanguage(viewModel.getSourceLangIso3Code()).build()
                textToSpeechEngine.language = locale
                Log.i(TAG, "Source $locale")
            }
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                val intent = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                intent?.let {
                    val recognizedText = it[0]
                    binding.sourceTextLayout.messageInput.setText(recognizedText)
                }

            }
        }

    private val textToSpeechEngineOutput: TextToSpeech by lazy {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                /*val locale = Locale.Builder().setLanguage(viewModel.getTargetLangIso3Code()).build()
                //  Log.i(TAG, "Target $locale")
                textToSpeechEngineOutput.language = locale*/

            }
        }
    }


    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Do something for new state
            when(newState){
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    //callback.isEnabled = false
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    // TODO()
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    callback.isEnabled = true

                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    // TODO()
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    // TODO()
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    //  TODO()
                }
            }

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Do something for slide offset
            // Animate Y clockwise
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(
            false // default to enabled
        ) {
            override fun handleOnBackPressed() {

                if(mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                }
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_multi_trans_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        toolbar = view.findViewById<Toolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        Log.i(TAG, "onViewCreated: ")
        viewModel.type = "source"
        binding.sourceTextLayout.cameraViewIconId.visibility = View.GONE
        mConstraintLayout = binding.sourceTextLayout.editableLayout
        mConstraintSet1.clone(mConstraintLayout)
        mConstraintSet1.createHorizontalChain(
            ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID, ConstraintSet.LEFT, intArrayOf(binding.sourceTextLayout.cameraViewIconId.id, binding.sourceTextLayout.microphoneViewIconId.id, binding.sourceTextLayout.speakerIconId.id, binding.sourceTextLayout.translationButtonId.id),
            null,
            ConstraintSet.CHAIN_SPREAD_INSIDE
        )
        binding.sourceTextLayout.translationButtonId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        mConstraintSet1.setMargin(binding.sourceTextLayout.microphoneViewIconId.id, ConstraintSet.START, resources.getDimension(R.dimen.spacing_normal).toInt())
        mConstraintSet1.setMargin(binding.sourceTextLayout.speakerIconId.id, ConstraintSet.END, resources.getDimension(R.dimen.spacing_normal).toInt())
        mConstraintSet1.applyTo(mConstraintLayout)

        //binding.sourceTextLayout.speakerIconId.marginLeft = resources.getDimension(R.dimen.element_margin_normal).toInt()
        dialog = Dialog(requireContext())

        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetViewId)
        // change the state of the bottom sheet
        mBottomSheetBehavior.isDraggable = false
        mBottomSheetBehavior.peekHeight = 0
        mBottomSheetBehavior.isFitToContents = true

        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
         mBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        /*Bottom RecyclerView Adapter*/
        langListAdapter = CountryListAdapter(SleepNightListener { language ->
           // viewModel.autoItemState = false
            autoDetectStateInActive()
            langListAdapter.notifyDataSetChanged()
            binding.bottomSheetRecyclerViewId.scrollToPosition(0)
            viewModel.onLanguageItemClicked(language)
        }, AutoNightListener {
           // viewModel.autoItemState = true
            autoDetectStateActive()
            langListAdapter.notifyDataSetChanged()
            binding.bottomSheetRecyclerViewId.scrollToPosition(0)

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
        },viewModel, requireContext())

        flagListAdapter = FlagIconsAdapter()
        multiResultAdapter = MultiResultAdapter(SpeakerIconListener { pos, text ->
            playSound(pos, text)
        },
        CopiedIconListener {
                           copiedResult(it)

        },
        FullScreenIconListener {
             openFullScreen(it)
         },
        ShareIconListener {
            openShareSheet(it)
        })

        binding.bottomSheetRecyclerViewId.adapter = langListAdapter
        binding.addLangLayout.horizontalRecyclerView.adapter = flagListAdapter
        binding.resultViewId.adapter = multiResultAdapter

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
        if(viewModel.list.isNullOrEmpty()){
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
        langListAdapter.data.clear()
        langListAdapter.data.addAll(addSubmitList(null, viewModel.list))
        langListAdapter.filterItem.addAll(langListAdapter.data)

        /*binding.topSourceId.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }*/

        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "onViewCreated: up back pressed "+ it.findNavController().popBackStack(R.id.main_fragment_id, true, true))
            (activity as AppCompatActivity).finish()


        }

        binding.addLangLayout.imageView3.setOnClickListener {
            it.findNavController().navigate(MultiTransMainFragmentDirections.actionMultiTransMainFragmentToAddMultiLangFragment())
        }


        /*source layout
        * input text editor*/

        /*source camera view button on click listener*/
/*        binding.sourceTextLayout.cameraViewIconId.setOnClickListener {
            if(InternetUtils.get(context).value == true){
                val bundle = bundleOf("amount" to amount)
                view.findNavController().navigate(R.id.confirmationAction, bundle)

                it.findNavController().navigate( R.id.action_global_camera_fragment_id, )
            }else{

                Toast.makeText(context, "This feature isn't available offline", Toast.LENGTH_SHORT).show()
            }
        }*/

        /*source microphone button icon on click listener */
        binding.sourceTextLayout.microphoneViewIconId.setOnClickListener {
            showInContextUI(viewModel.mircoLangCode)
            dialog.show()

        }

        /*source clear text button icon on click listener*/
        binding.sourceTextLayout.clearTextIconId.setOnClickListener {
            binding.sourceTextLayout.clearTextIconId.visibility = View.GONE
            binding.sourceTextLayout.messageInput.text?.clear()
           // binding.targetTextLayout.messageOutput.text = ""
            viewModel.setHomeTextNull()
          //  viewModel.setTranslatedHomeTextNull()
           // binding.targetTextLayout.root.visibility = View.GONE
            multiResultAdapter?.let {
                multiResultAdapter!!.data.clear()
                multiResultAdapter!!.notifyDataSetChanged()
            }
            stopSourceSpeakingVoice()
            viewModel.resultList.clear()
            stopTargetSpeakingVoice()
            if(InternetUtils.get(context).value == false){
                offlineStateWithEmptyTextField()
            }
        }

        /*source speaker button icon on click listener*/
        binding.sourceTextLayout.speakerIconId.setOnClickListener {
            val text = binding.sourceTextLayout.messageInput.text.toString().trim()

            if(InternetUtils.get(context).value == true){
                if (text.isNotEmpty()) {
                    if(viewModel.autoItemState){
                        if(viewModel.langCode != "und"){

                            if (textToSpeechEngine.isLanguageAvailable(
                                    Locale(
                                        viewModel.langCode
                                    )
                                ) == TextToSpeech.LANG_NOT_SUPPORTED
                            ) {
                                Log.i(
                                    TAG,
                                    "Not Supporting Languages: " + viewModel.langCode
                                )

                                Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Log.i(TAG, "SourceSpeakIcon: " + textToSpeechEngine.language.toString())
                                stopTargetSpeakingVoice()
                                textToSpeechEngine.language = Locale(viewModel.langCode)
                                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                            }
                        }

                    }else{
                        if (textToSpeechEngine.isLanguageAvailable(
                                Locale(
                                    viewModel.getSourceLangIso3Code().toString()
                                )
                            ) == TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            Log.i(
                                TAG,
                                "Not Supporting Languages: " + viewModel.getSourceLangIso3Code().toString()
                            )

                            Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.i(TAG, "SourceSpeakIcon: " + textToSpeechEngine.language.toString())
                            stopTargetSpeakingVoice()
                            textToSpeechEngine.language = Locale(viewModel.getSourceLangIso3Code().toString())

                            textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                        }
                    }


                } else {
                    Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }

        }
        /*source translation text button icon on click listener*/
        binding.sourceTextLayout.translationButtonId.setOnClickListener {

            hideKeyboard(it)
            val text = binding.sourceTextLayout.messageInput.text.toString().trim()
            stopSourceSpeakingVoice()
            stopTargetSpeakingVoice()
            multiResultAdapter?.let {
                multiResultAdapter!!.data.clear()
                multiResultAdapter!!.notifyDataSetChanged()
            }

            viewModel.resultList.clear()

            binding.sourceTextLayout.messageInput.isCursorVisible = false

            if(InternetUtils.get(context).value == true){

                if (text.isNotEmpty() && !viewModel.selectedItemsList.value.isNullOrEmpty()) {
                    if (UserPreferencesRepository.getInstance(requireContext())
                            .getPurchaseStatus(INAPP_Feature) == 0 && UserPreferencesRepository.getInstance(
                            requireContext()
                        ).getPurchaseStatus(SUBS_Feature) == 0
                    ) {
                        it.findNavController()
                            .navigate(MultiTransMainFragmentDirections.actionMultiTransMainFragmentToMultipremiumActivity())
                    }else{
                        binding.sourceTextLayout.clearTextIconId.isClickable = false
                        viewModel.setHomeText(text)


                        binding.sourceTextLayout.translationButtonId.setTextColor(
                            resources.getColor(
                                android.R.color.transparent,
                                null
                            )
                        )
                        binding.sourceTextLayout.translationButtonId.setIconTintResource(android.R.color.transparent)
                        binding.sourceTextLayout.progressBar.visibility = View.VISIBLE
                        binding.loadingLayout.root.visibility= View.VISIBLE


                        if(viewModel.autoItemState && viewModel.selectedItemsList.value!!.isNotEmpty()){
                            Log.e(TAG, "onViewCreated: pass")
                            viewModel.languageIdentifier.identifyLanguage(text)
                                .addOnSuccessListener { languageCode ->

                                    if (languageCode != "und"){

                                        Log.d(TAG, "language identifier: "+ Language(languageCode))
                                        viewModel.langCode = Language(languageCode).code
                                        binding.sourceTextLayout.textView14.text = Language(languageCode).displayName
                                        binding.sourceTextLayout.textView14.visibility = View.VISIBLE
                                        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)

                                        val offset = viewModel.calculateOffset(viewModel.selectedItemsList.value!!.size)

                                        viewModel.builtJsonObject()


                                        viewModel.selectedItemsList.value?.forEachIndexed { index, multiLangItem ->
                                            viewModel.doTranslation(text, viewModel.getSourceLangIso3Code()!!, multiLangItem.iso3!!)
                                            viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                                                .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                                                    Log.i(TAG, "Work State: " + workInfo?.state)
                                                    if (workInfo?.state == WorkInfo.State.SUCCEEDED) {

                                                        // viewModel.setTranslatedHomeText(workInfo.outputData.getString("translatedText"))
                                                        // insert the translation item in history table of database
                                                        //viewModel.addToHistory()


                                                        binding.loadingLayout.progressBar2.progress = binding.loadingLayout.progressBar2.progress+ offset
                                                        binding.loadingLayout.textView20.text = binding.loadingLayout.progressBar2.progress.toString()

                                                        multiResultAdapter!!.data.add(
                                                            ResultItem(
                                                                multiLangItem.langNameEN,
                                                                workInfo.outputData.getString("translatedText"),
                                                                countryFlags.getCountryFlagIcon(
                                                                    multiLangItem.countryCode
                                                                )
                                                            )
                                                        )
                                                        viewModel.resultList.add(ResultItem(
                                                            multiLangItem.langNameEN,
                                                            workInfo.outputData.getString("translatedText"),
                                                            countryFlags.getCountryFlagIcon(
                                                                multiLangItem.countryCode
                                                            )
                                                        ))
                                                        viewModel.builtResultJson()
                                                        multiResultAdapter!!.notifyDataSetChanged()

                                                        if(index == viewModel.selectedItemsList.value!!.size-1){
                                                            binding.sourceTextLayout.clearTextIconId.isClickable = true
                                                            binding.loadingLayout.root.visibility = View.GONE
                                                            binding.loadingLayout.progressBar2.progress = 0
                                                            binding.loadingLayout.textView20.text = "0"
                                                            viewModel.addToHistory()
                                                        }

                                                        // Do something with progress information
                                                        binding.sourceTextLayout.progressBar.visibility = View.GONE
                                                        binding.sourceTextLayout.translationButtonId.setTextColor(
                                                            resources.getColor(
                                                                R.color.primaryTextColor,
                                                                null
                                                            )
                                                        )
                                                        binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.primaryTextColor)

                                                        //binding.targetTextLayout.root.visibility = View.VISIBLE
                                                    }
                                                    if (workInfo?.state == WorkInfo.State.FAILED) {
                                                        binding.sourceTextLayout.clearTextIconId.isClickable = true
                                                        binding.sourceTextLayout.progressBar.visibility = View.GONE
                                                        binding.sourceTextLayout.translationButtonId.setTextColor(
                                                            resources.getColor(
                                                                R.color.primaryTextColor,
                                                                null
                                                            )
                                                        )
                                                        binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.primaryTextColor)
                                                        // show dialog
                                                    }

                                                })



                                        }

                                    }else{
                                        Log.d(TAG, "language identifier: "+ Language(languageCode))
                                        viewModel.langCode = languageCode
                                        binding.sourceTextLayout.clearTextIconId.isClickable = true
                                        binding.sourceTextLayout.progressBar.visibility = View.GONE
                                        binding.sourceTextLayout.translationButtonId.setTextColor(
                                            resources.getColor(
                                                R.color.primaryTextColor,
                                                null
                                            )
                                        )
                                        binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.primaryTextColor)
                                        // show dialog
                                    }

                                }

                        }else{
                            binding.sourceTextLayout.clearTextIconId.isClickable = true
                        }

                    }

                } else {
                    if(text.isNullOrEmpty()){
                        Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
                    }
                    if(viewModel.selectedItemsList.value.isNullOrEmpty()){
                        Toast.makeText(context, "Please select the languages for translation", Toast.LENGTH_SHORT).show()
                    }

                }
            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }

        }

        /*source text edit field text change listener*/
        binding.sourceTextLayout.messageInput.addTextChangedListener(object : TextWatcher {
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
                Log.i(TAG, "afterTextChanged: ")
                if(binding.sourceTextLayout.messageInput.text.isNullOrEmpty()){
                   // binding.targetTextLayout.root.visibility = View.GONE
                    binding.sourceTextLayout.clearTextIconId.visibility = View.GONE
                    binding.sourceTextLayout.translationButtonId.isClickable = false
                    multiResultAdapter?.let {
                        multiResultAdapter!!.data.clear()
                        multiResultAdapter!!.notifyDataSetChanged()
                    }
                    viewModel.resultList.clear()
                    if(viewModel.autoItemState){
                        binding.sourceTextLayout.textView14.visibility = View.GONE
                        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_gray_icon)
                    }
                }else{
                    binding.sourceTextLayout.clearTextIconId.visibility = View.VISIBLE
                    binding.sourceTextLayout.translationButtonId.isClickable = true
                }
            }

        })
        /*source text edit field on focus listener*/
        binding.sourceTextLayout.messageInput.setOnClickListener{
            binding.sourceTextLayout.messageInput.isCursorVisible = true
            stopSourceSpeakingVoice()
            stopTargetSpeakingVoice()
        }


        viewModel.selectedItemsList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.i(TAG, "observe: "+ it.size)
            if(it.isEmpty()){
                binding.addLangLayout.emptyStateId.visibility = View.VISIBLE
            }else{
                binding.addLangLayout.emptyStateId.visibility = View.GONE
            }

            viewModel.multiselect_list.clear()

            flagListAdapter.data.clear()
            flagListAdapter.notifyDataSetChanged()
            binding.addLangLayout.countId.text = it.size.toString()
            it?.let {
                it.forEachIndexed { index, multiLangItem ->
                    flagListAdapter.data.add(index, countryFlags.getCountryFlagIcon(multiLangItem.countryCode))

                    viewModel.multiselect_list.add(
                        SharedItem(
                            displayName = null,
                            langNameEN = multiLangItem.langNameEN,
                        langNameLocal = multiLangItem.langNameLocal,
                        countryName = multiLangItem.countryName,
                            showCountryName = null,
                        countryCode = multiLangItem.countryCode,
                        bcp47 = multiLangItem.bcp47,
                        iso3 = multiLangItem.iso3)
                    )
                }

                flagListAdapter.notifyDataSetChanged()
            }
            if(MultiTransMainFragment.itemEvent?.update() == true){
                viewModel.resultList = MultiTransMainFragment.itemEvent!!.setResult()
                viewModel.resultList.let {
                    Log.e(TAG, "onViewCreated: ResultItem" + it.size + MultiTransMainFragment.itemEvent!!.setSourceText())
                    viewModel.setHomeText(MultiTransMainFragment.itemEvent!!.setSourceText())
                    binding.sourceTextLayout.messageInput.setText(viewModel.homeText.value)

                    viewModel.languageIdentifier.identifyLanguage(viewModel.homeText.value)
                        .addOnSuccessListener { languageCode ->

                            if (languageCode != "und") {

                                Log.d(TAG, "language identifier: " + Language(languageCode))
                                viewModel.langCode = Language(languageCode).code
                                binding.sourceTextLayout.textView14.text =
                                    Language(languageCode).displayName
                                binding.sourceTextLayout.textView14.visibility = View.VISIBLE
                                binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
                                viewModel.sourceIso3Code = languageCode
                                textToSpeechEngine.language = Locale(viewModel.getSourceLangIso3Code().toString())
                            }
                        }

                    multiResultAdapter!!.data.clear()
                    multiResultAdapter!!.notifyDataSetChanged()
                    multiResultAdapter!!.data.addAll(it)
                    multiResultAdapter!!.notifyDataSetChanged()
                    MultiTransMainFragment.itemEvent?.default()
                }
            }else{
                viewModel.resultList.let {
                    multiResultAdapter!!.data.clear()
                    multiResultAdapter!!.notifyDataSetChanged()
                    multiResultAdapter!!.data.addAll(it)
                    multiResultAdapter!!.notifyDataSetChanged()

                }

            }
        })

        viewModel.navigateToLangItemName.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
        it?.let {
            viewModel.mircoLangCode = it.bcp47.toString()
            viewModel.setSourceItemValue(it)
            viewModel.onLanguageItemNavigated()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            if(InternetUtils.get(context).value == true){
                val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                sttIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                Log.d(TAG, "onViewCreated: language " + viewModel.mircoLangCode)
                sttIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE, viewModel.mircoLangCode
                )
                sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
                try {
                    // TODO: show the dialog
                    startForResult.launch(sttIntent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.not_support_stt), Toast.LENGTH_SHORT)
                        .show()
                }


            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }

        }

        })


        binding.searchWidgetLayoutId.searchButtonId.setOnClickListener {
            if (it.isClickable) {
                hideKeyboard(it)
                binding.searchWidgetLayoutId.searchButtonId.setImageResource(R.drawable.ic_search_black_icon)
                binding.searchWidgetLayoutId.searchFieldId.text.clear()
               // binding.emptyStateLayoutId.visibility = View.GONE
                binding.searchWidgetLayoutId.searchFieldId.isCursorVisible = false
                binding.searchWidgetLayoutId.searchButtonId.isClickable = false
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
                    langListAdapter.filter.filter(text)
                    if (langListAdapter.data.count() == 0) {
                        Log.i(TAG, "count " + langListAdapter.data.count())
                       // binding.emptyStateLayoutId.visibility = View.VISIBLE
                        //binding.fragmentLangRecyclerViewId.visibility = View.GONE
                    } else {
                        Log.i(TAG, "count " + langListAdapter.data.count())
                        //binding.emptyStateLayoutId.visibility = View.GONE
                        //binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                    }
                } else {
                   // binding.emptyStateLayoutId.visibility = View.GONE
                    //binding.fragmentLangRecyclerViewId.visibility = View.VISIBLE
                        langListAdapter.data.clear()
                    langListAdapter.data.addAll(addSubmitList(null, viewModel.list))
                    langListAdapter.notifyDataSetChanged()
                }


            }

        })

    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        imm.hideSoftInputFromWindow(activity!!.currentFocus?.windowToken, 0);
    }


    private fun onlineStateOperation() {
        binding.sourceTextLayout.messageInput.visibility = View.VISIBLE
        binding.sourceTextLayout.offlineView.visibility = View.GONE
        binding.sourceTextLayout.cameraViewIconId.setImageResource(R.drawable.ic_camera_black_icon)
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_black_icon)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
        binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.primaryTextColor)
        binding.sourceTextLayout.translationButtonId.setTextColor(
            resources.getColor(
                R.color.primaryTextColor,
                null
            )
        )
        binding.sourceTextLayout.translationButtonId.setBackgroundColor(
            resources.getColor(
                R.color.primaryColor,
                null
            )
        )
        binding.sourceTextLayout.translationButtonId.elevation = 2F
      //  binding.targetTextLayout.speakerViewId.setImageResource(R.drawable.ic_voice_white_icon)
    }

    private fun offlineStateWithEmptyTextField() {
        binding.sourceTextLayout.messageInput.visibility = View.GONE
        binding.sourceTextLayout.offlineView.visibility = View.VISIBLE
        offlineCommonViewState()
    }

    private fun offlineStateHaveText() {
        binding.sourceTextLayout.messageInput.visibility = View.VISIBLE
        binding.sourceTextLayout.offlineView.visibility = View.GONE
        offlineCommonViewState()
    }

    private fun offlineCommonViewState() {
        binding.sourceTextLayout.cameraViewIconId.setImageResource(R.drawable.ic_camera_gray_icon)
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_gray_icon)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_gray_icon)
        binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.offTextColor)
        binding.sourceTextLayout.translationButtonId.setTextColor(
            resources.getColor(
                R.color.offTextColor,
                null
            )
        )
        binding.sourceTextLayout.translationButtonId.setBackgroundColor(
            resources.getColor(
                R.color.offColor,
                null
            )
        )
        binding.sourceTextLayout.translationButtonId.elevation = 0F
       // binding.targetTextLayout.speakerViewId.setImageResource(R.drawable.ic_voice_white_gray_icon)
    }


    private fun autoDetectStateActive(){
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_black_icon)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_gray_icon)

    }

    private fun autoDetectStateInActive(){
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_black_icon)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
    }

    /*stop source speaking voice*/
    private fun stopSourceSpeakingVoice(){
        if(textToSpeechEngine.isSpeaking){
            textToSpeechEngine.stop()
        }
    }
    /*stop target speaking voice*/
    private fun stopTargetSpeakingVoice(){
        if(textToSpeechEngineOutput.isSpeaking){
            textToSpeechEngineOutput.stop()
        }
    }



    private fun checkAutoDetectState() {
        if (viewModel.autoItemState) {
            autoDetectStateActive()
            if(viewModel.langCode != "und" && viewModel.translatedHomeText.value != null){
                binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
            }
        } else {
            autoDetectStateInActive()
        }
    }


    private fun addSubmitList(recent: List<LanguageItem>?, list: List<LanguageItem>?): List<DataItem>{
        langListAdapter.positionSize = recent?.size ?: 0
        val items = when (recent) {
            null -> listOf(DataItem.LanguageHeader)+ list!!.map { DataItem.SleepNightItem(it)}
            else -> listOf(DataItem.AutoDetect) + listOf(DataItem.Recent) + recent.map { DataItem.RecentNightItem(it) }+ listOf(
                DataItem.LanguageHeader)  + list!!.map { DataItem.SleepNightItem(it) }
        }
        return items
    }

    private fun showInContextUI(
        language: String
    ) {
        dialog.setContentView(R.layout.popup_window_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
        val selectBtn = dialog.findViewById(R.id.select_button_id) as TextView
        //selectBtn.text = language
        val skipBtn = dialog.findViewById(R.id.skip_button_id) as TextView
        selectBtn.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            dialog.dismiss()
        }
        skipBtn.setOnClickListener {
            dialog.dismiss()

            if(InternetUtils.get(context).value == true){
                val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                sttIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                Log.d(TAG, "onViewCreated: language " + viewModel.mircoLangCode)
                sttIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE, "en-US"
                )
                sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
                try {
                    // TODO: show the dialog
                    startForResult.launch(sttIntent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.not_support_stt), Toast.LENGTH_SHORT)
                        .show()
                }


            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")

        InternetUtils.get(context).observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if(it == true){
                onlineStateOperation()
                checkAutoDetectState()
                //   Toast.makeText(context, "Internet Connected", Toast.LENGTH_LONG).show()
                Log.i(TAG, "Internet Connected")
            }else{
                checkAutoDetectState()
                if(binding.sourceTextLayout.messageInput.text.isNullOrEmpty()){
                    offlineStateWithEmptyTextField()
                    Log.i(TAG, "No Internet Connection with no text")

                }else{
                    offlineStateHaveText()
                    Log.i(TAG, "No Internet Connection have text")
                }

            }
        })
    }


    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        viewModel.type = null
        textToSpeechEngine.stop()
        textToSpeechEngineOutput.stop()
        view?.let { hideKeyboard(it) }

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        multiResultAdapter = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechEngine.shutdown()
        textToSpeechEngineOutput.shutdown()
        Log.i(TAG, "onDestroy: ")
    }


    fun playSound(pos: Int, text: String){
        if(InternetUtils.get(context).value == true){
            if (text.isNotEmpty()) {
                if (textToSpeechEngineOutput.isLanguageAvailable(
                        Locale(
                            viewModel.selectedItemsList.value?.get(pos)?.bcp47.toString()
                        )
                    ) == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.i(TAG, "targetSpeakIcon: " + textToSpeechEngineOutput.language.toString())
                    stopSourceSpeakingVoice()
                    textToSpeechEngineOutput.language = Locale(viewModel.selectedItemsList.value?.get(pos)?.bcp47.toString())
                    textToSpeechEngineOutput.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts2")
                }

            } else {
                Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
        }
    }

    fun copiedResult(text: String){
        val clipboard =
            context!!.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        if (text.isNotEmpty()) {
            val clip = ClipData.newPlainText(
                "Copied Text",
                text
            )
            clipboard.setPrimaryClip(clip)
            Snackbar.make(binding.root.rootView, resources.getText(R.string.text_copy_label), Snackbar.LENGTH_SHORT)
                .show()

        }
    }

    fun openFullScreen(text: String){
        viewModel.setTranslatedHomeText(text)
        if (!viewModel.homeText.value.isNullOrEmpty()) {
            NavHostFragment.findNavController(this).navigate(MultiTransMainFragmentDirections.actionMultiTransMainFragmentToMultiTranslationFragment())
        } else {
            Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openShareSheet(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }




    companion object {
        private const val TAG = "MultiTransMainFragment"

        const val INAPP_Feature = "InAppItem"
        const val SUBS_Feature = "SubsItem"

        var itemEvent: ItemSelectionEvent? = null
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MultiTransMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = MultiTransMainFragment()
    }
}