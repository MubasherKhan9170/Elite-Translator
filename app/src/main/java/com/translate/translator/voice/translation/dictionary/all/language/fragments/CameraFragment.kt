package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.work.WorkInfo
import com.google.mlkit.vision.common.InputImage
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentCameraBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.*
import com.translate.translator.voice.translation.dictionary.all.language.view.CropImageView
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cropImageView: CropImageView
    private var flashMode: Boolean = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private lateinit var cameraSelector: CameraSelector
    private lateinit var preview: Preview
    private var cameraExecutor: ExecutorService? = null
    private lateinit var sharedPreference: UserPreferencesRepository
    private lateinit var dialog: Dialog


   // private var cameraModel: CameraViewModel? = null
    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private val args: CameraFragmentArgs by navArgs()

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Do something if permission granted
            if (isGranted) {
                Log.i(TAG, "Storage permission granted")
               /* NavHostFragment.findNavController(this)
                    .navigate(R.id.action_camera_fragment_id_to_gallery_Fragment)*/
                NavHostFragment.findNavController(this)
                    .navigate(CameraFragmentDirections.actionCameraFragmentIdToGalleryFragment("nothing"))

            } else {
                Log.i(TAG, "Storage permission denied")
                // NavHostFragment.findNavController(this).popBackStack()

            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Do something if permission granted
            if (isGranted) {
                Log.i(TAG, "permission granted")
                startCamera()
            } else {
                Log.i(TAG, "permission denied")
                NavHostFragment.findNavController(this).popBackStack()
            }
        }

    private val startForResultForCameraPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == 0) {
                Log.i(TAG, "Camera Ok Result: " + result.resultCode)
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        REQUIRED_PERMISSIONS[0]
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedPreference.save(CAMERA_FIRST_ASK, false)
                    sharedPreference.save(CAMERA_PERMANENT_DENIED, false)
                    startCamera()
                } else {
                    Log.i(TAG, "Camera permission not granted")
                    NavHostFragment.findNavController(this).popBackStack()
                }
            }
        }

    private val startForResultForStoragePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == 0) {
                Log.i(TAG, "Storage Ok Result: " + result.resultCode)
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        REQUIRED_PERMISSIONS[1]
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedPreference.save(STORAGE_FIRST_ASK, false)
                    sharedPreference.save(STORAGE_PERMANENT_DENIED, false)
                    Log.i(TAG, "Storage permission not granted")
                    // open the storage
                    /*NavHostFragment.findNavController(this)
                        .navigate(R.id.action_camera_fragment_id_to_gallery_Fragment)*/
                    NavHostFragment.findNavController(this)
                        .navigate(CameraFragmentDirections.actionCameraFragmentIdToGalleryFragment("nothing"))
                } else {
                    Log.i(TAG, "Storage permission not granted")
                    //NavHostFragment.findNavController(this).popBackStack()
                }
            }
        }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == 0) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
            }
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        // return inflater.inflate(R.layout.fragment_camera, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(args.moveTo != "nothing"){
            viewModel.jumpTo = args.moveTo
        }



        //cameraModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)

        dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        sharedPreference = UserPreferencesRepository.getInstance(requireContext())
        Log.i(TAG, "onViewCreated: ")

        val cameraFlashButton = binding.fragmentCameraCaptureImageLayoutId.cameraFlashButton

        val cameraLanguageEnString = resources.getStringArray(R.array.camera_lang_array_name_en)
        Log.d(TAG, "onViewCreated: "+ cameraLanguageEnString.indexOf(viewModel.argumentSource.value.toString()))

            if(cameraLanguageEnString.indexOf(viewModel.argumentSource.value.toString()) == -1){
                viewModel.setSourceItemValue(LanguageItem( getString(R.string.default_src_lang_name), "English", "English", "United States", "United States",null, "US", "en-Us", "en"))
            }


        /*source language view on click listener*/
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutSourceLangId.setOnClickListener {
            it.findNavController()
                .navigate(
                    CameraFragmentDirections.actionCameraFragmentIdToLanguageSelectionFragmentId(
                        "source",
                        "camera"
                    )
                )
        }
        /*target language view on click listener*/
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutTargetLangId.setOnClickListener {
            it.findNavController()
                .navigate(
                    CameraFragmentDirections.actionCameraFragmentIdToLanguageSelectionFragmentId(
                        "target",
                        "camera"
                    )
                )
        }
        /*language switcher icon on click listener*/
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutLangSwitcherIconId.setOnClickListener {

            val find = viewModel.swapAtCameraScreen(
                cameraLanguageEnString,
                viewModel.argumentSource.value.toString(),
                viewModel.argumentTarget.value.toString()
            )
            if(!find){
                Toast.makeText(context,
                    getString(R.string.no_performed_part_a)+" "+ viewModel.argumentTarget.value.toString() +" "+ getString(R.string.no_performed_part_b), Toast.LENGTH_LONG).show()
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

        /*End Result layout view
        view model source language live data observer*/
        viewModel.argumentSource.observe(viewLifecycleOwner, {
            binding.fragmentCameraLanguageSwitcherLayoutId.layoutSourceLangId.text = it

        })
        /*End Result layout view
          view model target language live data observer*/
        viewModel.argumentTarget.observe(viewLifecycleOwner, {
            binding.fragmentCameraLanguageSwitcherLayoutId.layoutTargetLangId.text = it

        })

        /*Capture Image Layout
        * Set up the on click Listener
        * DO Image capture
        * And show the Image Crop Overlay*/

        binding.fragmentCameraCaptureImageLayoutId.cameraCaptureButton.setOnClickListener {
            takePhoto(requireContext())
            binding.fragmentCameraViewDisplayId.isShowCropOverlay = true
        }

        /*Capture Image Layout
        * Set up the on click Listener
        * Check the permission acknowledgement
        * And open the gallery fragment*/

        binding.fragmentCameraCaptureImageLayoutId.cameraGalleryButton.setOnClickListener {
            checkStoragePermissionStatus()
        }

        /*Capture Image Layout
        * Set up the on click Listener
        * And active and inactive the flash light*/

        cameraFlashButton.setOnClickListener {

            flashMode = if (!flashMode) {
                camera?.cameraControl?.enableTorch(true)
                cameraFlashButton.setImageResource(R.drawable.ic_camera_flash_icon)

                true
            } else {
                camera?.cameraControl?.enableTorch(false)
                cameraFlashButton.setImageResource(R.drawable.ic_camera_flash_off_icon)
                false
            }

        }

        /*Scan Image Layout
        * Set up the on click Listener
        * Get the Crop Image
        * Hide the crop Image Overlay
        * Extract the source text from the Image using ML*/

        binding.fragmentCameraScanImageLayoutId.cameraScanButton.setOnClickListener {
            var cropImage = cropImageView.croppedImage
            if(viewModel.selectedImageUri.value != null){

                val decode = BitmapUtils.decodeSampledBitmapRegion(context, viewModel.selectedImageUri.value, cropImageView.cropRect, cropImageView.cropRect.width(), cropImageView.cropRect.height(), 1)

                //recognizeText(InputImage.fromBitmap(decode.bitmap, 0))
               //cropImageView.setImageBitmap(decode.bitmap, 0)
                cropImage = decode.bitmap!!
                cropImageView.resetCropRect()
            }






            val analyzer = TextAnalyzer(
                requireContext(),
                lifecycle,
                viewModel
            )
            Log.d(TAG, "onViewCreated: ${cropImage.width}")

            TextAnalyzer.recognizeText(analyzer, InputImage.fromBitmap(cropImage, 0))
                .addOnCompleteListener {
                    if(it.isSuccessful && it.result.text.isNotEmpty()){
                        if(viewModel.autoItemState){
                            viewModel.languageIdentifier.identifyLanguage(viewModel.getImageExtractedText()!!)
                                .addOnSuccessListener { languageCode ->
                                    Log.d(TAG, "language code: $languageCode")
                                    if (languageCode != "und"){
                                        viewModel.langCode = languageCode
                                        Log.d(TAG, "language identifier: "+ Language(languageCode))
                                    }else{
                                        Log.d(TAG, "language identifier: "+ Language(languageCode))
                                        viewModel.langCode = languageCode
                                    }
                                }
                        }
                        when (viewModel.jumpTo) {
                            "homeScreen" -> {
                                InternetUtils.get(context).observe(viewLifecycleOwner, { status ->
                                    if(status == true){
                                        binding.progressBar.visibility = View.VISIBLE
                                        viewModel.workManager.cancelAllWork()

                                            viewModel.doTranslation(viewModel.getImageExtractedText()!!)

                                            viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                                                .observe(viewLifecycleOwner, { workInfo: WorkInfo? ->
                                                    Log.i(TAG, "Work State: " + workInfo?.state)
                                                    if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                                                        binding.progressBar.visibility = View.GONE
                                                        viewModel.setImageExtractedTextTranslation(workInfo.outputData.getString("translatedText")!!)

                                                        // Do something with progress information
                                                        binding.fragmentCameraViewDisplayId.isShowCropOverlay = false
                                                        binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
                                                        //binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.GONE
                                                        binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.GONE
                                                        binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.VISIBLE
                                                        binding.fragmentCameraResultLayoutId.root.visibility = View.VISIBLE


                                                        // set the text into the edit field
                                                        binding.fragmentCameraResultLayoutId.srcText.setText(viewModel.getImageExtractedText())
                                                        binding.fragmentCameraResultLayoutId.translatedText.setText(viewModel.getImageExtractedTextTranslation())
                                                    }
                                                    if (workInfo?.state == WorkInfo.State.FAILED) {
                                                        binding.progressBar.visibility = View.GONE
                                                        // show dialog
                                                        Toast.makeText(context, getString(R.string.translation_failed), Toast.LENGTH_SHORT).show()
                                                    }
                                                })

                                    }else{

                                        Toast.makeText(context, getString(R.string.check_net_connectivity), Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                            "scanScreen" -> {
                                // Do something with progress information
                                binding.fragmentCameraViewDisplayId.isShowCropOverlay = false
                                binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
                                //binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.GONE
                                binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.GONE
                                binding.fragmentPdfFinalizeResultLayoutId.root.visibility = View.VISIBLE
                                binding.fragmentCameraResultLayoutId.root.visibility = View.VISIBLE


                                // set the text into the edit field
                                binding.fragmentCameraResultLayoutId.srcText.setText(viewModel.getImageExtractedText())
                                binding.fragmentCameraResultLayoutId.result.visibility = View.GONE

                            }
                            "multiScreen" ->{

                            }
                            else -> {
                                Log.i(TAG, "onViewCreated: nothing" )

                            }
                        }


                    }
                    else{
                        // TODO: show the dialog box
                        Log.i(TAG, "onViewCreated: " + it.exception)
                        Toast.makeText(context, getString(R.string.image_text_no_found), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        /*Scan Image Layout
        * Set up the on click Listener
        * Do the retake*/

        binding.fragmentCameraScanImageLayoutId.cameraRetakeButton.setOnClickListener {
            binding.fragmentCameraViewDisplayId.visibility = View.GONE
           // binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.GONE
           // binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE
            if (viewModel.selectedImageUri.value != null) {
                viewModel.setSelectedImageUri(null)
                NavHostFragment.findNavController(this).popBackStack()
                startCamera()
            } else {
                mCameraStarted()
            }


        }

        /*Finalize Result Layout
        * Set up the on click Listener
        * Do the retake
        * Set to empty of source and translated text*/

        binding.fragmentCameraFinalizeResultLayoutId.cameraRefreshButton.setOnClickListener {
            binding.fragmentCameraViewDisplayId.visibility = View.GONE
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
           // binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE
            //cameraModel!!.sourceText.value = ""
            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")

            if (viewModel.selectedImageUri.value != null) {
                viewModel.setSelectedImageUri(null)
                startCamera()
            } else {
                mCameraStarted()
            }
        }

        binding.fragmentPdfFinalizeResultLayoutId.againButton.setOnClickListener {
            binding.fragmentCameraViewDisplayId.visibility = View.GONE
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            // binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentPdfFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE
            //cameraModel!!.sourceText.value = ""
            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")

            if (viewModel.selectedImageUri.value != null) {
                viewModel.setSelectedImageUri(null)
                NavHostFragment.findNavController(this).popBackStack()
                startCamera()
            } else {
                mCameraStarted()
            }
        }




        binding.fragmentCameraResultLayoutId.nextButtonId.setOnClickListener {
            //insert into the database
            viewModel.addToHistory()

            viewModel.setHomeTextNull()
            viewModel.setTranslatedHomeTextNull()
            viewModel.setSelectedImageUri(null)
            viewModel.setHomeText(viewModel.getImageExtractedText())
            viewModel.setTranslatedHomeText(viewModel.getImageExtractedTextTranslation())

            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")
            binding.fragmentCameraResultLayoutId.srcText.text.clear()
            binding.fragmentCameraResultLayoutId.translatedText.text.clear()

            binding.fragmentCameraViewDisplayId.visibility = View.GONE
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE


            viewModel.camDetect = true
            //mCameraStarted()
            it.findNavController().navigate(R.id.action_camera_fragment_id_to_main_fragment_id)


        }



        binding.fragmentCameraFinalizeResultLayoutId.editButtonId.setOnClickListener {

            //insert into the database
            viewModel.addToHistory()

            viewModel.dataType = null

            viewModel.setHomeTextNull()
            viewModel.setTranslatedHomeTextNull()
            viewModel.setSelectedImageUri(null)
            viewModel.setHomeText(viewModel.getImageExtractedText())
            viewModel.setTranslatedHomeText(viewModel.getImageExtractedTextTranslation())

            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")
            binding.fragmentCameraResultLayoutId.srcText.text.clear()
            binding.fragmentCameraResultLayoutId.translatedText.text.clear()

            binding.fragmentCameraViewDisplayId.visibility = View.GONE
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE

            viewModel.camDetect = true
            //mCameraStarted()
            it.findNavController().navigate(R.id.action_camera_fragment_id_to_main_fragment_id)
        }

        binding.fragmentPdfFinalizeResultLayoutId.checkButton.setOnClickListener {
            viewModel.dataType = null

            viewModel.setHomeTextNull()
            viewModel.setTranslatedHomeTextNull()
            viewModel.setSelectedImageUri(null)
            viewModel.setHomeText(viewModel.getImageExtractedText())
            viewModel.setTranslatedHomeText(viewModel.getImageExtractedTextTranslation())

            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")
            binding.fragmentCameraResultLayoutId.srcText.text.clear()
            binding.fragmentCameraResultLayoutId.translatedText.text.clear()

            binding.fragmentCameraViewDisplayId.visibility = View.GONE
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.GONE
            binding.fragmentPdfFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraPreviewFinderId.visibility = View.VISIBLE
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.VISIBLE

            //change has been made and skip the pdfGeneratorFragment between camera to main fragment.

           // it.findNavController().navigate(CameraFragmentDirections.actionCameraFragmentIdToMainFragmentId("cam"))
            viewModel.camDetect = true
            val action =  CameraFragmentDirections.actionCameraFragmentIdToMainFragmentId()
            action.tab= "cam"// your value
            it.findNavController().navigate(action)

        }


        binding.fragmentCameraResultLayoutId.cancelResultButtonId.setOnClickListener {
            viewModel.setImageExtractedText("")
            viewModel.setImageExtractedTextTranslation("")
            binding.fragmentCameraResultLayoutId.srcText.text.clear()
            binding.fragmentCameraResultLayoutId.translatedText.text.clear()
            binding.fragmentCameraLanguageSwitcherLayoutId.root.visibility = View.VISIBLE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.VISIBLE
            cropImageView.isShowCropOverlay = true
            binding.fragmentCameraResultLayoutId.root.visibility = View.GONE
            binding.fragmentCameraFinalizeResultLayoutId.root.visibility = View.GONE
            binding.fragmentPdfFinalizeResultLayoutId.root.visibility = View.GONE
        }

        viewModel.selectedImageUri.observe(viewLifecycleOwner, {
            if(it != null){

                cropImageView = binding.fragmentCameraViewDisplayId
                binding.fragmentCameraPreviewFinderId.visibility = View.GONE
                binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.GONE
                binding.fragmentCameraViewDisplayId.visibility = View.VISIBLE
                binding.fragmentCameraScanImageLayoutId.root.visibility = View.VISIBLE


               // cropImageView.clearImage()


                if(Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        viewModel.selectedImageUri.value
                    )
                    when(viewModel.imageUriOrientation){
                        90 -> cropImageView.setImageBitmap(bitmap, 90)
                        180 -> cropImageView.setImageBitmap(bitmap, 180)
                        270 -> cropImageView.setImageBitmap(bitmap, 270)

                        else -> cropImageView.setImageBitmap(bitmap)
                    }

                } else {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, viewModel.selectedImageUri.value!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    when(viewModel.imageUriOrientation){
                        90 -> cropImageView.setImageBitmap(bitmap, 90)
                        180 -> cropImageView.setImageBitmap(bitmap, 180)
                        270 -> cropImageView.setImageBitmap(bitmap, 270)
                        else -> cropImageView.setImageBitmap(bitmap)
                    }
                }

                Log.i(TAG, "image angle: "+ cropImageView.rotatedDegrees)
                /*try {
                    image = InputImage.fromFilePath(context, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/

              //  recognizeText(InputImage.fromFilePath(context!!, viewModel.selectedImageUri.value!!))

            }else{
                Log.i(TAG, "URI: null")

            }

        })

    }


    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
        orientationEventListener.enable()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: " + viewModel.selectedImageUri.value.toString())
        // called function to check the status of camera permission
        checkAutoDetectState()

        if (viewModel.selectedImageUri.value == null) {
            Log.i(TAG, "onResume: checked permission")
            checkCameraPermissionStatus()
        } /*else {
            Log.i(TAG, "Not Null: ")

            cropImageView = binding.fragmentCameraViewDisplayId
            binding.fragmentCameraPreviewFinderId.visibility = View.GONE
            binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.GONE
            binding.fragmentCameraViewDisplayId.visibility = View.VISIBLE
            binding.fragmentCameraScanImageLayoutId.root.visibility = View.VISIBLE
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                viewModel.selectedImageUri.value
            )
            cropImageView.setImageBitmap(bitmap, 0)
            *//*Glide.with(cropImageView.mImageView)
                .load(viewModel.selectedImageUri.value)
                .into(cropImageView.mImageView)*//*
            //cropImageView.isShowCropOverlay = true
            //viewModel.setSelectedImageUri(null)
        }*/

        binding.invalidateAll()




    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        orientationEventListener.disable()
        mCameraStopped()
        viewModel.workManager.cancelAllWork()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: ")
        viewModel.setSelectedImageUri(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
        cameraExecutor?.shutdown()
       // viewModel.jumpTo = "nothing"

    }
    private fun checkAutoDetectState() {
        if (viewModel.autoItemState) {
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
            autoDetectStateActive()
        } else {
            autoDetectStateInActive()
        }
    }

    private fun autoDetectStateActive(){
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_gray_light)
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutLangSwitcherIconId.isClickable = false

    }

    private fun autoDetectStateInActive(){
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutLangSwitcherIconId.setImageResource(R.drawable.ic_language_switch_black_yellow)
        binding.fragmentCameraLanguageSwitcherLayoutId.layoutLangSwitcherIconId.isClickable = true
    }



    private fun checkCameraPermissionStatus() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                REQUIRED_PERMISSIONS[0]
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "Camera Permission Granted")

                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                sharedPreference.save(CAMERA_FIRST_ASK, false)
                sharedPreference.save(CAMERA_PERMANENT_DENIED, false)
                startCamera()
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0]) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Log.d(
                    TAG,
                    "onViewCreated: " + shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])
                )
                Log.d(TAG, "Just denied ")
                if (!sharedPreference.getValueBoolean(CAMERA_PERMANENT_DENIED, false)) {
                    sharedPreference.save(CAMERA_FIRST_ASK, true)
                    showInContextUI(
                        R.drawable.ic_camera_premission_icon,
                        getString(R.string.camera_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(
                            CAMERA_PERMANENT_DENIED, false
                        ),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[0])
                    )
                } else {
                    showInContextUI(
                        R.drawable.ic_camera_premission_icon,
                        getString(R.string.camera_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(
                            CAMERA_PERMANENT_DENIED, false
                        ),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[0])
                    )
                }


            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                Log.d(
                    TAG,
                    "first: " + shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])
                )
                if (!shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0]) && !sharedPreference.getValueBoolean(
                        CAMERA_FIRST_ASK,
                        false
                    )
                ) {
                    showInContextUI(
                        R.drawable.ic_camera_premission_icon,
                        getString(R.string.camera_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(
                            CAMERA_PERMANENT_DENIED, false
                        ),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[0])
                    )
                    sharedPreference.save(CAMERA_FIRST_ASK, true)
                } else {
                    sharedPreference.save(CAMERA_PERMANENT_DENIED, true)
                    showInContextUI(
                        R.drawable.ic_camera_premission_icon,
                        getString(R.string.camera_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(
                            CAMERA_PERMANENT_DENIED, false
                        ),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[0])
                    )
                    Log.d(TAG, "permanently denied ")

                }


            }

        }

    }

    private fun checkStoragePermissionStatus() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                REQUIRED_PERMISSIONS[1]
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "Storage Permission Granted")

                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                sharedPreference.save(STORAGE_FIRST_ASK, false)
                sharedPreference.save(STORAGE_PERMANENT_DENIED, false)
                // open the storage file
                NavHostFragment.findNavController(this)
                    .navigate(CameraFragmentDirections.actionCameraFragmentIdToGalleryFragment("nothing"))
            }
            shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[1]) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Log.d(
                    TAG,
                    "onViewCreated: Storage" + shouldShowRequestPermissionRationale(
                        REQUIRED_PERMISSIONS[1]
                    )
                )
                Log.d(TAG, "Just denied Storage")
                if (!sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false)) {
                    sharedPreference.save(STORAGE_FIRST_ASK, true)
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[1])
                    )
                } else {
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[1])
                    )
                }


            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                Log.d(
                    TAG,
                    "first: Storage " + shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[1]) + "First ASk: " + sharedPreference.getValueBoolean(
                        STORAGE_FIRST_ASK,
                        false
                    )
                )
                if (!shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[1]) && !sharedPreference.getValueBoolean(
                        STORAGE_FIRST_ASK,
                        false
                    )
                ) {
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[1])
                    )
                    sharedPreference.save(STORAGE_FIRST_ASK, true)
                } else {
                    sharedPreference.save(STORAGE_PERMANENT_DENIED, true)
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false),
                        REQUIRED_PERMISSIONS.indexOf(REQUIRED_PERMISSIONS[1])
                    )
                    Log.d(TAG, "Storage permanently denied ")

                }


            }

        }

    }

    @SuppressLint("UnsafeOptInUsageError", "ClickableViewAccessibility")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.fragmentCameraPreviewFinderId.surfaceProvider)
                }
            //val previewPanel = findViewById<PreviewView>(R.id.fragment_camera_preview_finder_id)
            val previewPanel = binding.fragmentCameraPreviewFinderId

// tap to focus listener
            previewPanel.setOnTouchListener(View.OnTouchListener setOnTouchListener@{ _: View, motionEvent: MotionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                    MotionEvent.ACTION_UP -> {
                        // Get the MeteringPointFactory from PreviewView
                        val factory = binding.fragmentCameraPreviewFinderId.meteringPointFactory

                        // Create a MeteringPoint from the tap coordinates
                        val point = factory.createPoint(motionEvent.x, motionEvent.y)

                        // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                        val action = FocusMeteringAction.Builder(point).build()

                        // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                        // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                        camera?.cameraControl?.startFocusAndMetering(action)

                        return@setOnTouchListener true
                    }
                    else -> return@setOnTouchListener false
                }
            })

            // Listen to pinch gestures
            val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // Get the camera's current zoom ratio
                    val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F

                    // Get the pinch gesture's scaling factor
                    val delta = detector.scaleFactor

                    // Update the camera's zoom ratio. This is an asynchronous operation that returns
                    // a ListenableFuture, allowing you to listen to when the operation completes.
                    camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)

                    // Return true, as the event was handled
                    return true
                }
            }
            if(context != null){
                val scaleGestureDetector = ScaleGestureDetector(requireContext(), listener)
                // Attach the pinch gesture listener to the viewfinder
                previewPanel.setOnTouchListener { _, event ->
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }
            }




            // Image Capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()


            // Image Analyzer
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Select back camera as a default
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                mCameraStopped()

                // Bind use cases to camera
                mCameraStarted()

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun mCameraStarted() {

        cameraProvider?.unbindAll()
        // Bind use cases to camera
        camera = cameraProvider?.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalyzer
        )
    }

    private fun mCameraStopped() {

        cameraProvider?.unbindAll()

    }

    private fun takePhoto(context: Context) {

        imageCapture?.takePicture(ContextCompat.getMainExecutor(context), object :
            ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val rotation = image.imageInfo.rotationDegrees
                val scaleImage = imageProxyToBitmap(image)
                cropImageView = binding.fragmentCameraViewDisplayId
                binding.fragmentCameraPreviewFinderId.visibility = View.GONE
                binding.fragmentCameraCaptureImageLayoutId.root.visibility = View.GONE
                binding.fragmentCameraViewDisplayId.visibility = View.VISIBLE
                binding.fragmentCameraScanImageLayoutId.root.visibility = View.VISIBLE
                cropImageView.setImageBitmap(scaleImage, rotation)
                flashMode = false
                camera?.cameraControl?.enableTorch(false)
                image.close()
                mCameraStopped()

               // Toast.makeText(context, rotation.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun showInContextUI(
        imageResourceID: Int,
        message: String,
        buttonName: String,
        status: Boolean,
        index: Int
    ) {
        dialog.setContentView(R.layout.layout_permission_edu_ui)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        val icon = dialog.findViewById(R.id.layout_permission_icon_id) as ImageView
        icon.setImageResource(imageResourceID)
        val body = dialog.findViewById(R.id.layout_permission_message_id) as TextView
        body.text = message
        val yesBtn = dialog.findViewById(R.id.layout_permission_ok_button_id) as TextView
        yesBtn.text = buttonName
        val noBtn = dialog.findViewById(R.id.layout_permission_cancel_button_id) as TextView
        yesBtn.setOnClickListener {
            if (!status) {
                dialog.dismiss()
                if (index == 0) {
                    requestCameraPermission.launch(REQUIRED_PERMISSIONS[0])
                } else {
                    requestStoragePermission.launch(REQUIRED_PERMISSIONS[1])
                }

            } else {
                Log.d(TAG, "showInContextUI: Move to setting activity")
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + activity?.packageName)
                )

                try {
                    if (index == 0) {
                        startForResultForCameraPermission.launch(intent)
                    } else {
                        startForResultForStoragePermission.launch(intent)
                    }

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.not_support_stt), Toast.LENGTH_LONG)
                        .show()
                }
                dialog.dismiss()
            }
        }
        noBtn.setOnClickListener {
            if (index == 0) {
                if (!sharedPreference.getValueBoolean(CAMERA_PERMANENT_DENIED, false)) {
                    sharedPreference.save(CAMERA_FIRST_ASK, false)
                }
                dialog.dismiss()
                NavHostFragment.findNavController(this).popBackStack()
            } else {
                Log.d(
                    TAG,
                    "showInContextUI: " + sharedPreference.getValueBoolean(
                        STORAGE_PERMANENT_DENIED,
                        false
                    )
                )
                if (!sharedPreference.getValueBoolean(STORAGE_PERMANENT_DENIED, false)) {
                    sharedPreference.save(STORAGE_FIRST_ASK, false)
                    Log.d(TAG, "showInContextUI: ")
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    companion object {
        private const val TAG = "CameraFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private const val CAMERA_FIRST_ASK = "FirstAskCamera"
        private const val CAMERA_PERMANENT_DENIED = "PermanentlyDeniedCamera"

        private const val STORAGE_FIRST_ASK = "FirstAskStorage"
        private const val STORAGE_PERMANENT_DENIED = "PermanentlyDeniedStorage"



    }

}




