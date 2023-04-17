package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.adapters.GalleryAdapter
import com.translate.translator.voice.translation.dictionary.all.language.adapters.GridItemDecoration
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentGalleryBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class GalleryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: FragmentGalleryBinding
    private val args: GalleryFragmentArgs by navArgs()
    private lateinit var sharedPreference: UserPreferencesRepository
    private lateinit var dialog: Dialog


    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Do something if permission granted
            if (isGranted) {
                Log.i(TAG, "Storage permission granted")
                showImages()
                binding.invalidateAll()
            } else {
                Log.i(TAG, "Storage permission denied")
                 NavHostFragment.findNavController(this).popBackStack()

            }
        }

    private val startForResultForStoragePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == 0) {
                Log.i(TAG, "Storage Ok Result: " + result.resultCode)
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        GalleryFragment.REQUIRED_PERMISSIONS[1]
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedPreference.save(GalleryFragment.STORAGE_FIRST_ASK, false)
                    sharedPreference.save(GalleryFragment.STORAGE_PERMANENT_DENIED, false)
                    Log.i(TAG, "Storage permission not granted")
                    // open the storage
                    showImages()
                    binding.invalidateAll()
                 //   NavHostFragment.findNavController(this).navigate(R.id.action_camera_fragment_id_to_gallery_Fragment)
                } else {
                    Log.i(TAG, "Storage permission not granted")
                    //NavHostFragment.findNavController(this).popBackStack()
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
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        // return inflater.inflate(R.layout.fragment_gallery, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        sharedPreference = UserPreferencesRepository.getInstance(requireContext())

        val galleryAdapter = GalleryAdapter() { image ->
            viewModel.setSelectedImageUri(image.contentUri)
            viewModel.imageUriOrientation = image.orientation
            Log.i(TAG, "onViewCreated: arg" + args.scanTab)
            if(args.scanTab == "scanScreen"){
                this.findNavController()
                    .navigate(GalleryFragmentDirections.actionGalleryFragmentToCameraFragmentId(args.scanTab))
            }else{
                this.findNavController()
                    .navigate(GalleryFragmentDirections.actionGalleryFragmentToCameraFragmentId("nothing"))
            }

            /*NavHostFragment.findNavController(this)
                .navigate(R.id.action_gallery_Fragment_to_camera_fragment_id)*/

        }


        viewModel.images.observe(viewLifecycleOwner, { images ->
            if(!images.isNullOrEmpty()){
                binding.emptyStateViewId.visibility = View.GONE
                binding.emptyStateTextId.visibility = View.GONE
                binding.galleryViewId.visibility = View.VISIBLE
                binding.galleryViewId.also {
                    it.layoutManager = GridLayoutManager(activity, 3)
                    //spacing in pixels
                    it.addItemDecoration(
                        GridItemDecoration(
                            4,
                            3,
                            true
                        )
                    )
                    //resources.getDimensionPixelOffset(R.dimen.small_margin)
                   // it.setBackgroundColor(resources.getColor(R.color.white, null))
                    it.adapter = galleryAdapter
                }
                galleryAdapter.submitList(images)
            }else{
                binding.emptyStateViewId.visibility = View.VISIBLE
                binding.emptyStateTextId.visibility = View.VISIBLE
                binding.galleryViewId.visibility = View.GONE
            }


        })

    }

    override fun onResume() {
        super.onResume()
        checkStoragePermissionStatus()

    }


    private fun showImages() {
        viewModel.loadImages()

    }

    private fun checkStoragePermissionStatus() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                GalleryFragment.REQUIRED_PERMISSIONS[1]
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "Storage Permission Granted")

                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                sharedPreference.save(GalleryFragment.STORAGE_FIRST_ASK, false)
                sharedPreference.save(GalleryFragment.STORAGE_PERMANENT_DENIED, false)
                // open the storage file
                showImages()
                binding.invalidateAll()
              //  NavHostFragment.findNavController(this).navigate(CameraFragmentDirections.actionCameraFragmentIdToGalleryFragment("nothing"))
            }
            shouldShowRequestPermissionRationale(GalleryFragment.REQUIRED_PERMISSIONS[1]) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Log.d(TAG,
                    "onViewCreated: Storage" + shouldShowRequestPermissionRationale(
                        GalleryFragment.REQUIRED_PERMISSIONS[1]
                    )
                )
                Log.d(TAG, "Just denied Storage")
                if (!sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false)) {
                    sharedPreference.save(GalleryFragment.STORAGE_FIRST_ASK, true)
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false),
                        GalleryFragment.REQUIRED_PERMISSIONS.indexOf(GalleryFragment.REQUIRED_PERMISSIONS[1])
                    )
                } else {
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false),
                        GalleryFragment.REQUIRED_PERMISSIONS.indexOf(GalleryFragment.REQUIRED_PERMISSIONS[1])
                    )
                }


            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                Log.d(TAG,
                    "first: Storage " + shouldShowRequestPermissionRationale(GalleryFragment.REQUIRED_PERMISSIONS[1]) + "First ASk: " + sharedPreference.getValueBoolean(
                        GalleryFragment.STORAGE_FIRST_ASK,
                        false
                    )
                )
                if (!shouldShowRequestPermissionRationale(GalleryFragment.REQUIRED_PERMISSIONS[1]) && !sharedPreference.getValueBoolean(
                        GalleryFragment.STORAGE_FIRST_ASK,
                        false
                    )
                ) {
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_one),
                        getString(R.string.premission_dialog_continue_button),
                        sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false),
                        GalleryFragment.REQUIRED_PERMISSIONS.indexOf(GalleryFragment.REQUIRED_PERMISSIONS[1])
                    )
                    sharedPreference.save(GalleryFragment.STORAGE_FIRST_ASK, true)
                } else {
                    sharedPreference.save(GalleryFragment.STORAGE_PERMANENT_DENIED, true)
                    showInContextUI(
                        R.drawable.ic_storage_permission_icon,
                        getString(R.string.storage_premission_msg_second),
                        getString(R.string.premission_dialog_setting_button),
                        sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false),
                        GalleryFragment.REQUIRED_PERMISSIONS.indexOf(GalleryFragment.REQUIRED_PERMISSIONS[1])
                    )
                    Log.d(TAG, "Storage permanently denied ")

                }


            }

        }

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
                   // requestCameraPermission.launch(GalleryFragment.REQUIRED_PERMISSIONS[0])
                } else {
                    requestStoragePermission.launch(GalleryFragment.REQUIRED_PERMISSIONS[1])
                }

            } else {
                Log.d(TAG, "showInContextUI: Move to setting activity")
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + activity?.packageName)
                )

                try {
                    if (index == 0) {
                       // startForResultForCameraPermission.launch(intent)
                    } else {
                        startForResultForStoragePermission.launch(intent)
                    }

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Your device does not support STT.", Toast.LENGTH_LONG)
                        .show()
                }
                dialog.dismiss()
            }
        }
        noBtn.setOnClickListener {
            if (index == 0) {
                /*if (!sharedPreference.getValueBoolean(GalleryFragment.CAMERA_PERMANENT_DENIED, false)) {
                    sharedPreference.save(GalleryFragment.CAMERA_FIRST_ASK, false)
                }
                dialog.dismiss()
                NavHostFragment.findNavController(this).popBackStack()*/
            } else {
                Log.d(
                    TAG,
                    "showInContextUI: " + sharedPreference.getValueBoolean(
                        GalleryFragment.STORAGE_PERMANENT_DENIED,
                        false
                    )
                )
                if (!sharedPreference.getValueBoolean(GalleryFragment.STORAGE_PERMANENT_DENIED, false)) {
                    sharedPreference.save(GalleryFragment.STORAGE_FIRST_ASK, false)
                    Log.d(TAG, "showInContextUI: ")
                }
                dialog.dismiss()
                NavHostFragment.findNavController(this).popBackStack()
            }
        }
        dialog.show()
    }
    companion object {
        private const val TAG = "GalleryFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private const val STORAGE_FIRST_ASK = "FirstAskStorage"
        private const val STORAGE_PERMANENT_DENIED = "PermanentlyDeniedStorage"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}