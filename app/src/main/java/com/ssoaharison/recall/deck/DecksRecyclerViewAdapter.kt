package com.ssoaharison.recall.deck

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.google.android.material.card.MaterialCardView

class DecksRecyclerViewAdapter(
    private val listOfDecks: List<ImmutableDeck>,
    private val context: Context,
    private val editDeckClickListener: (ImmutableDeck) -> Unit,
    private val deleteDeckClickListener: (ImmutableDeck) -> Unit,
    private val startQuizListener: (ImmutableDeck) -> Unit,
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
            startQuizListener,
            deckClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val deckNameTV: TextView? = view.findViewById(R.id.deckNameTV)
        private val deckDescriptionTV: TextView? = view.findViewById(R.id.deckDescriptionTV)
        private val deckRoot: MaterialCardView? = view.findViewById(R.id.deckRoot)
        private val deckContendLanguages: TextView? = view.findViewById(R.id.tv_content_language)
        private val deckDefinitionLanguages: TextView? = view.findViewById(R.id.tv_definition_language)
        private val cardSum: TextView? = view.findViewById(R.id.cardsSum)
        private val tvKnownCardSum: TextView? = view.findViewById(R.id.tv_known_cards_Sum)
        private val tvUnKnownCardSum: TextView? = view.findViewById(R.id.tv_un_known_cards_Sum)
        private val popupMenuBT: Button? = view.findViewById(R.id.popup_menu_BT)
        private val ICON_MARGIN = 5


        @SuppressLint("ResourceAsColor")
        fun bind(
            deck: ImmutableDeck,
            context: Context,
            editDeckClickListener: (ImmutableDeck) -> Unit,
            deleteDeckClickListener: (ImmutableDeck) -> Unit,
            startQuizListener: (ImmutableDeck) -> Unit,
            deckClickListener: (ImmutableDeck) -> Unit
        ) {
            deckNameTV?.text = deck.deckName
            deckDescriptionTV?.text = deck.deckDescription
            deckContendLanguages?.text = deck.cardContentDefaultLanguage
            deckDefinitionLanguages?.text = deck.cardDefinitionDefaultLanguage
            cardSum?.text = deck.cardSum.toString()
            if (deck.knownCardCount!! > 0) {
                tvKnownCardSum?.visibility = View.VISIBLE
                tvKnownCardSum?.text = deck.knownCardCount.toString()
            } else {
                tvKnownCardSum?.visibility = View.GONE
            }
            if (deck.unKnownCardCount!! > 0) {
                tvUnKnownCardSum?.visibility = View.VISIBLE
                tvUnKnownCardSum?.text = deck.unKnownCardCount.toString()
            } else {
                tvUnKnownCardSum?.visibility = View.GONE
            }

            val deckColorCode = deck.deckColorCode?.let {
                DeckColorCategorySelector().selectColor(
                    it
                )
            } ?: R.color.red700

            deckRoot?.apply {
                setCardBackgroundColor(ContextCompat.getColor(context, deckColorCode))
                setOnLongClickListener { v: View ->
                    showMenu(
                        context,
                        v,
                        R.menu.deck_popup_menu,
                        editDeckClickListener,
                        deleteDeckClickListener,
                        startQuizListener,
                        deck
                    )
                    true
                }
                setOnClickListener { deckClickListener(deck) }
            }

            popupMenuBT?.setOnClickListener { v: View ->
                showMenu(
                    context,
                    v,
                    R.menu.deck_popup_menu,
                    editDeckClickListener,
                    deleteDeckClickListener,
                    startQuizListener,
                    deck
                )
            }
        }

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            v: View,
            @MenuRes menuRes: Int,
            editDeckClickListener: (ImmutableDeck) -> Unit,
            deleteDeckClickListener: (ImmutableDeck) -> Unit,
            startQuizListener: (ImmutableDeck) -> Unit,
            deck: ImmutableDeck
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
                     R.id.edit_deck_DM -> {
                        editDeckClickListener(deck)
                        true
                    }
                    R.id.delete_deck_DM -> {
                        deleteDeckClickListener(deck)
                        true
                    }
                    R.id.start_quiz_DM -> {
                        startQuizListener(deck)
                        true
                    }
                    else -> false
                }

            }
            popup.setOnDismissListener {
                // Respond to popup being dismissed.
            }
            // Show the popup menu.
            popup.show()
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