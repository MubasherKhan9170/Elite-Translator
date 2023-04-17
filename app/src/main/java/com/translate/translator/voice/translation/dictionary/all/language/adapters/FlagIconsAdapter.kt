package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutFlagIconItemBinding

class FlagIconsAdapter:   RecyclerView.Adapter<FlagIconsAdapter.ViewHolder>() {
    var data = mutableListOf<BitmapDrawable>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FlagIconsAdapter.ViewHolder {
        //TODO("Not yet implemented")
        return FlagIconsAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FlagIconsAdapter.ViewHolder, position: Int) {
        //TODO("Not yet implemented")
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        //TODO("Not yet implemented")
        return data.size
    }

    class ViewHolder private constructor(val binding: LayoutFlagIconItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BitmapDrawable) {
            binding.flagIcon= item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutFlagIconItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}