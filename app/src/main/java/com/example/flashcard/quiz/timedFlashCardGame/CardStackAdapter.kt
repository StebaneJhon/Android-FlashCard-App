package com.example.flashcard.quiz.timedFlashCardGame

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
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
    private val deck: ImmutableDeck,
    private val cardItem: (CardView, CardView) -> Unit
): RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(context, items[position], deck, cardItem)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val cardRoot: ConstraintLayout = view.findViewById(R.id.cardRootTF)

        private val cardBackgroundIMFront: ImageView = view.findViewById(R.id.cardBackgroundImgFront)
        private val languageHintFront: TextView = view.findViewById(R.id.languageHintFront)
        private val onCardTextFront: TextView = view.findViewById(R.id.onCardTextFront)
        private val onCardTextDefinitionFront: TextView = view.findViewById(R.id.onCardTextDefinitionFront)
        private val cardRootFront: CardView = view.findViewById(R.id.cardRootFront)

        private val cardBackgroundIMBack: ImageView = view.findViewById(R.id.cardBackgroundImgBack)
        private val languageHintBack: TextView = view.findViewById(R.id.languageHintBack)
        private val onCardTextBack: TextView = view.findViewById(R.id.onCardTextBack)
        private val onCardTextDefinitionBack: TextView = view.findViewById(R.id.onCardTextDefinitionBack)
        private val cardRootBack: CardView = view.findViewById(R.id.cardRootBack)

        fun bind(
            context: Context,
            card: ImmutableCard,
            deck: ImmutableDeck,
            cardItem: (CardView, CardView) -> Unit
        ) {
            initCardFront(deck, card, context)
            initCardBack(deck, card, context)

            cardRoot.setOnClickListener {
                cardItem(cardRootFront, cardRootBack)
            }

            /*
            Handler(Looper.getMainLooper()).postDelayed({
                cardItem(cardRootFront, cardRootBack)
            }, 5000)
             */

        }
        private fun initCardBack(
            deck: ImmutableDeck,
            card: ImmutableCard,
            context: Context
        ) {
            languageHintBack.text = deck.deckSecondLanguage
            onCardTextBack.text = card.cardDefinition
            onCardTextDefinitionBack.text = card.valueDefinition

            val backgroundBack = card.backgroundImg?.let {
                CardBackgroundSelector().selectPattern(it)
            } ?: R.drawable.abstract_surface_textures
            cardBackgroundIMBack.setImageResource(backgroundBack)
            val deckColorCodeBack = deck.deckColorCode?.let {
                DeckColorCategorySelector().selectColor(it)
            } ?: R.color.white
            cardRootBack.setCardBackgroundColor(ContextCompat.getColor(context, deckColorCodeBack))
        }

        private fun initCardFront(
            deck: ImmutableDeck,
            card: ImmutableCard,
            context: Context
        ) {
            languageHintFront.text = deck.deckFirstLanguage
            onCardTextFront.text = card.cardContent
            onCardTextDefinitionFront.text = card.contentDescription

            val backgroundFront = card.backgroundImg?.let {
                CardBackgroundSelector().selectPattern(it)
            } ?: R.drawable.abstract_surface_textures
            cardBackgroundIMFront.setImageResource(backgroundFront)
            val deckColorCodeFront = deck.deckColorCode?.let {
                DeckColorCategorySelector().selectColor(it)
            } ?: R.color.white
            cardRootFront.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    deckColorCodeFront
                )
            )
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