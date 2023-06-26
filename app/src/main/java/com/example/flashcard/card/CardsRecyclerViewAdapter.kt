package com.example.flashcard.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck

class CardsRecyclerViewAdapter(
    private val cardList: List<Card>,
    private val deck: Deck
): RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(cardList[position], deck)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val onCardText = view.findViewById<TextView>(R.id.onCardTextTV)
        val onCardTextDescription = view.findViewById<TextView>(R.id.onCardTextDescriptionTV)
        val languageHint = view.findViewById<TextView>(R.id.languageHint)

        fun bind(card: Card,
                 deck: Deck
        ) {
            languageHint.text = deck.deckFirstLanguage
            onCardText.text = card.cardContent
            onCardTextDescription.text = card.contentDescription
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_adapter_card_view, parent, false)

                return ViewHolder(view)
            }
        }
    }

}