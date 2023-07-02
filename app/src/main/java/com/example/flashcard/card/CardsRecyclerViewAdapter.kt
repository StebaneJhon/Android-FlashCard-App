package com.example.flashcard.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck

class CardsRecyclerViewAdapter(
    private val cardList: List<Card>,
    private val deck: Deck,
    private val fullScreenClickListener: (Card) -> Unit,
    private val editCardClickListener: (Card) -> Unit,
    private val deleteCardClickListener: (Card) -> Unit,
    private val cardClickListener: (Card) -> Unit
) : RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            cardList[position],
            deck,
            fullScreenClickListener,
            editCardClickListener,
            deleteCardClickListener,
            cardClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val onCardText = view.findViewById<TextView>(R.id.onCardTextTV)
        val onCardTextDescription = view.findViewById<TextView>(R.id.onCardTextDescriptionTV)
        val languageHint = view.findViewById<TextView>(R.id.languageHint)
        val cardFullscreenButton = view.findViewById<ImageButton>(R.id.fullScreenButton)
        val cardEditButton: ImageButton = view.findViewById(R.id.editCardButton)
        val cardDeleteButton: ImageButton = view.findViewById(R.id.deleteCardButton)
        val cardRoot: CardView = view.findViewById(R.id.cardRoot)

        fun bind(
            card: Card,
            deck: Deck,
            fullScreenClickListener: (Card) -> Unit,
            editCardClickListener: (Card) -> Unit,
            deleteCardClickListener: (Card) -> Unit,
            cardClickListener: (Card) -> Unit
        ) {
            languageHint.text = deck.deckFirstLanguage
            onCardText.text = card.cardContent
            onCardTextDescription.text = card.contentDescription

            cardFullscreenButton.setOnClickListener { fullScreenClickListener(card) }
            cardEditButton.setOnClickListener { editCardClickListener(card) }
            cardDeleteButton.setOnClickListener { deleteCardClickListener(card) }
            cardRoot.setOnClickListener { cardClickListener(card) }
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