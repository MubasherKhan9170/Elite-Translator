package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.data.VoiceDataItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutVoiceListLeftItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutVoiceListRightItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel



private val FIRST_PERSON_ITEM = 0
private val SECOND_PERSON_ITEM = 1

class VoiceListAdapter(val soundListener: VoiceItemListener, val model: TranslatorMainViewModel):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var data = mutableListOf<VoiceDataItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // TODO("Not yet implemented")
        return when (viewType) {
            FIRST_PERSON_ITEM -> VoiceListAdapter.LeftViewHolder.from(parent)
            SECOND_PERSON_ITEM -> VoiceListAdapter.RightViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
       // return LeftViewHolder.from(parent)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //   TODO("Not yet implemented")
        when(holder){
            is VoiceListAdapter.LeftViewHolder -> {
               // val item = data[position] as VoiceDataItem.LeftItem
                holder.bind(data[position], soundListener)
                //holder.binding.s
            }

            is VoiceListAdapter.RightViewHolder -> {
               // val item = data[position] as VoiceDataItem.RightItem
                holder.bind(data[position], soundListener)
               // notifyItemChanged(position)
            }

        }
    }

    override fun getItemCount(): Int {
        // TODO("Not yet implemented")
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].viewType
    }


    class LeftViewHolder private constructor(val binding: LayoutVoiceListLeftItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VoiceDataItem, clickListener: VoiceItemListener) {
            binding.sourceText = item.src
            binding.targetText = item.tar
            binding.left = item.viewType
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): LeftViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutVoiceListLeftItemBinding.inflate(layoutInflater, parent, false)
                return LeftViewHolder(binding)
            }
        }
    }

    class RightViewHolder private constructor(val binding: LayoutVoiceListRightItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VoiceDataItem, clickListener: VoiceItemListener) {
            binding.sourceText = item.src
            binding.targetText = item.tar
            binding.right = item.viewType
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): RightViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutVoiceListRightItemBinding.inflate(layoutInflater, parent, false)
                return RightViewHolder(binding)
            }
        }
    }



}
class VoiceItemListener(val clickListener: (text: String, type: Int) -> Unit) {
    fun onClick(text: String, type: Int){
        clickListener(text, type)
    }
}
