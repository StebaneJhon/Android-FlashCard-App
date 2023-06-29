package com.example.flashcard.deck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck

class DecksRecyclerViewAdapter(
    private val listOfDecks: List<ImmutableDeck>,
    private val context: Context,
    private val editDeckClickListener: (ImmutableDeck) -> Unit,
    private val deleteDeckClickListener: (ImmutableDeck) -> Unit,
    private val deckClickListener: (ImmutableDeck) -> Unit
) : RecyclerView.Adapter<DecksRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return listOfDecks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            listOfDecks[position],
            context,
            editDeckClickListener,
            deleteDeckClickListener,
            deckClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val deckNameTV: TextView? = view.findViewById(R.id.deckNameTV)
        private val deckDescriptionTV: TextView? = view.findViewById(R.id.deckDescriptionTV)
        private val deckRoot: CardView? = view.findViewById(R.id.deckRoot)
        private val deckLanguages: TextView? = view.findViewById(R.id.languages)
        private val cardSum: TextView? = view.findViewById(R.id.cardsSum)
        private val deckFirstLanguageHint: TextView? = view.findViewById(R.id.firstLanguageHint)
        private val editDeckButton: ImageButton? = view.findViewById(R.id.editDeckButton)
        private val deleteDeckButton: ImageButton? = view.findViewById(R.id.deleteDeckButton)


        fun bind(
            deck: ImmutableDeck,
            context: Context,
            editDeckClickListener: (ImmutableDeck) -> Unit,
            deleteDeckClickListener: (ImmutableDeck) -> Unit,
            deckClickListener: (ImmutableDeck) -> Unit
        ) {
            deckNameTV?.text = deck.deckName
            deckDescriptionTV?.text = deck.deckDescription
            deckLanguages?.text = context.getString(
                R.string.deck_languages,
                deck.deckFirstLanguage,
                deck.deckSecondLanguage
            )
            cardSum?.text = context.getString(R.string.cards_sum, deck.cardSum.toString())
            deckRoot?.setOnClickListener { deckClickListener(deck) }
            deckFirstLanguageHint?.text = deck.deckFirstLanguage
            editDeckButton?.setOnClickListener { editDeckClickListener(deck) }
            deleteDeckButton?.setOnClickListener { deleteDeckClickListener(deck) }
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