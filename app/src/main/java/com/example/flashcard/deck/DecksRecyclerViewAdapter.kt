package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.entities.Deck
import org.w3c.dom.Text

class DecksRecyclerViewAdapter(
    private val listOfDecks: List<Deck>,
    private val context: Context,
    private val deckClickListener: (Deck) -> Unit
): RecyclerView.Adapter<DecksRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return listOfDecks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(listOfDecks[position], context, deckClickListener)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val deckNameTV: TextView? = view.findViewById(R.id.deckNameTV)
        private val deckDescriptionTV: TextView? = view.findViewById(R.id.deckDescriptionTV)
        private val deckRoot: CardView? = view.findViewById(R.id.deckRoot)
        private val deckLanguages: TextView? = view.findViewById(R.id.languages)
        private val cardSum: TextView? = view.findViewById(R.id.cardsSum)
        private val deckFirstLanguageHint: TextView? = view.findViewById(R.id.firstLanguageHint)


        fun bind(
            deck: Deck,
            context: Context,
            deckClickListener: (Deck) -> Unit
        ){
            deckNameTV?.text = deck.deckName
            deckDescriptionTV?.text = deck.deckDescription
            deckLanguages?.text = context.getString(R.string.deck_languages, deck.deckFirstLanguage, deck.deckSecondLanguage)
            cardSum?.text = context.getString(R.string.cards_sum, deck.cardSum.toString())
            deckRoot?.setOnClickListener { deckClickListener(deck) }
            deckFirstLanguageHint?.text = deck.deckFirstLanguage
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