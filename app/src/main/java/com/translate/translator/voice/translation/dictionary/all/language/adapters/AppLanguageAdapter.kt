package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.data.AppLangItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutAppLangListItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import java.util.ArrayList

class AppLanguageAdapter(val soundListener: ItemListener, val model: TranslatorMainViewModel):  RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {

    var data = mutableListOf<AppLangItem>()
    val filterItem = mutableListOf<AppLangItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppLanguageAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ViewHolder){
            holder.bind(data[position], position, soundListener)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder private constructor(val binding: LayoutAppLangListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppLangItem, pos: Int, clickListener: ItemListener) {
            binding.name = item.Name
            binding.code = item.code
            binding.position = pos
            binding.itemListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutAppLangListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<AppLangItem> = ArrayList<AppLangItem>()
            if (constraint.isEmpty()) {
                filteredList.addAll(filterItem)
                Log.i("CountryListAdapter", "performFiltering: " + filterItem.size
                )
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in filterItem) {
                    Log.i("CountryListAdapter", "performFiltering: "+ item)
                    if (item.Name!!.lowercase().contains(filterPattern)) {
                        filteredList.add(item)
                        Log.d("CountryListAdapter", "performFiltering: "+ filteredList.size)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            data.clear()
            data.addAll(results.values as MutableList<AppLangItem>)
            notifyDataSetChanged()
        }
    }
}

class ItemListener(val clickListener: (text: String, code: String, pos: Int) -> Unit) {
    fun onClick(text: String, code: String, position: Int){
        clickListener(text, code, position)
    }
}