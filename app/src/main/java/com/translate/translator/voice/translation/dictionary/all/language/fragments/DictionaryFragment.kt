package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentDictionaryBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import android.text.Editable





/**
 * A simple [Fragment] subclass.
 * Use the [DictionaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class DictionaryFragment : Fragment() {
    // instance variables declaration and initialization
    private lateinit var binding: FragmentDictionaryBinding
    private val model: TranslatorMainViewModel by activityViewModels()

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
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_dictionary,
            container,
            false
        )
        binding.status = true
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")

        binding.fragmentDictionarySearchLayoutId.searchButtonId.setOnClickListener {
            hideKeyboard(it)
            val word: Editable? = binding.fragmentDictionarySearchLayoutId.searchFieldId.text
            if(InternetUtils.get(context).value == true ){
                if(!word.isNullOrBlank()){
                    binding.animationView.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.wordOutput.text = null
                    val arr = word.trim().toString().split(" ").toTypedArray()
                    model.searchWordDefinition(arr[0])
                }
                else{
                    Toast.makeText(context, getString(R.string.search_field_empty), Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(context, getString(R.string.feature_unavailable_offline), Toast.LENGTH_SHORT).show()
            }

        }

        model.wordResponse.observe(viewLifecycleOwner, { it ->
            if(it != null){
                val spannableString = SpannableStringBuilder()
                val wordSpannable = SpannableString(it.word?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.ENGLISH
                    ) else it.toString()
                })
                wordSpannable.setSpan(ForegroundColorSpan(Color.parseColor("#ffd301")), 0, wordSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                wordSpannable.setSpan(RelativeSizeSpan(1.5f), 0, wordSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                wordSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, wordSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.append(wordSpannable)
                spannableString
                    .appendLine()
                    .appendLine()
                if(it.phonetic != null){
                    spannableString.append(it.phonetic.toString())
                        .appendLine()
                        .appendLine()
                }


                val titleSpannable = SpannableString("DEFINITIONS")
                // titleSpannable.setSpan(foregroundSpan, 0, titleSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                titleSpannable.setSpan(RelativeSizeSpan(1.0f), 0, titleSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                titleSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, titleSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.append(titleSpannable)
                it.meanings?.forEach {
                    spannableString.appendLine()
                        .appendLine()
                    if(it.partOfSpeech != null){
                        spannableString.append(it.partOfSpeech)
                    }

                    it.definitions?.forEach {
                        val definitionSpannable = SpannableString(it.definition)
                        definitionSpannable.setSpan(ForegroundColorSpan(Color.parseColor("#ffd301")), 0, definitionSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        definitionSpannable.setSpan(RelativeSizeSpan(1.0f), 0, definitionSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        definitionSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, definitionSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        spannableString.appendLine()
                            .appendLine()
                            .append(definitionSpannable)
                            .appendLine()
                            .appendLine()
                        if(!it.example.isNullOrEmpty()){
                            val exampleSpan = SpannableString(it.example)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                exampleSpan.setSpan(BulletSpan(20, ForegroundColorSpan(Color.parseColor("#ffffff")).foregroundColor, 10), 0, exampleSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            } else{
                                exampleSpan.setSpan(BulletSpan(20, Color.WHITE), 0, exampleSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            spannableString.append(exampleSpan)
                        }
                        Log.i(TAG, "onViewCreated: "+ it.synonyms?.size)

                        if(!it.synonyms.isNullOrEmpty()){
                            spannableString.appendLine()
                                .appendLine()
                            val synSpannable = SpannableString("synonyms: ")
                            synSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, synSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            spannableString.append(synSpannable)
                            for (synonym in it.synonyms!!) {
                                spannableString.append(synonym.toString())
                                if(synonym.toString() != it.synonyms!!.get(it.synonyms!!.lastIndex)){
                                    spannableString.append(", ")
                                }

                            }

                        }
                        if(!it.antonyms.isNullOrEmpty()){
                            spannableString.appendLine()
                                .appendLine()
                            val antSpannable = SpannableString("antonyms: ")
                            antSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, antSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            spannableString.append(antSpannable)
                            for (antonym in it.antonyms!!) {
                                spannableString.append(antonym.toString())
                                if(antonym.toString() != it.antonyms!!.get(it.antonyms!!.lastIndex)){
                                    spannableString.append(", ")
                                }
                            }
                        }
                    }

                }
                binding.wordOutput.setText(spannableString, TextView.BufferType.SPANNABLE)
                binding.progressBar.visibility = View.GONE
                binding.resultTextId.visibility = View.VISIBLE
                binding.clearTextIconId.visibility = View.VISIBLE
                binding.animationView.visibility = View.GONE
            }
            else{
                binding.progressBar.visibility = View.GONE
                binding.animationView.visibility = View.VISIBLE
                    Toast.makeText(context, getString(R.string.unable_fetch_response), Toast.LENGTH_SHORT).show()
            }

            binding.invalidateAll()
        })


        binding.clearTextIconId.setOnClickListener{
            binding.wordOutput.text = null
            binding.fragmentDictionarySearchLayoutId.searchFieldId.text.clear()
            binding.resultTextId.visibility = View.GONE
            binding.clearTextIconId.visibility = View.GONE
            binding.animationView.visibility = View.VISIBLE
        }


        binding.fragmentDictionarySearchLayoutId.searchFieldId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //  TODO("Not yet implemented")
                // Log.i(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //  TODO("Not yet implemented")
                 Log.i(TAG, "onTextChanged: ")
                if(p0?.length == 1 && p0.last() == ' '){
                    binding.fragmentDictionarySearchLayoutId.searchFieldId.text.clear()
                }
                if(p0?.length!! > 1 && p0.last() == ' '){
                    binding.fragmentDictionarySearchLayoutId.searchFieldId.setText(p0.subSequence(0, p0.length - 1))
                    binding.fragmentDictionarySearchLayoutId.searchFieldId.setSelection(p0.length-1)
                }


            }


            override fun afterTextChanged(p0: Editable?) {
                //  TODO("Not yet implemented")
                Log.i(TAG, "afterTextChanged: ")

                //viewModel.setHomeText(binding.sourceTextLayout.messageInput.text.toString().trim())

            }

        })


    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
        InternetUtils.get(context).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it == true){
                Log.i(TAG, "Internet Connected")
            }else{
                  Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        binding.fragmentDictionarySearchLayoutId.searchFieldId.text.clear()

        view?.let { hideKeyboard(it) }

    }


    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        imm.hideSoftInputFromWindow(activity!!.currentFocus?.windowToken, 0);
    }

    companion object {
        private const val TAG = "DictionaryFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DictionaryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = DictionaryFragment()

    }
}