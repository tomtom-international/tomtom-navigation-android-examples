package com.tomtom.sdk.examples.maps.mapdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode

class BaseMapStyleDataAdapter(private val recyclerView: RecyclerView, private val baseMapStyleItemList: List<BaseMapStyleItem>, private val clickListener: OnRecyclerViewItemClickListener, private val currentStyleMode: StyleMode, private val currentStandardStyle: StyleDescriptor): RecyclerView.Adapter<BaseMapStyleItemHolder>() {
    private var selectedItem = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMapStyleItemHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val baseMapStyleItemView = layoutInflater.inflate(R.layout.activity_base_map_style_item, parent, false)

        return BaseMapStyleItemHolder(baseMapStyleItemView)
    }

    override fun onBindViewHolder(holder: BaseMapStyleItemHolder, position: Int) {
        val baseMapStyleItem = baseMapStyleItemList[position]

        holder.getTitleText()?.text = baseMapStyleItem.title
        holder.getImageView()?.setImageResource(baseMapStyleItem.imageId)

        //Set blue background border for currently displayed map style
        if(baseMapStyleItem.styleMode == currentStyleMode && baseMapStyleItem.styleDescriptor == currentStandardStyle) {
            holder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            selectedItem = holder.bindingAdapterPosition
        }

        holder.itemView.setOnClickListener{
            clickListener.onBaseMapStyleItemClick(baseMapStyleItem)

            if(holder.bindingAdapterPosition != selectedItem) {
                if(selectedItem != -1) {
                    val previousHolder =
                        recyclerView.findViewHolderForAdapterPosition(selectedItem) as BaseMapStyleItemHolder
                    previousHolder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border)
                }
                selectedItem = holder.bindingAdapterPosition
                holder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            }
        }
    }

    override fun getItemCount(): Int {
        return baseMapStyleItemList.size
    }
}