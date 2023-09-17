package com.example.flashcard.quiz.baseFlashCardGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
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
import com.example.flashcard.quiz.timedFlashCardGame.CardStackAdapter
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector

class BaseFlashCardGameAdapter(
    private val context: Context,
    private var items: List<ImmutableCard>,
    private val deck: ImmutableDeck
): RecyclerView.Adapter<BaseFlashCardGameAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(context, items[position], deck)
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

        lateinit var front_anim: AnimatorSet
        lateinit var back_anim: AnimatorSet
        var isFront = true

        fun bind(
            context: Context,
            card: ImmutableCard,
            deck: ImmutableDeck,
        ) {
            initCardFront(deck, card, context)
            initCardBack(deck, card, context)
            val scale: Float = context.applicationContext.resources.displayMetrics.density
            cardRootFront.cameraDistance = 8000 * scale
            cardRootBack.cameraDistance = 8000 * scale


            front_anim = AnimatorInflater.loadAnimator(context.applicationContext, R.animator.front_animator) as AnimatorSet
            back_anim = AnimatorInflater.loadAnimator(context.applicationContext, R.animator.back_animator) as AnimatorSet

            cardRoot.setOnClickListener {
                flipCard()

            }

        }

        fun flipCard() {
            isFront = if (isFront) {
                front_anim.setTarget(cardRootFront)
                back_anim.setTarget(cardRootBack)
                front_anim.start()
                back_anim.start()
                false
            } else {
                front_anim.setTarget(cardRootBack)
                back_anim.setTarget(cardRootFront)
                back_anim.start()
                front_anim.start()
                true
            }
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