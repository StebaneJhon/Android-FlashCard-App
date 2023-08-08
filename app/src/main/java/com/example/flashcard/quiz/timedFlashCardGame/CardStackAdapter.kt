package com.example.flashcard.quiz.timedFlashCardGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector

class CardStackAdapter(
    private val context: Context,
    private var items: List<ImmutableCard>,
    private val deck: ImmutableDeck
): RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(context, items[position], deck)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val cardBackgroundIM: ImageView = view.findViewById(R.id.cardBackgroundImg)
        private val languageHint: TextView = view.findViewById(R.id.languageHint)
        private val onCardText: TextView = view.findViewById(R.id.onCardText)
        private val onCardTextDefinition: TextView = view.findViewById(R.id.onCardTextDefinition)
        private val cardRoot: CardView = view.findViewById(R.id.cardRoot)

        fun bind(
            context: Context,
            card: ImmutableCard,
            deck: ImmutableDeck
        ) {
            languageHint.text = deck.deckFirstLanguage
            onCardText.text = card.cardContent
            onCardTextDefinition.text = card.contentDescription

            val background = card.backgroundImg?.let {
                CardBackgroundSelector().selectPattern(it)
            } ?: R.drawable.abstract_surface_textures
            cardBackgroundIM.setImageResource(background)
            val deckColorCode = deck.deckColorCode?.let {
                DeckColorCategorySelector().selectColor(it)
            } ?: R.color.white
            cardRoot.setCardBackgroundColor(ContextCompat.getColor(context, deckColorCode))
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.timed_flash_card_item, parent, false)
                return ViewHolder(view)
            }
        }
    }

    fun getItems() = items
    fun setItems(items: List<ImmutableCard>) {
        this.items = items
    }

}