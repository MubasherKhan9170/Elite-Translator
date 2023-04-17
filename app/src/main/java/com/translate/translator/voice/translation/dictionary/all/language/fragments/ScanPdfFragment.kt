package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentScanPdfBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.InternetUtils
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [ScanPdfFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ScanPdfFragment : Fragment() {
    // instance variables declaration and initialization
    private lateinit var binding: FragmentScanPdfBinding
    private val model: TranslatorMainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scan_pdf,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.galleryCardId.setOnClickListener {
            it.findNavController().navigate(MainFragmentDirections.actionMainFragmentIdToGalleryFragment("scanScreen"))

        }

        binding.cameraCardId.setOnClickListener {
            this.findNavController()
                .navigate(MainFragmentDirections.actionMainFragmentIdToCameraFragmentId("scanScreen"))
        }

    }

    override fun onResume() {
        super.onResume()
        InternetUtils.get(context).observe(viewLifecycleOwner, {
            if(it == true){
                Log.i(TAG, "Internet Connected")
            }else{

                Log.i(TAG, "Internet Connected")
            }
        })
    }



    companion object {
        private const val TAG = "ScanPdfFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScanPdfFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ScanPdfFragment()
    }
}