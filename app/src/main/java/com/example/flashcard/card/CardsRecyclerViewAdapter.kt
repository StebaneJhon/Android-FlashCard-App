package com.example.flashcard.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.google.android.material.card.MaterialCardView


class CardsRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<Card>,
    private val deck: ImmutableDeck,
    private val fullScreenClickListener: (Card) -> Unit,
    private val editCardClickListener: (Card) -> Unit,
    private val deleteCardClickListener: (Card) -> Unit,
) : RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            deck,
            fullScreenClickListener,
            editCardClickListener,
            deleteCardClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var isCardRevealed = false

        private val onCardText: TextView = view.findViewById(R.id.tv_card_content)
        private val cardDescription: TextView = view.findViewById(R.id.tv_card_description)
        private val cardStatus: TextView = view.findViewById(R.id.tv_card_status)
        private val popUpBT: ImageButton = view.findViewById(R.id.pupUpBT)
        private val cardRoot: MaterialCardView = view.findViewById(R.id.card_root)

        private val ICON_MARGIN = 5

        fun bind(
            context: Context,
            card: Card,
            deck: ImmutableDeck,
            fullScreenClickListener: (Card) -> Unit,
            editCardClickListener: (Card) -> Unit,
            deleteCardClickListener: (Card) -> Unit
        ) {
            val statusColor = SpaceRepetitionAlgorithmHelper().box[card.cardStatus]?.color
            val colorStateList = ContextCompat.getColorStateList(context, statusColor!!)
            cardStatus.apply {
                text = card.cardStatus
                backgroundTintList = colorStateList
            }
            onCardText.text = card.cardContent
            cardDescription.text = card.cardDefinition

            popUpBT.setOnClickListener { v: View ->
                showMenu(
                    context,
                    v,
                    R.menu.card_popup_menu,
                    fullScreenClickListener,
                    editCardClickListener,
                    deleteCardClickListener,
                    card
                )
            }
            cardRoot.setOnClickListener {
                //flipCard(card, deck)
            }

        }

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            v: View,
            @MenuRes menuRes: Int,
            fullScreenClickListener: (Card) -> Unit,
            editCardClickListener: (Card) -> Unit,
            deleteCardClickListener: (Card) -> Unit,
            card: Card
            ) {

            val popup = PopupMenu(context, v)
            popup.menuInflater.inflate(menuRes, popup.menu)

            if (popup.menu is MenuBuilder) {
                val menuBuilder = popup.menu as MenuBuilder
                menuBuilder.setOptionalIconsVisible(true)
                for (item in menuBuilder.visibleItems) {
                    val iconMarginPx =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            ICON_MARGIN.toFloat(),
                            context.resources.displayMetrics
                        )
                            .toInt()
                    if (item.icon != null) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                            item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                        } else {
                            item.icon =
                                object :
                                    InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                    override fun getIntrinsicWidth(): Int {
                                        return intrinsicHeight + iconMarginPx + iconMarginPx
                                    }
                                }
                        }
                    }
                }
            }

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit_card_DM -> {
                        editCardClickListener(card)
                        true
                    }
                    R.id.delete_card_DM -> {
                        deleteCardClickListener(card)
                        true
                    }
                    R.id.fullscrean_card_DM -> {
                        fullScreenClickListener(card)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            popup.setOnDismissListener {
                // Respond to popup being dismissed.
            }
            // Show the popup menu.
            popup.show()

        }

        /*
        private fun flipCard(card: Card, deck: ImmutableDeck) {
            if (!isCardRevealed) {
                languageHint.text = deck.deckSecondLanguage
                onCardText.text = card.cardDefinition
                onCardTextDescription.text = card.valueDefinition
                isCardRevealed = true
            } else {
                languageHint.text = deck.deckFirstLanguage
                onCardText.text = card.cardContent
                onCardTextDescription.text = card.contentDescription
                isCardRevealed = false
            }
        }

         */

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_card_fragment_item, parent, false)
                return ViewHolder(view)
            }
        }
    }

}