package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentScreenTranslationBinding
import com.translate.translator.voice.translation.dictionary.all.language.services.ScreenCaptureService
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"




/**
 * A simple [Fragment] subclass.
 * Use the [ScreenTranslationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScreenTranslationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mpManager: MediaProjectionManager? = null
    private lateinit var binding: FragmentScreenTranslationBinding
    private val viewModel: TranslatorMainViewModel by activityViewModels()

    private val startForResultForPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result code: "+ result.resultCode)
            if (result.resultCode == 0) {
                if(Settings.canDrawOverlays(context)){
                    // start back
                    Log.d(TAG, "Granted: ")
                    startProjection()
                }


            }
        }

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
    ): View? {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_screen_translation,
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

        val screenLanguageEnString = resources.getStringArray(R.array.camera_lang_array_name_en)

        Log.d(TAG, "onViewCreated: "+ screenLanguageEnString.indexOf(viewModel.argumentSource.value.toString()))

       if(!viewModel.autoItemState){
           if(screenLanguageEnString.indexOf(viewModel.argumentSource.value.toString()) == -1){
               viewModel.setSourceItemValue(LanguageItem(getString(R.string.default_src_lang_name),"English", "English", "United States","United States", null, "US", "en-Us", "en"))
           }
       }



        /*source language view on click listener*/
        binding.fragmentScreenLanguageSwitcherLayout.layoutSourceLangId.setOnClickListener {
            it.findNavController()
                .navigate(
                    MainFragmentDirections.actionMainFragmentIdToLanguageSelectionFragmentId(
                        "source",
                        "screen"
                    )
                )

        }
        /*target language view on click listener*/
        binding.fragmentScreenLanguageSwitcherLayout.layoutTargetLangId.setOnClickListener {
            it.findNavController()
                .navigate(
                    MainFragmentDirections.actionMainFragmentIdToLanguageSelectionFragmentId(
                        "target",
                        "screen"
                    )
                )
        }


        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.


        binding.buttonId.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity?.packageName)
                )
                startForResultForPermission.launch(intent)
            } else {
                //requireActivity().startService(Intent(context, FloatingViewService::class.java))
                //requireActivity().finish()
                Log.d(TAG, "Button: Clicked")
                startProjection()

            }
        }

        viewModel.argumentSource.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "source change:  $it")
            binding.fragmentScreenLanguageSwitcherLayout.layoutSourceLangId.text = it
            if(viewModel.autoItemState){
                autoDetectStateActive()
            }else{
                autoDetectStateInActive()
            }

        })

        viewModel.argumentTarget.observe(viewLifecycleOwner, Observer {
            binding.fragmentScreenLanguageSwitcherLayout.layoutTargetLangId.text = it
        })

        /*language switcher icon on click listener*/
        binding.fragmentScreenLanguageSwitcherLayout.layoutLangSwitcherIconId.setOnClickListener {
            val find = viewModel.swapAtCameraScreen(
                screenLanguageEnString,
                viewModel.argumentSource.value.toString(),
                viewModel.argumentTarget.value.toString()
            )
            if(!find){
                Toast.makeText(context,
                    getString(R.string.no_performed_part_a)+ " "+ viewModel.argumentTarget.value.toString() +" "+ getString(R.string.no_performed_part_b), Toast.LENGTH_LONG).show()
            }else{
                it.rotation = 0F
                it.animate()
                    .rotation(it.rotation + 180F)
                    .start()

                val tempDisplay = viewModel._displaySrcLanguage.value
                viewModel._displaySrcLanguage.value = viewModel._displayTarLanguage.value
                viewModel._displayTarLanguage.value = tempDisplay
            }
        }

    }

    override fun onResume() {
        super.onResume()
        binding.invalidateAll()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "Projection: requestCode "+ requestCode+ " resultCode "+ resultCode+ " data "+ data)
        if (requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                requireActivity().startService(ScreenCaptureService.getStartIntent(
                    requireContext(),
                    resultCode,
                    data
                )
                )
                requireActivity().finish()
            }

        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    private fun startProjection() {
        val mProjectionManager =
            requireActivity().getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        startActivityForResult(
            mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE
        )
       // startForResultForPermission.launch(mProjectionManager.createScreenCaptureIntent())
    }

    /*private fun stopProjection() {
        requireActivity().startService(ScreenCaptureService.getStopIntent(requireContext()))
    }*/


    private fun autoDetectStateActive(){
        binding.fragmentScreenLanguageSwitcherLayout.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_gray_light)
        binding.fragmentScreenLanguageSwitcherLayout.layoutLangSwitcherIconId.isClickable = false
    }

    private fun autoDetectStateInActive(){
        binding.fragmentScreenLanguageSwitcherLayout.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_black_yellow)
        binding.fragmentScreenLanguageSwitcherLayout.layoutLangSwitcherIconId.isClickable = true
    }



    companion object {
        private const val TAG = "ScreenTranslationFragme"
        private const val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
        private const val REQUEST_CODE = 100


        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ScreenTranslationFragment()
    }
}