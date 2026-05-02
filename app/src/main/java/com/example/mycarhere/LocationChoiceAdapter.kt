package com.example.mycarhere

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationChoiceAdapter(
    private val items: List<Place>,
    private val selectedNames: MutableSet<String>,
    private val multiSelect: Boolean,
    private val onPick: (String) -> Unit
) : RecyclerView.Adapter<LocationChoiceAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvLocationName)
        val check: ImageView = v.findViewById(R.id.ivSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_location_choice, parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val place = items[pos]
        h.name.text = place.name
        h.check.visibility = if (selectedNames.contains(place.name)) View.VISIBLE else View.GONE
        h.itemView.setOnClickListener {
            if (!multiSelect) selectedNames.clear()
            if (selectedNames.contains(place.name)) {
                selectedNames.remove(place.name)
            } else {
                selectedNames.add(place.name)
            }
            notifyDataSetChanged()
            onPick(place.name)
        }
    }

    override fun getItemCount() = items.size
}
