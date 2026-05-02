package com.example.mycarhere

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceAdapter(
    private var items: MutableList<Place>,
    private val onEdit: (Place) -> Unit,
    private val onDelete: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvPlaceName)
        val edit: ImageButton = v.findViewById(R.id.btnEditPlace)
        val delete: ImageButton = v.findViewById(R.id.btnDeletePlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val place = items[pos]
        h.name.text = place.name
        h.edit.setOnClickListener { onEdit(place) }
        h.delete.setOnClickListener { onDelete(place) }
    }

    override fun getItemCount() = items.size

    fun setData(newItems: List<Place>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
