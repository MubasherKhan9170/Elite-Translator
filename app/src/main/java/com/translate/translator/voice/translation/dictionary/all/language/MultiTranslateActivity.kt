package com.translate.translator.voice.translation.dictionary.all.language

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.translate.translator.voice.translation.dictionary.all.language.databinding.ActivityMultiTranslateBinding
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultiTranslateActivity : BaseActivity() {
    private lateinit var binding: ActivityMultiTranslateBinding
    val viewModel: TranslatorMultiViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multi_translate)
        //setContentView(R.layout.activity_multi_translate)
        binding.lifecycleOwner = this
    }

    companion object {
        private const val TAG = "MultiTranslateActivity"
    }

}