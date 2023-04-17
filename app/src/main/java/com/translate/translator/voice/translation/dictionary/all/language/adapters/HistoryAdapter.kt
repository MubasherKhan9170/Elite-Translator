package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.database.MultiTranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.database.TranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutHistoryListItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutMultiLangHistoryListItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
private val SINGLE_ITEM_VIEW_TYPE =0
private val MULTI_ITEM_VIEW_TYPE = 1
class HistoryAdapter(val singleClickListener: HistoryItemListener, val multiClickListener: MultiHistoryItemListener, val model: TranslatorMainViewModel):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var data = mutableListOf<Any?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       // TODO("Not yet implemented")
        // Create a new view, which defines the UI of the list item
        return when (viewType) {
            SINGLE_ITEM_VIEW_TYPE -> HistoryAdapter.SingleViewHolder.from(parent)
            MULTI_ITEM_VIEW_TYPE -> HistoryAdapter.MultiViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
     //   TODO("Not yet implemented")
        when(holder){
            is SingleViewHolder ->{
               // Log.i("Multi", "onBindViewHolder: no")
                val item = data[position] as TranslationItem
                holder.bind(singleClickListener, item, model)
            }
            is MultiViewHolder ->{
                //Log.i("Multi", "onBindViewHolder: yes")
                val item = data[position] as MultiTranslationItem
                holder.bind(multiClickListener, item, model)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {

        return when (data[position]) {
            is TranslationItem -> SINGLE_ITEM_VIEW_TYPE
            is MultiTranslationItem -> MULTI_ITEM_VIEW_TYPE
            else -> {-1}
        }
    }

    override fun getItemCount(): Int {
       // TODO("Not yet implemented")
        return data.size
    }


    class SingleViewHolder private constructor(val binding: LayoutHistoryListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: HistoryItemListener, item: TranslationItem, model: TranslatorMainViewModel) {
            binding.history = item
            binding.clickListener = clickListener
            binding.dataSource = model
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): SingleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutHistoryListItemBinding.inflate(layoutInflater, parent, false)
                return SingleViewHolder(binding)
            }
        }
    }

    class MultiViewHolder private constructor(val binding: LayoutMultiLangHistoryListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: MultiHistoryItemListener, item: MultiTranslationItem, model: TranslatorMainViewModel) {
            val results = model.getItemValueFromJson(item.translationText)
            val langs = model.getLanguageFromJson(item.tarLanguage)
            if(results.size >= 2){
                binding.item1 = results[0]
                binding.language1 = langs[0]
                binding.item2 = results[1]
                binding.language2 = langs[1]

            }else{
                binding.item1 = results[0]
                binding.language1 = langs[0]
                binding.item2 = null
                binding.language2 = null
            }
            binding.size = results.size
            binding.history = item
            binding.clickListener = clickListener
            binding.dataSource = model
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): MultiViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutMultiLangHistoryListItemBinding.inflate(layoutInflater, parent, false)
                return MultiViewHolder(binding)
            }
        }
    }




}

class HistoryItemListener(val clickListener: (item: TranslationItem) -> Unit) {
    fun onClick(item: TranslationItem){
        clickListener(item)
    }
}

class MultiHistoryItemListener(val clickListener: (item: MultiTranslationItem) -> Unit) {
    fun onClick(item: MultiTranslationItem){
        clickListener(item)
    }
}