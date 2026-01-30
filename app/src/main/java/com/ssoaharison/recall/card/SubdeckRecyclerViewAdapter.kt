package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.MenuRes
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.textview.MaterialTextView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME


class SubdeckRecyclerViewAdapter(
    private val listOfDecks: List<ExternalDeck>,
    private val context: Context,
    private val appTheme: String?,
    private val editDeckClickListener: (ExternalDeck) -> Unit,
    private val deleteDeckClickListener: (ExternalDeck) -> Unit,
    private val startQuizListener: (ExternalDeck) -> Unit,
    private val deckClickListener: (ExternalDeck) -> Unit
) : RecyclerView.Adapter<SubdeckRecyclerViewAdapter.ViewHolder>() {

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
            appTheme,
            editDeckClickListener,
            deleteDeckClickListener,
            startQuizListener,
            deckClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val deckNameTV: TextView? = view.findViewById(R.id.deckNameTV)
        private val deckRoot: ConstraintLayout? = view.findViewById(R.id.deckRoot)
        private val vwCardSum: TextView = view.findViewById(R.id.in_cards_sum)
        private val llCardSumContainer: LinearLayout = view.findViewById(R.id.ll_container_cards_sum)
        private val vwKnownCardSum: TextView = view.findViewById(R.id.in_known_cards_sum)
        private val llKnownCardSumContainer: LinearLayout = view.findViewById(R.id.ll_container_known_cards_sum)
        private val vwUnKnownCardSum: TextView = view.findViewById(R.id.in_un_known_cards_Sum)
        private val llUnKnownCardSumContainer: LinearLayout = view.findViewById(R.id.ll_container_un_known_cards_Sum)
        private val popupMenuBT: Button? = view.findViewById(R.id.popup_menu_BT)
        private val tvDeckCreationDate: TextView = view.findViewById(R.id.tv_deck_creation_date)
        private val divider: MaterialDivider = view.findViewById(R.id.divider)
        private val ivDeck: ImageView = view.findViewById(R.id.iv_deck)

        private val ICON_MARGIN = 5


        @SuppressLint("ResourceAsColor")
        fun bind(
            deck: ExternalDeck,
            context: Context,
            appTheme: String?,
            editDeckClickListener: (ExternalDeck) -> Unit,
            deleteDeckClickListener: (ExternalDeck) -> Unit,
            startQuizListener: (ExternalDeck) -> Unit,
            deckClickListener: (ExternalDeck) -> Unit
        ) {
            val deckColorHelper = DeckColorCategorySelector()

            val deckSurfaceColorCode: Int
            val deckTextColorCode: Int
            val deckSurfaceContainerColorCode: Int


            if (appTheme == DARK_THEME) {
                deckSurfaceColorCode = deckColorHelper.selectDeckDarkColorSurfaceContainerLowEst(context, deck.deckColorCode)
                deckTextColorCode = deckColorHelper.selectDeckOnSurfaceColorDark(context, deck.deckColorCode)
                deckSurfaceContainerColorCode = deckColorHelper.selectDeckDarkColorSurfaceContainerLow(context, deck.deckColorCode)
            } else {
                deckSurfaceColorCode = deckColorHelper.selectDeckColorSurfaceContainerLowEst(context, deck.deckColorCode)
                deckTextColorCode = deckColorHelper.selectDeckOnSurfaceColor(context, deck.deckColorCode)
                deckSurfaceContainerColorCode = deckColorHelper.selectDeckColorSurfaceContainerLow(context, deck.deckColorCode)
            }

            deckNameTV?.apply {
                text = deck.deckName
                setTextColor(deckTextColorCode)
            }

            if (deck.cardCount == 0) {
                llCardSumContainer.visibility = View.VISIBLE
                vwCardSum.text = context.getString(R.string.card_count, 0)
            } else {
                llCardSumContainer.visibility = View.GONE
            }

            if (deck.knownCardCount > 0) {
                llKnownCardSumContainer.visibility = View.VISIBLE
                vwKnownCardSum.text = context.getString(R.string.known_card_count, deck.knownCardCount)
                vwKnownCardSum.backgroundTintList = context.getColorStateList(R.color.green200)
                vwKnownCardSum.setTextColor(context.getColor(R.color.green950))
            } else {
                llKnownCardSumContainer.visibility = View.GONE
            }
            if (deck.unKnownCardCount > 0) {
                llUnKnownCardSumContainer.visibility = View.VISIBLE
                vwUnKnownCardSum.text = context.getString(R.string.un_known_card_count, deck.unKnownCardCount)
                vwUnKnownCardSum.backgroundTintList = context.getColorStateList(R.color.red200)
                vwUnKnownCardSum.setTextColor(context.getColor(R.color.red950))
            } else {
                llUnKnownCardSumContainer.visibility = View.GONE
            }

            if (deck.deckCreationDate != null) {
                tvDeckCreationDate.apply {
                    visibility = View.VISIBLE
                    text = deck.deckCreationDate.split(" ")[0]
                }
            } else {
                tvDeckCreationDate.visibility = View.GONE
            }

            deckRoot?.apply {
                setBackgroundColor(deckSurfaceColorCode)
//                setCardBackgroundColor(deckSurfaceColorCode)
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
            divider.dividerColor = deckSurfaceContainerColorCode
            ivDeck.setColorFilter(deckTextColorCode)
            ivDeck.backgroundTintList = ColorStateList.valueOf(
                deckSurfaceContainerColorCode
            )

        }

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            v: View,
            @MenuRes menuRes: Int,
            editDeckClickListener: (ExternalDeck) -> Unit,
            deleteDeckClickListener: (ExternalDeck) -> Unit,
            startQuizListener: (ExternalDeck) -> Unit,
            deck: ExternalDeck
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
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
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
            }
            popup.show()
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                //val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_adapter_deck_view, parent, false)
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_adapter_deck_view_3, parent, false)

                return ViewHolder(view)
            }
        }
    }
}