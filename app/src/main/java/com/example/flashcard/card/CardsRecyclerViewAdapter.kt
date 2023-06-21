package com.example.flashcard.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.entities.Card

class CardsRecyclerViewAdapter(
    private val cardList: List<Card>
): RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(cardList[position])
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val onCardText = view.findViewById<TextView>(R.id.onCardTextTV)
        val onCardTextDescription = view.findViewById<TextView>(R.id.onCardTextDescriptionTV)

        fun bind(card: Card) {
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