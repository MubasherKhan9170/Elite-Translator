package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.data.ResultItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutMultiTextOutputDisplayerBinding

class MultiResultAdapter(
    val speakerIconListener: SpeakerIconListener,
    val copiedIconListener: CopiedIconListener,
    val fullScreenIconListener: FullScreenIconListener,
    val shareIconListener: ShareIconListener
) : RecyclerView.Adapter<MultiResultAdapter.ViewHolder>() {
    var data = mutableListOf<ResultItem?>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiResultAdapter.ViewHolder {
        // TODO("Not yet implemented")
        return MultiResultAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MultiResultAdapter.ViewHolder, position: Int) {
        //TODO("Not yet implemented")
        val item = data[position]
        holder.bind(
            speakerIconListener,
            copiedIconListener,
            fullScreenIconListener,
            shareIconListener,
            item!!,
            position
        )
    }

    override fun getItemCount(): Int {
        //TODO("Not yet implemented")
        return data.size
    }


    class ViewHolder private constructor(val binding: LayoutMultiTextOutputDisplayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(speakerIconListener: SpeakerIconListener,
                 copiedIconListener: CopiedIconListener,
                 fullScreenIconListener: FullScreenIconListener,
                 shareIconListener: ShareIconListener,
                 item: ResultItem,
                 index: Int) {
            binding.name = item.Name
            binding.flagIcon = item.flagIcon
            binding.text = item.text
            binding.transLangShow.visibility = View.VISIBLE
            binding.position = index
            binding.speakerClickListener = speakerIconListener
            binding.copyClickListener = copiedIconListener
            binding.fullClickListener = fullScreenIconListener
            binding.shareClickListener = shareIconListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    LayoutMultiTextOutputDisplayerBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

class SpeakerIconListener(val speakerClickListener: (pos: Int, text: String) -> Unit) {
    fun onSpeakerIconClick(index: Int, text: String) {
        speakerClickListener(index, text)
    }
}

class CopiedIconListener(val copyClickListener: (text: String) -> Unit) {
    fun onCopyIconClick(text: String) {
        copyClickListener(text)
    }
}

class FullScreenIconListener(val fullScreenClickListener: (text: String) -> Unit) {
    fun onFullScreenIconClick(text: String) {
        fullScreenClickListener(text)
    }
}

class ShareIconListener(val shareClickListener: (text: String) -> Unit) {
    fun onShareClickListener(text: String) {
        shareClickListener(text)
    }
}
