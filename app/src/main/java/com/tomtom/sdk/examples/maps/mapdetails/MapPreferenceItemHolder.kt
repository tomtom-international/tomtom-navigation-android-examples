package com.tomtom.sdk.examples.maps.mapdetails

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R

class MapPreferenceItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var itemTitleTextView: TextView
    private var itemImageViewPreferenceOn: ImageView
    private var itemImageViewPreferenceOff: ImageView

    init {
        itemTitleTextView = itemView.findViewById(R.id.map_preference_text_view_title)
        itemImageViewPreferenceOn = itemView.findViewById(R.id.map_preference_card_view_image)
        itemImageViewPreferenceOff = itemView.findViewById(R.id.card_view_image_2)
    }

    fun getTitleText(): TextView {
        return itemTitleTextView
    }

    fun getImageViewPreferenceOn(): ImageView {
        return itemImageViewPreferenceOn
    }

    fun getImageViewPreferenceOff(): ImageView {
        return itemImageViewPreferenceOff
    }
}