package com.tomtom.sdk.examples.maps.mapdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R

class MapPreferenceDataAdapter(private val mapPreferenceItemList: List<MapPreferenceItem>, private val clickListener: OnRecyclerViewItemClickListener, private val currentMethods: MutableMap<String, MapPreference>): RecyclerView.Adapter<MapPreferenceItemHolder>() {
    private val mapOfSelectedItems = mutableMapOf<Int, MapPreference>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPreferenceItemHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val mapPreferenceItemView = layoutInflater.inflate(R.layout.activity_map_preference_item, parent, false)

        return MapPreferenceItemHolder(mapPreferenceItemView)
    }

    override fun onBindViewHolder(holder: MapPreferenceItemHolder, position: Int) {
        val mapPreferenceItem = mapPreferenceItemList[position]

        holder.getTitleText()?.text = mapPreferenceItem.title
        holder.getImageView()?.setImageResource(mapPreferenceItem.imageId)
        holder.getImageView2()?.setImageResource(mapPreferenceItem.imageIdHide)

        //Set blue background border for currently displayed map preferences
        if(currentMethods.containsValue(mapPreferenceItem.methodShow)) {
            holder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            mapOfSelectedItems[holder.bindingAdapterPosition] = mapPreferenceItem.methodShow
        } else if(currentMethods.containsValue(mapPreferenceItem.methodHide)) {
            holder.getImageView2()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            mapOfSelectedItems[holder.bindingAdapterPosition] = mapPreferenceItem.methodHide
        }

        holder.getImageView()?.setOnClickListener {
            clickListener.onMapPreferenceItemClick(mapPreferenceItem.title, mapPreferenceItem.methodShow)

            if(!mapOfSelectedItems.containsValue(mapPreferenceItem.methodShow)) {
                if(mapOfSelectedItems.containsKey(holder.bindingAdapterPosition)) {
                    holder.getImageView2()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border)
                }
                mapOfSelectedItems[holder.bindingAdapterPosition] = mapPreferenceItem.methodShow
                holder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            }
        }

        holder.getImageView2()?.setOnClickListener {
            clickListener.onMapPreferenceItemClick(mapPreferenceItem.title, mapPreferenceItem.methodHide)

            if(!mapOfSelectedItems.containsValue(mapPreferenceItem.methodHide)) {
                if(mapOfSelectedItems.containsKey(holder.bindingAdapterPosition)) {
                    holder.getImageView()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border)
                }
                mapOfSelectedItems[holder.bindingAdapterPosition] = mapPreferenceItem.methodHide
                holder.getImageView2()?.setBackgroundResource(R.drawable.map_detail_card_view_item_border_selected)
            }
        }
    }

    override fun getItemCount(): Int {
        return mapPreferenceItemList.size
    }
}