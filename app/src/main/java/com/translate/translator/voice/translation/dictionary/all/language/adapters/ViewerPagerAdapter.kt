package com.translate.translator.voice.translation.dictionary.all.language.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.translate.translator.voice.translation.dictionary.all.language.fragments.*

class ViewerPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
       return when(position){

            0 ->  HomeFragment.newInstance()
            1 ->  VoiceFragment.newInstance()
            2 ->  ScanPdfFragment.newInstance()
            3 ->  DictionaryFragment.newInstance()
            4 ->  ScreenTranslationFragment.newInstance()
           else -> HomeFragment.newInstance()
       }
    }



}