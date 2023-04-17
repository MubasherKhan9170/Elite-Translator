package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.data.ImageAttributeMediaStore


/**
 * A [ListAdapter] for [MediaStoreImage]s.
 */
class GalleryAdapter(val onClick: (ImageAttributeMediaStore) -> Unit) :
    ListAdapter<ImageAttributeMediaStore, GalleryAdapter.ImageViewHolder>(ImageAttributeMediaStore.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.layout_gallery_image_item, parent, false)
        return ImageViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val mediaStoreImage = getItem(position)
        holder.rootView.tag = mediaStoreImage

        Glide.with(holder.imageView)
            .load(mediaStoreImage.contentUri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.imageView)
    }



    /**
     * Basic [RecyclerView.ViewHolder] for our gallery.
     */
    class ImageViewHolder(view: View, onClick: (ImageAttributeMediaStore) -> Unit) :
        RecyclerView.ViewHolder(view) {
        val rootView = view
        val imageView: ImageView = view.findViewById(R.id.image)

        init {
            imageView.setOnClickListener {
                val image = rootView.tag as? ImageAttributeMediaStore ?: return@setOnClickListener
                onClick(image)
            }
        }
    }
}

class GridItemDecoration(
    val spacing: Int,
    private val spanCount: Int,
    private val includeEdge: Boolean
) :
    RecyclerView.ItemDecoration() {

    /**
     * Applies padding to all sides of the [Rect], which is the container for the view
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        if (includeEdge) {
            outRect.left =
                spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right =
                (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left =
                column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right =
                spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}


