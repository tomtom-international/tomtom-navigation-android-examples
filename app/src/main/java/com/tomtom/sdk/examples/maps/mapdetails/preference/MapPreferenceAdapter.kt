package com.tomtom.sdk.examples.maps.mapdetails.preference

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.databinding.ActivityMapPreferenceItemBinding

class MapPreferenceAdapter(
    private val mapPreferences: MutableMap<MapPreference, Boolean>,
    private val itemClickListener: MapPreferenceChangeListener,
) : RecyclerView.Adapter<MapPreferenceAdapter.MapPreferenceItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPreferenceItemHolder {
        val binding = ActivityMapPreferenceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapPreferenceItemHolder(binding)
    }

    override fun onBindViewHolder(holder: MapPreferenceItemHolder, position: Int) {
        val mapPreference = mapPreferences.keys.toList()[position]
        val isEnabled = mapPreferences.values.toList()[position]

        with(holder.binding) {
            preferenceTitle.text = mapPreference.title
            enabledPreferenceImage.setImageResource(mapPreference.preferenceEnabledImageId)
            disabledPreferenceImage.setImageResource(mapPreference.preferenceDisabledImageId)

            enabledPreferenceImage.setBackgroundResource(
                if (isEnabled) R.drawable.map_detail_card_view_item_border_selected
                else R.drawable.map_detail_card_view_item_border
            )

            disabledPreferenceImage.setBackgroundResource(
                if (isEnabled) R.drawable.map_detail_card_view_item_border
                else R.drawable.map_detail_card_view_item_border_selected
            )

            enabledPreferenceContainer.setOnClickListener {
                if (!isEnabled) {
                    itemClickListener.onMapPreferenceChange(mapPreference, true)
                }
            }

            disabledPreferenceContainer.setOnClickListener {
                if (isEnabled) {
                    itemClickListener.onMapPreferenceChange(mapPreference, false)
                }
            }
        }
    }

    fun updatePreference(preference: MapPreference, isEnabled: Boolean) {
        val position = mapPreferences.keys.toList().indexOf(preference)
        mapPreferences[preference] = isEnabled
        notifyItemChanged(position)
    }

    override fun getItemCount() = mapPreferences.size

    inner class MapPreferenceItemHolder(val binding: ActivityMapPreferenceItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
