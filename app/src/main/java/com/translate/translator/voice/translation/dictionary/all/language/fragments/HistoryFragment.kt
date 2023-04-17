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
import com.translate.translator.voice.translation.dictionary.all.language.adapters.*
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentHistoryBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.ItemSelectionEvent
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment(), ItemSelectionEvent {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var adapter: HistoryAdapter

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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        // return inflater.inflate(R.layout.fragment_gallery, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = HistoryAdapter(
            HistoryItemListener {
                Log.i(TAG, "onViewCreated: " + it)
                val item = it

                viewModel.setSourceItemValue(
                    LanguageItem(
                        it.displaySrcName,
                        it.srcLanguage,
                        null,
                        it.showSrcCountry,
                        it.srcCountry,
                        null,
                        null,
                        it.srcBcp47,
                        it.srcIso3
                    )
                )
                Log.i(
                    TAG,
                    "onViewCreated: S " + LanguageItem(
                        item.displaySrcName,
                        item.srcLanguage,
                        null,
                        item.showSrcCountry,
                        item.srcCountry,
                        null,
                        null,
                        item.srcBcp47,
                        item.srcIso3
                    )
                )

                viewModel.setTargetItemValue(
                    LanguageItem(
                        it.displayTarName,
                        it.tarLanguage,
                        null,
                        it.showTarCountry,
                        it.tarCountry,
                        null,
                        null,
                        it.tarBcp47,
                        it.tarIso3
                    )
                )
                Log.i(
                    TAG,
                    "onViewCreated: T " + LanguageItem(
                        it.displayTarName,
                        it.tarLanguage,
                        null,
                        it.showTarCountry,
                        it.tarCountry,
                        null,
                        null,
                        it.tarBcp47,
                        it.tarIso3
                    )
                )
                viewModel.setHomeText(it.srcText)
                viewModel.setTranslatedHomeText(it.translationText)
                viewModel.autoItemState = it.srcLanguage == "Auto Detected"
                //NavHostFragment.findNavController(this).navigate(HistoryFragmentDirections.actionHistoryFragmentIdToMainFragmentId())
                viewModel.camDetect = false
                NavHostFragment.findNavController(this).navigate(R.id.action_history_fragment_id_to_main_fragment_id)
            },
            MultiHistoryItemListener {
                viewModel.item = it
                MultiTransMainFragment.itemEvent = this
                MultiTransMainFragment.itemEvent !!.setLanguageToTable()
                NavHostFragment.findNavController(this)
                    .navigate(R.id.multi_translate_activity_id)


            },
            viewModel
        )


        binding.fragmentHistoryRecyclerViewId.adapter = adapter

        viewModel.historyItems.observe(viewLifecycleOwner, Observer {

            if (it.isEmpty()) {
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentHistoryRecyclerViewId.visibility = View.GONE
            } else {
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentHistoryRecyclerViewId.visibility = View.VISIBLE
            }
            adapter.data.clear()
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()


        })


        viewModel.MultiHistoryItems.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "onViewCreated: " + it.size)
            if (!binding.listSelectionLayoutId.multiListButtonId.isClickable && binding.listSelectionLayoutId.singleListButtonId.isClickable) {
                if (it.isEmpty()) {
                    binding.emptyStateLayoutId.visibility = View.VISIBLE
                    binding.fragmentHistoryRecyclerViewId.visibility = View.GONE
                } else {
                    binding.emptyStateLayoutId.visibility = View.GONE
                    binding.fragmentHistoryRecyclerViewId.visibility = View.VISIBLE
                }
                adapter.data.clear()
                adapter.data.addAll(it)
                adapter.notifyDataSetChanged()

            }
        })





        binding.listSelectionLayoutId.singleListButtonId.setOnClickListener {


            binding.listSelectionLayoutId.multiListButtonId.isClickable = true
            binding.listSelectionLayoutId.singleListButtonId.isClickable = false
            binding.listSelectionLayoutId.singleListButtonId.setBackgroundColor(
                resources.getColor(
                    R.color.primaryColor,
                    null
                )
            )
            binding.listSelectionLayoutId.multiListButtonId.setBackgroundColor(
                resources.getColor(
                    R.color.offColor,
                    null
                )
            )

            adapter.data.clear()
            adapter.notifyDataSetChanged()

            if (viewModel.historyItems.value.isNullOrEmpty()) {
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentHistoryRecyclerViewId.visibility = View.GONE
            } else {
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentHistoryRecyclerViewId.visibility = View.VISIBLE
                adapter.data.addAll(viewModel.historyItems.value!!)
                adapter.notifyDataSetChanged()
            }

        }



        binding.listSelectionLayoutId.multiListButtonId.setOnClickListener {
            /*viewModel.singleState = false
            viewModel.multiState = true*/

            binding.listSelectionLayoutId.multiListButtonId.isClickable = false
            binding.listSelectionLayoutId.singleListButtonId.isClickable = true
            binding.listSelectionLayoutId.singleListButtonId.setBackgroundColor(
                resources.getColor(
                    R.color.offColor,
                    null
                )
            )
            binding.listSelectionLayoutId.multiListButtonId.setBackgroundColor(
                resources.getColor(
                    R.color.primaryColor,
                    null
                )
            )

            adapter.data.clear()
            adapter.notifyDataSetChanged()

            if (viewModel.MultiHistoryItems.value.isNullOrEmpty()) {
                binding.emptyStateLayoutId.visibility = View.VISIBLE
                binding.fragmentHistoryRecyclerViewId.visibility = View.GONE
            } else {
                binding.emptyStateLayoutId.visibility = View.GONE
                binding.fragmentHistoryRecyclerViewId.visibility = View.VISIBLE


                adapter.data.addAll(viewModel.MultiHistoryItems.value!!)
                adapter.notifyDataSetChanged()
            }


        }


    }

/*    override fun onResume() {
        super.onResume()
        binding.invalidateAll()

    }*/


    companion object {
        private const val TAG = "HistoryFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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