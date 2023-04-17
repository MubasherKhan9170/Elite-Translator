package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.translate.translator.voice.translation.dictionary.all.language.R

import com.translate.translator.voice.translation.dictionary.all.language.adapters.FavouriteListAdapter
import com.translate.translator.voice.translation.dictionary.all.language.adapters.MultiFavListener
import com.translate.translator.voice.translation.dictionary.all.language.adapters.SingleFavListener
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentFavouriteBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.ItemSelectionEvent
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [FavouriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FavouriteFragment : Fragment(), ItemSelectionEvent {

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var adapter: FavouriteListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false)
        // return inflater.inflate(R.layout.fragment_gallery, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FavouriteListAdapter(SingleFavListener {

            viewModel.setSourceItemValue(LanguageItem(it.displaySrcName,it.srcLanguage,null,it.showSrcCountry,it.srcCountry,null,null,it.srcBcp47,it.srcIso3))
            viewModel.setTargetItemValue(LanguageItem(it.displayTarName,it.tarLanguage,null,it.showTarCountry,it.tarCountry,null,null, it.tarBcp47,it.tarIso3))
            viewModel.setHomeText(it.srcText)
            viewModel.setTranslatedHomeText(it.translationText)
            viewModel.autoItemState = it.srcLanguage == "Auto Detected"
          //  NavHostFragment.findNavController(this).navigate(FavouriteFragmentDirections.actionFavouriteFragmentToMainFragmentId())
            viewModel.camDetect = false
            NavHostFragment.findNavController(this).navigate(R.id.action_favouriteFragment_to_main_fragment_id)
        },
            MultiFavListener {
                viewModel.item = it
                MultiTransMainFragment.itemEvent = this
                MultiTransMainFragment.itemEvent !!.setLanguageToTable()
                NavHostFragment.findNavController(this)
                    .navigate(R.id.multi_translate_activity_id)

            }
            ,viewModel)

        binding.fragmentFavRecyclerViewId.adapter = adapter

        viewModel.favouriteItems.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentFavRecyclerViewId.visibility = View.GONE
            }else{
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentFavRecyclerViewId.visibility = View.VISIBLE
            }
            adapter.data.clear()
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.MultiFavouriteItems.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "onViewCreated: "+ it.size)
            if(!binding.listSelectionLayoutId.multiListButtonId.isClickable && binding.listSelectionLayoutId.singleListButtonId.isClickable){
                if(it.isEmpty()){
                    binding.emptyStateLayoutId.visibility = View.VISIBLE
                    binding.fragmentFavRecyclerViewId.visibility = View.GONE
                }else{
                    binding.emptyStateLayoutId.visibility = View.GONE
                    binding.fragmentFavRecyclerViewId.visibility = View.VISIBLE
                }
                adapter.data.clear()
                adapter.data.addAll(it)
                adapter.notifyDataSetChanged()

            }
        })

        binding.listSelectionLayoutId.singleListButtonId.setOnClickListener {


            binding.listSelectionLayoutId.multiListButtonId.isClickable = true
            binding.listSelectionLayoutId.singleListButtonId.isClickable = false
            binding.listSelectionLayoutId.singleListButtonId.setBackgroundColor(resources.getColor(R.color.primaryColor, null))
            binding.listSelectionLayoutId.multiListButtonId.setBackgroundColor(resources.getColor(R.color.offColor, null))

            adapter.data.clear()
            adapter.notifyDataSetChanged()

            if(viewModel.favouriteItems.value.isNullOrEmpty()){
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentFavRecyclerViewId.visibility = View.GONE
            }else{
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentFavRecyclerViewId.visibility = View.VISIBLE
                adapter.data.addAll(viewModel.favouriteItems.value!!)
                adapter.notifyDataSetChanged()
            }

        }



        binding.listSelectionLayoutId.multiListButtonId.setOnClickListener {
            /*viewModel.singleState = false
            viewModel.multiState = true*/

            binding.listSelectionLayoutId.multiListButtonId.isClickable = false
            binding.listSelectionLayoutId.singleListButtonId.isClickable = true
            binding.listSelectionLayoutId.singleListButtonId.setBackgroundColor(resources.getColor(R.color.offColor, null))
            binding.listSelectionLayoutId.multiListButtonId.setBackgroundColor(resources.getColor(R.color.primaryColor, null))

            adapter.data.clear()
            adapter.notifyDataSetChanged()

            if(viewModel.MultiFavouriteItems.value.isNullOrEmpty()){
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentFavRecyclerViewId.visibility = View.GONE
            }else{
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentFavRecyclerViewId.visibility = View.VISIBLE


                adapter.data.addAll(viewModel.MultiFavouriteItems.value!!)
                adapter.notifyDataSetChanged()
            }


        }


/*        adapter = FavouriteListAdapter(FavouriteItemListener {
            viewModel.setSourceItemValue(LanguageItem(it.srcLanguage,null,it.srcCountry,null,it.srcBcp47,it.srcIso3))
            viewModel.setTargetItemValue(LanguageItem(it.tarLanguage,null,it.tarCountry,null,it.tarBcp47,it.tarIso3))
            viewModel.setHomeText(it.srcText)
            viewModel.setTranslatedHomeText(it.translationText)
            viewModel.autoItemState = it.srcLanguage == "Auto Detected"
            val frag: MainFragment? = this.parentFragment as MainFragment?
            frag?.setViewPager(0)

        },  viewModel)
        binding.fragmentFavRecyclerViewId.adapter = adapter

        viewModel.favouriteItems.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentFavRecyclerViewId.visibility = View.GONE
            }else{
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentFavRecyclerViewId.visibility = View.VISIBLE
            }
            adapter.data.clear()
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()
        })*/

    }


    companion object {
        private const val TAG = "FavouriteFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment FavouriteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = FavouriteFragment()
    }

    override fun setLanguageToTable() {
        // TODO("Not yet implemented")

        viewModel.item?.let {
            viewModel.clearMultiTable()
            viewModel.insertToMultiLangTable(viewModel.getLangListFromJson(it.tarLanguage))
        }
    }

    override fun setResult(): MutableList<ResultItem> {
        var list = mutableListOf<ResultItem>()
        //TODO("Not yet implemented")
        viewModel.item?.let {
            list = viewModel.getResultItemFromJson(it.tarLanguage, it.translationText)
        }
        return list
    }

    override fun setSourceText(): String {
        //TODO("Not yet implemented")
        return viewModel.item!!.srcText
    }

    override fun update(): Boolean {
        //TODO("Not yet implemented")
        return viewModel.item != null
    }

    override fun default() {
        //TODO("Not yet implemented")
        viewModel.item = null
    }
}