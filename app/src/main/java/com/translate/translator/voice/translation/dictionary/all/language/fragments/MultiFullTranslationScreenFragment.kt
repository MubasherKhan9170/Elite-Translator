package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel
import com.google.android.material.snackbar.Snackbar
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentMultiFullTranslationScreenBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MultiFullTranslationScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MultiFullTranslationScreenFragment : Fragment() {
    private lateinit var binding: FragmentMultiFullTranslationScreenBinding
    private val viewModel: TranslatorMultiViewModel by activityViewModels()
    private lateinit var  toolbar: Toolbar
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_multi_full_translation_screen,
            container,
            false
        )
        // return inflater.inflate(R.layout.fragment_translation, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById<Toolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.title =""

        binding.textCopiedButtonId.setOnClickListener {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            if(!viewModel.translatedHomeText.value.isNullOrEmpty()){
                val clip = ClipData.newPlainText("Copied Text", binding.textView.text)
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root.rootView,"Copied Text", Snackbar.LENGTH_SHORT)
                    .show()

            }

        }


        toolbar.setNavigationOnClickListener {
            it.findNavController().popBackStack()

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MultiTranslationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MultiFullTranslationScreenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}