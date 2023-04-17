package com.translate.translator.voice.translation.dictionary.all.language.data

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*
/**
 * Simple data class to hold information about an image included in the device's MediaStore.
 */
data class ImageAttributeMediaStore(
    val id: Long,
    val displayName: String,
    val dateAdded: Date,
    val contentUri: Uri,
    val orientation: Int
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<ImageAttributeMediaStore>() {
            override fun areItemsTheSame(oldItem: ImageAttributeMediaStore, newItem: ImageAttributeMediaStore) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ImageAttributeMediaStore, newItem: ImageAttributeMediaStore) =
                oldItem == newItem
        }
    }
}