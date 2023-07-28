package com.tomtom.sdk.examples.maps.mapdetails

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R

class BaseMapStyleItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var itemTitleTextView: TextView
        private set
    var itemImageView: ImageView
        private set

    init {
        itemTitleTextView = itemView.findViewById(R.id.text_view_title)
        itemImageView = itemView.findViewById(R.id.card_view_image)
    }
}