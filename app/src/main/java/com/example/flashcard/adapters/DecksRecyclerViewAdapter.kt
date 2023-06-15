package com.example.flashcard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.entities.Deck

class DecksRecyclerViewAdapter(
    private val listOfDecks: List<Deck>,
    private val deckClickListener: (Deck) -> Unit
): RecyclerView.Adapter<DecksRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return listOfDecks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(listOfDecks[position], deckClickListener)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val deckNameTV = view.findViewById<TextView>(R.id.deckNameTV)
        val deckDescriptionTV = view.findViewById<TextView>(R.id.deckDescriptionTV)
        val deckRoot = view.findViewById<CardView>(R.id.deckRoot)

        fun bind(
            deck: Deck,
            deckClickListener: (Deck) -> Unit
        ){
            deckNameTV.text = deck.deckName
            deckDescriptionTV.text = deck.deckDescription
            deckRoot.setOnClickListener { deckClickListener(deck) }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_adapter_deck_view, parent, false)

                return ViewHolder(view)
            }
        }
    }
}