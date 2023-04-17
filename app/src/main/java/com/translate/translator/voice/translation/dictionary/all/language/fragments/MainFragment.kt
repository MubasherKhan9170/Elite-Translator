package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.translate.translator.voice.translation.dictionary.all.language.adapters.ViewerPagerAdapter
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.OnBackPressedCallback
import com.translate.translator.voice.translation.dictionary.all.language.R

import com.translate.translator.voice.translation.dictionary.all.language.TranslatorMainActivity
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentLanguageSelectionBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private var binding: FragmentLanguageSelectionBinding? = null
    private val args: MainFragmentArgs by navArgs()
    lateinit var callback: OnBackPressedCallback
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.

    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem == 0) {
                    // If the user is currently looking at the first step, allow the system to handle the
                    // Back button. This calls finish() on this activity and pops the back stack.



                    if((activity as? TranslatorMainActivity)!!.showExit){
                        (activity as? TranslatorMainActivity)!!.showExit = false
                        requireActivity().finish()

                    }else{
                        val trans = childFragmentManager.beginTransaction()
                        trans.add(LogoutFragment.newInstance(), "Logout").commit()
                    }

                   //
                } else {
                    // Otherwise, select the previous step.
                    viewPager.currentItem = 0
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // Log.i(TAG, "Main: Camera "+ args.tab)



        val viewerPagerAdapter: ViewerPagerAdapter by lazy { ViewerPagerAdapter(childFragmentManager, lifecycle) }
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = viewerPagerAdapter
        viewPager.offscreenPageLimit = 3

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        val tabLayoutMediator = TabLayoutMediator(
            tabLayout,
            viewPager,
            true,
            true
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.tab_home)
                    tab.setIcon(R.drawable.ic_home_tab_colored_icon)
                }
                1 -> {
                    tab.text = getString(R.string.tab_chat)
                    tab.setIcon(R.drawable.ic_voice_tab_icon)
                }
                2 -> {
                    tab.text = getString(R.string.tab_scan_pdf)
                    tab.setIcon(R.drawable.ic_scan_pdf_icon)
                }
                3 -> {
                    tab.text = getString(R.string.tab_dictionary)
                    tab.setIcon(R.drawable.ic_dictionary_icon)
                }
                4 -> {
                    tab.text = getString(R.string.tab_screen_translation)
                    tab.setIcon(R.drawable.ic_tab_screen_icon)
                }
            }
        }
        tabLayoutMediator.attach()
        viewPager.isUserInputEnabled = false

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                viewModel.tabSelected = position == 1

                if(position == 4 && viewModel.autoItemState){
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

                //callback.isEnabled = position != 0

                Log.d(TAG, "onPageSelected: $position")
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)


            }
        })

    }
    fun setViewPager(tab: Int){
        viewPager.currentItem = 0
    }



    companion object {
        private const val TAG = "MainFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MainFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}