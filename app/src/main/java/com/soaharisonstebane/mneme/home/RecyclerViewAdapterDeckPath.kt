package com.soaharisonstebane.mneme.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.models.ExternalDeck

class RecyclerViewAdapterDeckPath(
    private val listOfLocations: List<ExternalDeck>,
    private val onLocationClicked: (ExternalDeck) -> Unit
): RecyclerView.Adapter<RecyclerViewAdapterDeckPath.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        return holder.bind(listOfLocations[position], onLocationClicked)
    }

    override fun getItemCount(): Int {
        return listOfLocations.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val tvLocationDeckName: TextView = view.findViewById(R.id.tv_location_deck_name)

        fun bind(
            location: ExternalDeck,
            onLocationClicked: (ExternalDeck) -> Unit
        ) {

            tvLocationDeckName.apply {
                text = context.getString(R.string.deck_location, location.deckName)
                setOnClickListener {
                    onLocationClicked(location)
                }
            }

        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_deck_path, parent, false)
                return ViewHolder(view)
            }
        }

    }

}