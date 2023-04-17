package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.adapters.*
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.VoiceDataItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentVoiceBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.CountrySymbols
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [VoiceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class VoiceFragment : Fragment() {

    private lateinit var binding: FragmentVoiceBinding
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var langListAdapter: CountryListAdapter<ViewModel>
    private lateinit var voiceListAdapter: VoiceListAdapter
    val list = mutableListOf<LanguageItem>()

    //variable for button click
    private var yellow: Boolean = false


    private val startForVoiceResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {


            val intent = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            //val lang = result.data?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)


            intent?.let {
                val recognizedText = it[0]
                //viewModel.setHomeText(recognizedText)

               // Log.i(TAG, "language: "+ it[3])
              //  binding.sourceTextLayout.messageInput.setText(recognizedText)
                if(InternetUtils.get(context).value == true){
                    if (recognizedText.isNotEmpty()) {
                        if(yellow){
                            viewModel.doVoiceTranslation(recognizedText,
                                viewModel.getSourceLangIso3Code()!!, viewModel.getTargetLangIso3Code()!!)
                        }else{
                            viewModel.doVoiceTranslation(recognizedText,
                                viewModel.getTargetLangIso3Code()!!, viewModel.getSourceLangIso3Code()!!)
                        }

                        viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                            .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                                Log.i(TAG, "Work State: " + workInfo?.state)
                                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                                  //  viewModel.setTranslatedHomeText(workInfo.outputData.getString("translatedText"))

                                    // Do something with progress information
                                    if(yellow){
                                        val item = VoiceDataItem(recognizedText, workInfo.outputData.getString("translatedText")!!, 0)
                                        viewModel.voiceItemList.add(item)
                                        voiceListAdapter.data.add(item)
                                        //get the size of the list
                                        viewModel._voice.value = viewModel.voiceItemList.size

                                    }else{
                                        val item = VoiceDataItem(recognizedText, workInfo.outputData.getString("translatedText")!!, 1)
                                        viewModel.voiceItemList.add(item)
                                        voiceListAdapter.data.add(item)
                                        //get the size of the list
                                        viewModel._voice.value = viewModel.voiceItemList.size
                                    }
                                    playingSound(workInfo.outputData.getString("translatedText")!!, yellow)

                                    // show clear button
                                    binding.clearListButtonId.visibility = View.VISIBLE

                                    voiceListAdapter.notifyItemChanged(voiceListAdapter.itemCount)
                                    binding.fragmentVoiceRecyclerViewId.smoothScrollToPosition(voiceListAdapter.itemCount.minus(1))
                                    // insert the translation item in history table of database
                                    viewModel.addToHistoryViaVoice(recognizedText, workInfo.outputData.getString("translatedText")!!)

                                }
                                if (workInfo?.state == WorkInfo.State.FAILED) {
                                    // dialog
                                }
                            })
                    } else {
                       // Toast.makeText(context, "Text cannot be empty", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(context, getString(R.string.voice_internet_disconnect), Toast.LENGTH_LONG).show()
                }
            }

        }
    }



    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            context
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {

                resetSoundPlayingCode(yellow)

            }
        }
    }



    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Do something for new state
            when(newState){
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if(viewModel.firstPersonFocusState){
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeColorResource(android.R.color.transparent)
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.strokeWidth = 0
                    }else{
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeColorResource(android.R.color.transparent)
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.strokeWidth = 0
                    }

                    viewModel.onLanguageItemNavigated()
                    binding.invalidateAll()

                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                   // TODO()
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    //langListAdapter.submitList(langListAdapter.currentList)
                    langListAdapter.notifyDataSetChanged()
                    if(viewModel.firstPersonFocusState){
                        viewModel.dataType = "source"
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeColorResource(R.color.red)
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeWidthResource(R.dimen.button_stroke_width)
                    }else{
                        viewModel.dataType = "target"
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeColorResource(R.color.red)
                        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeWidthResource(R.dimen.button_stroke_width)
                    }


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
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetMoveIcon.rotationX = slideOffset * 180
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voice, container, false)
        binding.model = viewModel
        // return inflater.inflate(R.layout.fragment_camera, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")

        val metrics = resources.displayMetrics
        val densityDpi = (metrics.density * 160f)
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.fragmentVoiceBottomSheetLayoutId.bottomSheetContainerId)
        // change the state of the bottom sheet
        mBottomSheetBehavior.isDraggable = true
        mBottomSheetBehavior.peekHeight = (86*(densityDpi/160f)).toInt()
        mBottomSheetBehavior.isFitToContents = true
       mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        mBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        /*Bottom RecyclerView Adapter*/
        langListAdapter = CountryListAdapter(SleepNightListener { language ->
            viewModel.onLanguageItemClicked(language)
        }, AutoNightListener {  },viewModel, requireContext())
        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetRecyclerViewId.adapter = langListAdapter

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
            list.add(item)
        }
        langListAdapter.data.addAll(addSubmitList(viewModel.recentlist, list))

        /*Voice List RecyclerView Adapter*/
        voiceListAdapter = VoiceListAdapter(VoiceItemListener{ text, type ->
            if(type == 0){
           //     stopSpeakingVoice()
                playingSound(text, true)
            }
            if(type == 1){
             //   stopSpeakingVoice()
                playingSound(text, false)
            }

        }, viewModel)
        binding.fragmentVoiceRecyclerViewId.adapter = voiceListAdapter





        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.text = viewModel.displayTarget.value

        viewModel.navigateToLangItemName.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                if(viewModel.firstPersonFocusState){
                    viewModel.dataType = "source"
                    viewModel.setSourceItemValue(night)
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    Log.i(TAG, "Change Source: " + viewModel.argumentSource.value)
                    binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.text = viewModel.displaySource.value

                }else{
                    viewModel.dataType = "target"
                    viewModel.setTargetItemValue(night)
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    Log.i(TAG, "change Target: " + viewModel.argumentTarget.value)
                    binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.text = viewModel.displayTarget.value
                }
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetRecyclerViewId.scrollToPosition(0)
                val item = LanguageItem(
                    it.displayName,
                    it.langNameEN,
                    it.langNameLocal,
                    it.showCountryName,
                    it.countryName,
                    it.flagIcon,
                    it.countryCode,
                    it.bcp47,
                    it.iso3
                )
                viewModel.itemInsertWithSingle(night)
                /*viewModel.recentlist.forEach { a ->
                    Log.i(TAG, "distinct: " + a)
                }*/
                langListAdapter.updateList(null, viewModel.recentlist, list)
            }

        })

        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setOnClickListener {
            if(mBottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED){
                viewModel.dataType = "source"
                viewModel.firstPersonFocusState = true
                viewModel.secondPersonFocusState = false
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeColorResource(R.color.red)
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeWidthResource(R.dimen.button_stroke_width)
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeColorResource(android.R.color.transparent)
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.strokeWidth = 0
                val flag = viewModel.itemCheckUp(viewModel.argumentSource.value.toString(),
                    viewModel.getSelectedLangCountry()!!, viewModel.getSourceLangBcp47Code()!!
                )
               // Log.i(TAG, "flag: " + flag)
                if(flag){
                    langListAdapter.updateList(null, viewModel.recentlist, list)
                }
                langListAdapter.notifyDataSetChanged()
            }else{

                if(InternetUtils.get(context).value == true){
                    val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    sttIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    Log.d(TAG, "onViewCreated: language " + viewModel.argumentSource.value.toString())
                    sttIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE, viewModel.getSourceLangBcp47Code()
                    )
                    sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

                    try {
                        yellow = true
                        startForVoiceResult.launch(sttIntent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        Toast.makeText(context, getString(R.string.not_support_stt), Toast.LENGTH_LONG)
                            .show()
                    }
                }else{
                    Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_LONG).show()
                }

            }

        }

        binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setOnClickListener {
            if(mBottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED){
                viewModel.dataType = "target"
                viewModel.firstPersonFocusState = false
                viewModel.secondPersonFocusState = true
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.setStrokeColorResource(android.R.color.transparent)
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.strokeWidth = 0
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeColorResource(R.color.red)
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetSecondPersonLangId.setStrokeWidthResource(R.dimen.button_stroke_width)
                val flag = viewModel.itemCheckUp(viewModel.argumentTarget.value.toString(),
                    viewModel.getSelectedLangCountry()!!, viewModel.getTargetLangBcp47Code()!!
                )
                // Log.i(TAG, "flag: " + flag)
                if(flag){
                    langListAdapter.updateList(null, viewModel.recentlist, list)
                }
                langListAdapter.notifyDataSetChanged()
            }else{

                if(InternetUtils.get(context).value == true){
                    val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    sttIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    Log.d(TAG, "onViewCreated: language " + viewModel.argumentSource.value.toString())
                    sttIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE, viewModel.getTargetLangBcp47Code()
                    )
                    sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

                    try {
                        yellow = false
                        startForVoiceResult.launch(sttIntent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        Toast.makeText(context, getString(R.string.not_support_stt), Toast.LENGTH_LONG)
                            .show()
                    }
                }else{
                    Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_LONG).show()
                }

            }

        }




        binding.clearListButtonId.setOnClickListener {
            viewModel.voiceItemList.clear()
            voiceListAdapter.data.clear()
            //get the size of the list
            viewModel._voice.value = viewModel.voiceItemList.size
            voiceListAdapter.notifyDataSetChanged()
            it.visibility = View.GONE
        }

        viewModel.voiceList.observe(viewLifecycleOwner, Observer {
            if(it == 0){
                binding.emptyStateLayoutId.visibility = View.VISIBLE
            }else{
                binding.emptyStateLayoutId.visibility = View.GONE
            }
        })




    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
    }


    override fun onResume() {
        super.onResume()
        if(viewModel.autoItemState){
            if(viewModel.tabSelected){
                Log.i(TAG, "tabSelected: ")
                viewModel.setSourceItemValue(LanguageItem(getString(R.string.default_src_lang_name),"English", "English", "United States","United States", null, "US", "en-Us", "en"))
                binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.text = viewModel.displaySource.value
            }


        }else{
            binding.fragmentVoiceBottomSheetLayoutId.bottomSheetFirstPersonLangId.text = viewModel.displaySource.value
        }



        binding.invalidateAll()

        InternetUtils.get(context).observe(viewLifecycleOwner, {
            if(it == true){
               if( voiceListAdapter.data.count() == 0 && viewModel.voiceItemList.isNotEmpty()){
                   voiceListAdapter.data.addAll(viewModel.voiceItemList)
                   voiceListAdapter.notifyDataSetChanged()
                   binding.clearListButtonId.visibility = View.VISIBLE

               }
                Log.i(TAG, "Internet Connected")
            }else{

                Log.i(TAG, "Internet Connected")
            }
        })



    }

    /*stop source speaking voice*/
    private fun stopSpeakingVoice(){
        if(textToSpeechEngine.isSpeaking){
            textToSpeechEngine.stop()
        }
    }

    private fun resetSoundPlayingCode(Id: Boolean) {
        if (Id) {
            // play person 2 sound
            val locale = Locale.Builder().setLanguage(viewModel.getTargetLangIso3Code()).build()
            textToSpeechEngine.language = locale
        } else {
            // play person 1 sound
            val locale = Locale.Builder().setLanguage(viewModel.getSourceLangIso3Code()).build()
            textToSpeechEngine.language = locale
        }
    }

    private fun playingSound(text: String, playId: Boolean) {
        resetSoundPlayingCode(playId)
        if(playId){
            //check person 2 language
            if (textToSpeechEngine.isLanguageAvailable(
                    Locale(
                        viewModel.getTargetLangIso3Code().toString()
                    )
                ) == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_LONG)
                    .show()
            } else {
                Log.i(TAG, "targetSpeakIcon: " + textToSpeechEngine.language.toString())
                textToSpeechEngine.speak(text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "tts1"
                )
            }
        }else{
            // check person 1 language
            if (textToSpeechEngine.isLanguageAvailable(
                    Locale(
                        viewModel.getSourceLangIso3Code().toString()
                    )
                ) == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_LONG)
                    .show()
            } else {
                Log.i(TAG, "targetSpeakIcon: " + textToSpeechEngine.language.toString())
                textToSpeechEngine.speak(text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "tts1"
                )
            }
        }

    }


    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        viewModel.dataType = null
        stopSpeakingVoice()
        if(viewModel.autoItemState){
            viewModel.setSourceItemValue(
                LanguageItem(
                    getString(R.string.auto_detected),
                    "Auto Detected",
                    "Auto",
                    "Auto",
                    "Auto",
                    null,
                    "US",
                    "en_US",
                    "auto"
                )
            )

        }

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")


    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechEngine.shutdown()
        Log.i(TAG, "onDestroy: ")
    }

    fun addSubmitList(recent: List<LanguageItem>?, list: List<LanguageItem>?): List<DataItem>{
        langListAdapter.positionSize = recent?.size ?: 0
        val items = when (recent) {
            null -> list!!.map { DataItem.SleepNightItem(it)}
            else -> listOf(DataItem.Recent) + recent.map { DataItem.RecentNightItem(it) }+ listOf(DataItem.LanguageHeader)  + list!!.map { DataItem.SleepNightItem(it) }
        }
        return items
    }

    companion object {
        private const val TAG = "VoiceFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DictionaryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = VoiceFragment()
    }


}
