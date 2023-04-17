package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.content.ActivityNotFoundException

import android.content.Intent
import android.net.Uri
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.DialogFragmentAppRateBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppRateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AppRateFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: DialogFragmentAppRateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
/*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    AlertDialog.Builder(requireContext(), R.layout.dialog_fragment_app_rate)
        .create()*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_app_rate, container, false)
        // return inflater.inflate(R.layout.fragment_gallery, container, false)
        //binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        binding.closeDialogId.setOnClickListener {
            dialog?.dismiss()
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, _, byUser ->

            if(byUser){
                if(ratingBar.rating > 3.0F){
                    dialog?.dismiss()
                    launchMarket()
                }
                else{
                    dialog?.dismiss()
                    Toast.makeText(context, getString(R.string.thanks_msg_rating), Toast.LENGTH_LONG).show()
                }
            }


        }
    }


    private fun launchMarket() {
        val uri: Uri = Uri.parse("market://details?id=" + requireActivity().packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, getString(R.string.not_find_app), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "AppRateDialog"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppRateFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppRateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}