package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentHomeBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.util.Language
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.withTranslation
import androidx.core.util.lruCache
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    /*Class Variables*/
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            context
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder().setLanguage(viewModel.getSourceLangIso3Code()).build()
                textToSpeechEngine.language = locale
                /*if(!viewModel.autoItemState){
                    textToSpeechEngine.language = locale
                }*/

                //Log.i(TAG, "Source $locale")
            }
        }
    }
    private val textToSpeechEngineOutput: TextToSpeech by lazy {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder().setLanguage(viewModel.getTargetLangIso3Code()).build()
                //  Log.i(TAG, "Target $locale")
                textToSpeechEngineOutput.language = locale
                /*Locale.getAvailableLocales().forEach {
                    Log.i("Speak",  "Available Languages"+ it.displayLanguage + " "+ it.displayCountry + " "+ it.language)
                }*/
                textToSpeechEngineOutput.language = locale
            }
        }
    }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                val intent = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                intent?.let {
                    val recognizedText = it[0]
                    //viewModel.setHomeText(recognizedText)
                    binding.sourceTextLayout.messageInput.setText(recognizedText)
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: ")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        // return inflater.inflate(R.layout.fragment_home, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")

        /*source language view on click listener*/
        binding.fragmentHomeLanguageSwitcherLayout.layoutSourceLangId.setOnClickListener {
            it.findNavController()
                .navigate(MainFragmentDirections.actionMainFragmentIdToLanguageSelectionFragmentId("source", "home"))
        }
        /*target language view on click listener*/
        binding.fragmentHomeLanguageSwitcherLayout.layoutTargetLangId.setOnClickListener {
            it.findNavController()
                .navigate(MainFragmentDirections.actionMainFragmentIdToLanguageSelectionFragmentId("target", "home"))
        }
        /*language switcher icon on click listener*/
        binding.fragmentHomeLanguageSwitcherLayout.layoutLangSwitcherIconId.setOnClickListener {
            it.rotation = 0F
            it.animate()
                .rotation(it.rotation + 180F)
                .start()

            val result = viewModel.swapAtHomeScreen(
                viewModel.argumentSource.value.toString(),
                viewModel.argumentTarget.value.toString()
            )
           /* val temp = textToSpeechEngine.language
            textToSpeechEngine.language =  Locale(
                viewModel.getTargetLangIso3Code().toString()
            )
            textToSpeechEngineOutput.language =  Locale(
                viewModel.getSourceLangIso3Code().toString()
            )*/
            Log.d(TAG, "onViewCreated: source " +textToSpeechEngine.language)
            Log.d(TAG, "onViewCreated: Target " +textToSpeechEngineOutput.language)
            if(result){
                // add the item in history table
               // viewModel.addToHistory()
            }

        }
        /*source camera view button on click listener*/
        binding.sourceTextLayout.cameraViewIconId.setOnClickListener {
            if(InternetUtils.get(context).value == true){
                this.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentIdToCameraFragmentId("homeScreen"))
            }else{

                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }

        }
        /*source microphone button icon on click listener */
        binding.sourceTextLayout.microphoneViewIconId.setOnClickListener {

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
        /*source clear text button icon on click listener*/
        binding.sourceTextLayout.clearTextIconId.setOnClickListener {
            binding.sourceTextLayout.clearTextIconId.visibility = View.GONE
            binding.sourceTextLayout.messageInput.text?.clear()
            binding.targetTextLayout.messageOutput.text = ""
            viewModel.setHomeTextNull()
            viewModel.setTranslatedHomeTextNull()
            binding.targetTextLayout.root.visibility = View.GONE
            viewModel.camDetect = false
            binding.sourceTextLayout.cameraViewIconId.visibility = View.VISIBLE
            binding.sourceTextLayout.microphoneViewIconId.visibility = View.VISIBLE

            stopSourceSpeakingVoice()
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

            binding.sourceTextLayout.messageInput.isCursorVisible = false

            if(InternetUtils.get(context).value == true){
                if (text.isNotEmpty()) {
                    binding.sourceTextLayout.clearTextIconId.isClickable = false

                    binding.sourceTextLayout.translationButtonId.setTextColor(
                        resources.getColor(
                            android.R.color.transparent,
                            null
                        )
                    )
                    binding.sourceTextLayout.translationButtonId.setIconTintResource(android.R.color.transparent)
                    binding.sourceTextLayout.progressBar.visibility = View.VISIBLE


                    if(viewModel.autoItemState){
                        Log.e(TAG, "onViewCreated: pass")
                        viewModel.languageIdentifier.identifyLanguage(text)
                            .addOnSuccessListener { languageCode ->

                                if (languageCode != "und"){

                                    Log.d(TAG, "language identifier: "+ Language(languageCode))
                                    viewModel.langCode = Language(languageCode).code
                                    binding.sourceTextLayout.textView14.text = Language(languageCode).displayName
                                    binding.sourceTextLayout.textView14.visibility = View.VISIBLE
                                    binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)

                                     viewModel.doTranslation(text)

                                    viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                                        .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                                            Log.i(TAG, "Work State: " + workInfo?.state)
                                            if (workInfo?.state == WorkInfo.State.SUCCEEDED) {

                                                viewModel.setTranslatedHomeText(workInfo.outputData.getString("translatedText"))
                                                // insert the translation item in history table of database
                                                viewModel.addToHistory()

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
                        Log.e(TAG, "onViewCreated: fail")
                        viewModel.doTranslation(text)

                        viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                            .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                                Log.i(TAG, "Work State: " + workInfo?.state)
                                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {

                                    viewModel.setTranslatedHomeText(workInfo.outputData.getString("translatedText"))
                                    // insert the translation item in history table of database
                                    viewModel.addToHistory()

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


                } else {
                    Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
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
                //viewModel.setHomeText(binding.sourceTextLayout.messageInput.text.toString().trim())
                if(binding.sourceTextLayout.messageInput.text.isNullOrEmpty()){
                    binding.targetTextLayout.root.visibility = View.GONE
                    binding.sourceTextLayout.clearTextIconId.visibility = View.GONE
                    binding.sourceTextLayout.translationButtonId.isClickable = false
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
        /*target speaker button icon on click listener*/

        binding.targetTextLayout.speakerViewId.setOnClickListener {

            val text = binding.targetTextLayout.messageOutput.text.toString().trim()

            if(InternetUtils.get(context).value == true){
                if (text.isNotEmpty()) {
                    if (textToSpeechEngineOutput.isLanguageAvailable(
                            Locale(
                                viewModel.getTargetLangIso3Code().toString()
                            )
                        ) == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Toast.makeText(context, getString(R.string.language_not_supported), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.i(TAG, "targetSpeakIcon: " + textToSpeechEngineOutput.language.toString())
                        stopSourceSpeakingVoice()
                        textToSpeechEngineOutput.language = Locale(viewModel.getTargetLangIso3Code().toString())
                        textToSpeechEngineOutput.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts2")
                    }

                } else {
                    Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }
        }
        /*target text copied button icon on click listener*/
        binding.targetTextLayout.copiedViewId.setOnClickListener {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            if (!viewModel.translatedHomeText.value.isNullOrEmpty()) {
                val clip = ClipData.newPlainText(
                    "Copied Text",
                    viewModel.translatedHomeText.value.toString()
                )
                clipboard.setPrimaryClip(clip)
                //Snackbar.make(binding.root.rootView, getString(R.string.text_copy_label), Snackbar.LENGTH_SHORT).show()

                val toast = Toast.makeText(requireContext(), getString(R.string.text_copy_label), Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()

            }

        }
        /*target expand view button icon on click listener*/
        binding.targetTextLayout.expandViewId.setOnClickListener {
            val text = viewModel.homeText.value
            if (!text.isNullOrEmpty()) {
                it.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentIdToTranslationFragment())
            } else {
                Toast.makeText(context, getString(R.string.home_editor_empty_state), Toast.LENGTH_SHORT).show()
            }

        }

        /*target expand view button icon on click listener*/
        binding.targetTextLayout.shareViewId.setOnClickListener {
                /*val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, viewModel.translatedHomeText.value.toString())
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)*/

            var sendIntent: Intent? = null
            var filePath: Uri?
            var item = -1

            if (!binding.sourceTextLayout.messageInput.text.isNullOrEmpty() && !binding.targetTextLayout.messageOutput.text.isNullOrEmpty()) {
                val singleItems = arrayOf(getString(R.string.pdf_generate_option_one), getString(R.string.pdf_generate_option_two))
                val checkedItem = -1

                MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(getString(R.string.pdf_generate_share_title))
                    .setNeutralButton(getString(R.string.pdf_generate_cancel_button)) { dialog, _ ->
                        // Respond to neutral button press
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.pdf_generate_ok_button)) { dialog, _ ->
                        // Respond to positive button press
                        when (item) {
                            0 -> {
                                val shareIntent = Intent.createChooser(sendIntent, getString(R.string.pdf_generate_share_title))
                                startActivity(shareIntent)
                                dialog.dismiss()
                            }
                            1 -> {
                                val mText = binding.sourceTextLayout.messageInput.text.toString() +" \n "+ binding.targetTextLayout.messageOutput.text.toString()
                                filePath = generatePDF("File", mText)
                                if (filePath != null) {
                                    sendIntent?.setDataAndType(filePath, "application/pdf")
                                    sendIntent?.putExtra(Intent.EXTRA_STREAM, filePath)
                                    val shareIntent = Intent.createChooser(sendIntent, getString(R.string.pdf_generate_share_title))
                                    startActivity(shareIntent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.not_find_path),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }


                                dialog.dismiss()
                            }
                            else -> {
                                dialog.dismiss()
                            }
                        }


                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(singleItems, checkedItem) { _, i ->
                        Log.i(TAG, "item $i")
                        when (i) {

                            0 -> {
                                val mText = binding.sourceTextLayout.messageInput.text.toString() +" \n "+ binding.targetTextLayout.messageOutput.text.toString()
                                sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, mText)
                                    type = "text/plain"
                                }

                                item = 0
                            }
                            1 -> {

                                sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    type = "application/pdf"
                                }
                                item = 1
                            }
                        }


                    }
                    .setCancelable(false)
                    .show()


            } else {
                Toast.makeText(context, getString(R.string.no_text_found), Toast.LENGTH_LONG).show()
            }



        }




        /*view model home source text live data observer*/
        viewModel.homeText.observe(viewLifecycleOwner, {
            Log.i(TAG, "home text: ")
            if (!it.isNullOrEmpty()) {
                binding.sourceTextLayout.messageInput.setText(it)
                if(viewModel.camDetect){
                    binding.sourceTextLayout.cameraViewIconId.visibility = View.INVISIBLE
                    binding.sourceTextLayout.microphoneViewIconId.visibility = View.INVISIBLE
                }else{
                    binding.sourceTextLayout.cameraViewIconId.visibility = View.VISIBLE
                    binding.sourceTextLayout.microphoneViewIconId.visibility = View.VISIBLE
                }
            }

        })
        /*view model source language live data observer*/
        viewModel.argumentSource.observe(viewLifecycleOwner, {
            binding.fragmentHomeLanguageSwitcherLayout.layoutSourceLangId.text = it
            val locale = Locale.Builder().setLanguage(viewModel.getSourceLangIso3Code()).build()
            textToSpeechEngine.language = locale
            /*if(!viewModel.autoItemState){
                textToSpeechEngine.language = locale
            }*/
        })

        /*view model target language live data observer*/

        viewModel.argumentTarget.observe(viewLifecycleOwner, {
            binding.fragmentHomeLanguageSwitcherLayout.layoutTargetLangId.text = it
            binding.sourceTextLayout.messageInput.setText(viewModel.homeText.value.toString())

            val locale = Locale.Builder().setLanguage(viewModel.getTargetLangIso3Code()).build()
            textToSpeechEngineOutput.language = locale
            val text = binding.sourceTextLayout.messageInput.text.toString().trim()

            if (text.isNotEmpty() && viewModel.dataType == "target") {
                viewModel.dataType = null
                binding.sourceTextLayout.translationButtonId.setTextColor(
                    resources.getColor(
                        android.R.color.transparent,
                        null
                    )
                )
                binding.sourceTextLayout.translationButtonId.setIconTintResource(android.R.color.transparent)
                binding.sourceTextLayout.progressBar.visibility = View.VISIBLE

                viewModel.doTranslation(text)
                viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                    .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                        Log.i(TAG, "Work State: " + workInfo?.state)
                        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                            viewModel.setTranslatedHomeText(workInfo.outputData.getString("translatedText"))
                            // insert the translation item in history table of database
                            viewModel.addToHistory()
                            // Do something with progress information
                            binding.sourceTextLayout.progressBar.visibility = View.GONE
                            binding.sourceTextLayout.translationButtonId.setTextColor(
                                resources.getColor(
                                    R.color.primaryTextColor,
                                    null
                                )
                            )
                            binding.sourceTextLayout.translationButtonId.setIconTintResource(R.color.primaryTextColor)
                        }
                        if (workInfo?.state == WorkInfo.State.FAILED) {
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
        })
        /*view model home target text live data*/
        viewModel.translatedHomeText.observe(viewLifecycleOwner, {
            Log.i(TAG, "text $it")
            if (!it.isNullOrEmpty()) {
                Log.i(TAG, "called: ")
                if(viewModel.autoItemState && viewModel.langCode != "und"){
                    binding.sourceTextLayout.textView14.text = Language(viewModel.langCode).displayName
                    binding.sourceTextLayout.textView14.visibility = View.VISIBLE
                    binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
                }
                binding.targetTextLayout.messageOutput.text = it
                viewModel.setHomeText(binding.sourceTextLayout.messageInput.text.toString())
                //binding.sourceTextLayout.messageInput.setText(viewModel.homeText.value)
                binding.targetTextLayout.root.visibility = View.VISIBLE
                binding.sourceTextLayout.clearTextIconId.isClickable = true

            }
        })


    }

    @SuppressLint("SimpleDateFormat")
    private fun generatePDF(fileName: String, text: String): Uri {


        // creating an object variable
        // for our PDF document.
        val pdfDocument = PdfDocument()

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        //  val paint = Paint()
        val title = TextPaint()

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        Log.i(TAG, "generatePDF: length" + text.length)

        val pages : Float = text.length.toFloat()/2250F
        Log.i(TAG, "generatePDF: $pages")

        val myPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

        // below line is used for setting
        // start page for our PDF file.
        val myPage = pdfDocument.startPage(myPageInfo)

        // creating a variable for canvas
        // from our page of PDF.
        val canvas: Canvas = myPage.canvas


        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.textSize = 16F

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.color = ContextCompat.getColor(context!!, R.color.primaryTextColor)

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
        // canvas.drawText(text, 20F, 100F, title)
        if(text.length > 2250.minus(17)){
            canvas.drawMultilineText(text,title, 545,20F, 100F, 0 , 2250.minus(17))
        }else{
            canvas.drawMultilineText(text,title, 545,20F, 100F, 0 , text.length)
        }

        //  canvas.drawText("Elite Translator", 209F, 80F, title)


        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage)

        if(pages > 1.0){
            val myPageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()

            // below line is used for setting
            // start page for our PDF file.
            val myPage = pdfDocument.startPage(myPageInfo)

            // creating a variable for canvas
            // from our page of PDF.
            val canvas: Canvas = myPage.canvas


            // below line is used for adding typeface for
            // our text which we will be adding in our PDF file.
            title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // below line is used for setting text size
            // which we will be displaying in our PDF file.
            title.textSize = 16F

            // below line is sued for setting color
            // of our text inside our PDF file.
            title.color = ContextCompat.getColor(context!!, R.color.primaryTextColor)

            // below line is used to draw text in our PDF file.
            // the first parameter is our text, second parameter
            // is position from start, third parameter is position from top
            // and then we are passing our variable of paint which is title.
            // canvas.drawText(text, 20F, 100F, title)
            canvas.drawMultilineText(text,title, 545,20F, 100F, 2250.minus(17), text.length)
            //  canvas.drawText("Elite Translator", 209F, 80F, title)


            // after adding all attributes to our
            // PDF file we will be finishing our page.
            pdfDocument.finishPage(myPage)
        }


        // below line is used to set the name of
        // our PDF file and its path.
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val t = date.format(calendar.time)

        val file = File(
            context!!.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${fileName + "_" + t.toString()}.pdf"
        )
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(FileOutputStream(file))

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(
                context,
                getString(R.string.pdf_generated) + file.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            // below line is used
            // to handle error
            e.printStackTrace()
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context!!, "com.translate.translator.voice.translation.dictionary.all.language", file)
        } else {
            return Uri.fromFile(file)
        }

        //  return Uri.fromFile(file)
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
        binding.targetTextLayout.speakerViewId.setImageResource(R.drawable.ic_voice_white_icon)
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
        binding.targetTextLayout.speakerViewId.setImageResource(R.drawable.ic_voice_white_gray_icon)
    }


    private fun autoDetectStateActive(){
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_gray_icon)
        binding.fragmentHomeLanguageSwitcherLayout.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_gray_light)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_gray_icon)
        binding.fragmentHomeLanguageSwitcherLayout.layoutLangSwitcherIconId.isClickable = false

    }

    private fun autoDetectStateInActive(){
        binding.sourceTextLayout.microphoneViewIconId.setImageResource(R.drawable.ic_voice_black_icon)
        binding.fragmentHomeLanguageSwitcherLayout.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_black_yellow)
        binding.sourceTextLayout.speakerIconId.setImageResource(R.drawable.ic_voice_listen_black_icon)
        binding.fragmentHomeLanguageSwitcherLayout.layoutLangSwitcherIconId.isClickable = true
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

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")

        val defaultSource = viewModel.instancePreference.getLanguage(TranslatorMainViewModel.SOURCE_LANGUAGE)
        val defaultTarget = viewModel.instancePreference.getLanguage(TranslatorMainViewModel.TARGET_LANGUAGE)
        Log.i(TAG, "default: "+ defaultSource)
        val languageCodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)
        val languageDisplayString = resources.getStringArray(R.array.lang_array_display_name)

        if(defaultSource != null){
            viewModel.setSourceItemValue(defaultSource)
            viewModel.autoItemState = defaultSource.langNameEN == "Auto Detected"
            if(!viewModel.autoItemState){

                // change the display name of source language according to App language
                if(languageCodeString.contains(viewModel.getSourceLangBcp47Code())){
                    viewModel._displaySrcLanguage.value = languageDisplayString[languageCodeString.indexOf(viewModel.getSourceLangBcp47Code())]
                }
            }else{
                viewModel._displaySrcLanguage.value = getString(R.string.auto_detected)
            }
        }else{
            viewModel._displaySrcLanguage.value = getString(R.string.default_src_lang_name)
        }

        if(defaultTarget != null){
            // change the display name of source language according to App language
            if(languageCodeString.contains(viewModel.getTargetLangBcp47Code())){
                viewModel._displayTarLanguage.value = languageDisplayString[languageCodeString.indexOf(viewModel.getTargetLangBcp47Code())]
            }
        }else{
            viewModel._displayTarLanguage.value = getString(R.string.default_tar_lang_name)
        }


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


                //  Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()

            }
        })
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

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        viewModel.dataType = null
        textToSpeechEngine.stop()
        textToSpeechEngineOutput.stop()
        view?.let { hideKeyboard(it) }

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeechEngine.shutdown()
        textToSpeechEngineOutput.shutdown()
        Log.i(TAG, "onDestroy: ")
        
    }

    companion object {
        private const val TAG = "HomeFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = HomeFragment()

    }



    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE,
        justificationMode: Int = Layout.JUSTIFICATION_MODE_NONE) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                "$maxLines-$breakStrategy-$hyphenationFrequency-$justificationMode"

        val staticLayout = StaticLayoutCache[cacheKey] ?:
        StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .setJustificationMode(justificationMode)
            .build().apply { StaticLayoutCache[cacheKey] = this }

        staticLayout.draw(this, x, y)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                "$maxLines-$breakStrategy-$hyphenationFrequency"

        val staticLayout = StaticLayoutCache[cacheKey] ?:
        StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .build().apply { StaticLayoutCache[cacheKey] = this }

        staticLayout.draw(this, x, y)
    }

    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize"

        // The public constructor was deprecated in API level 28,
        // but the builder is only available from API level 23 onwards
        val staticLayout = StaticLayoutCache[cacheKey] ?:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, start, end, textPaint, width)
                .setAlignment(alignment)
                .setLineSpacing(spacingAdd, spacingMult)
                .setIncludePad(includePad)
                .setEllipsizedWidth(ellipsizedWidth)
                .setEllipsize(ellipsize)
                .build()
        } else {
            StaticLayout(text, start, end, textPaint, width, alignment,
                spacingMult, spacingAdd, includePad, ellipsize, ellipsizedWidth)
                .apply { StaticLayoutCache[cacheKey] = this }
        }

        staticLayout.draw(this, x, y)
    }

    private fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
        canvas.withTranslation(x, y) {
            draw(this)
        }
    }

    private object StaticLayoutCache {

        private const val MAX_SIZE = 50 // Arbitrary max number of cached items
        private val cache = lruCache<String, StaticLayout>(MAX_SIZE)

        operator fun set(key: String, staticLayout: StaticLayout) {
            cache.put(key, staticLayout)
        }

        operator fun get(key: String): StaticLayout? {
            return cache[key]
        }
    }



}