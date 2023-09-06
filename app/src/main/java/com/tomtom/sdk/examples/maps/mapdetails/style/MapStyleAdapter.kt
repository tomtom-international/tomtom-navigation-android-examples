package com.tomtom.sdk.examples.maps.mapdetails.style

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.databinding.ActivityBaseMapStyleItemBinding

class MapStyleAdapter(
    private val mapStyles: List<MapStyle>,
    private var activeStyle: MapStyle,
    private val clickListener: MapStyleChangeListener,
) : RecyclerView.Adapter<MapStyleAdapter.MapStyleHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapStyleHolder {
        val binding = ActivityBaseMapStyleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapStyleHolder(binding)
    }

    override fun onBindViewHolder(holder: MapStyleHolder, position: Int) {
        val baseMapStyleItem = mapStyles[position]

        holder.binding.textViewTitle.text = baseMapStyleItem.title
        holder.binding.cardViewImage.setImageResource(baseMapStyleItem.imageId)

        holder.binding.cardViewImage.setBackgroundResource(
            if (baseMapStyleItem == activeStyle)
                R.drawable.map_detail_card_view_item_border_selected
            else R.drawable.map_detail_card_view_item_border
        )

        holder.itemView.setOnClickListener {
            if (baseMapStyleItem != activeStyle) {
                clickListener.onMapStyleChange(baseMapStyleItem)
            }
        }
    }

    override fun getItemCount() = mapStyles.size

    fun updateCurrentStyle(style: MapStyle) {
        val oldItemIndex = mapStyles.indexOf(activeStyle)
        val newItemIndex = mapStyles.indexOf(style)

        activeStyle = style

        notifyItemChanged(oldItemIndex)
        notifyItemChanged(newItemIndex)
    }

    inner class MapStyleHolder(val binding: ActivityBaseMapStyleItemBinding) : RecyclerView.ViewHolder(binding.root)

}
