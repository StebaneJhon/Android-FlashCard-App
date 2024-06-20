package com.example.flashcard.profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.card.MaterialCardView

class ProfileFragmentDecksSectionRecyclerViewAdapter(
    private val listOfDecks: List<ImmutableDeck>,
    private val context: Context,
    private val deckClickListener: (ImmutableDeck) -> Unit
) : RecyclerView.Adapter<ProfileFragmentDecksSectionRecyclerViewAdapter.ViewHolder>() {

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
            deckClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val deckNameTV: TextView? = view.findViewById(R.id.deckNameTV)
        private val deckDescriptionTV: TextView? = view.findViewById(R.id.deckDescriptionTV)
        private val deckRoot: MaterialCardView? = view.findViewById(R.id.deckRoot)
        private val deckLanguages: TextView? = view.findViewById(R.id.languages)
        private val cardSum: TextView? = view.findViewById(R.id.cardsSum)
        private val popupMenuBT: Button? = view.findViewById(R.id.popup_menu_BT)
        private val startQuizButton: Button? = view.findViewById(R.id.startGameButton)

        private val ICON_MARGIN = 5


        @SuppressLint("ResourceAsColor")
        fun bind(
            deck: ImmutableDeck,
            context: Context,
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

            val deckColorCode = deck.deckColorCode?.let {
                DeckColorCategorySelector().selectColor(
                    it
                )
            } ?: R.color.red700
            popupMenuBT?.visibility = View.GONE
            startQuizButton?.visibility = View.GONE
            deckRoot?.setCardBackgroundColor(ContextCompat.getColor(context, deckColorCode))
            deckRoot?.setOnClickListener { deckClickListener(deck) }
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