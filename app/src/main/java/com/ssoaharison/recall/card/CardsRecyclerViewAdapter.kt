package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.util.ItemLayoutManager.STAGGERED_GRID_LAYOUT_MANAGER
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel


class CardsRecyclerViewAdapter(
    private val context: Context,
    private val layoutManager: String,
    private val appTheme: String,
    private val deck: ImmutableDeck,
    private val cardList: List<ImmutableCard?>,
    private val boxLevels: List<ImmutableSpaceRepetitionBox>,
    private val editCardClickListener: (ImmutableCard?) -> Unit,
    private val deleteCardClickListener: (ImmutableCard?) -> Unit,
    private val onReadContent: (TextClickedModel) -> Unit,
    private val onReadDefinition: (TextClickedModel) -> Unit,
    private val onContentLanguageClicked: (RelativeLayout) -> Unit,
    private val onDefinitionLanguageClicked: (RelativeLayout) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = if (viewType == 0) {
        DeckDetailsViewHolder.create(parent)
    } else {
        CardViewHolder.create(parent)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (position == 0) {
            (holder as DeckDetailsViewHolder).bind(
                context,
                deck,
                onContentLanguageClicked,
                onDefinitionLanguageClicked
            )
            if (layoutManager == STAGGERED_GRID_LAYOUT_MANAGER) {
                val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
            } else { }
        } else {
            (holder as CardViewHolder).bind(
                context,
                appTheme,
                deck,
                cardList[position.minus(1)],
                boxLevels,
                editCardClickListener,
                deleteCardClickListener,
                onReadContent,
                onReadDefinition,
            )
        }

    override fun getItemCount(): Int {
        return cardList.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val onCardText: TextView = view.findViewById(R.id.tv_card_content)
        private val cardDescription1: TextView = view.findViewById(R.id.tv_card_description1)
        private val cardDescription2: TextView = view.findViewById(R.id.tv_card_description2)
        private val cardDescription3: TextView = view.findViewById(R.id.tv_card_description3)
        private val cardDescription4: TextView = view.findViewById(R.id.tv_card_description4)
        private val cardDescription5: TextView = view.findViewById(R.id.tv_card_description5)
        private val cardDescription6: TextView = view.findViewById(R.id.tv_card_description6)
        private val cardDescription7: TextView = view.findViewById(R.id.tv_card_description7)
        private val cardDescription8: TextView = view.findViewById(R.id.tv_card_description8)
        private val cardDescription9: TextView = view.findViewById(R.id.tv_card_description9)
        private val cardDescription10: TextView = view.findViewById(R.id.tv_card_description10)
        private val cardStatus: TextView = view.findViewById(R.id.tv_card_status)
        private val popUpBT: Button = view.findViewById(R.id.pupUpBT)
        private val cardRoot: MaterialCardView = view.findViewById(R.id.card_root)
        private val cvContainerCard: ConstraintLayout = view.findViewById(R.id.cl_container_card)
        private val cardDescriptionError: TextView =
            view.findViewById(R.id.tv_card_description_error)

        private val tvDescriptions = listOf(
            cardDescription1,
            cardDescription2,
            cardDescription3,
            cardDescription4,
            cardDescription5,
            cardDescription6,
            cardDescription7,
            cardDescription8,
            cardDescription9,
            cardDescription10,
        )

        private val ICON_MARGIN = 5

        fun bind(
            context: Context,
            appTheme: String,
            deck: ImmutableDeck,
            card: ImmutableCard?,
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            editCardClickListener: (ImmutableCard?) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            onReadContent: (TextClickedModel) -> Unit,
            onReadDefinition: (TextClickedModel) -> Unit,
        ) {

            if (appTheme == DARK_THEME) {
                onBrightTheme(boxLevels, card, context)
            } else {
                onDarkTheme(boxLevels, card, context)
            }

            val correctDefinition = getCorrectDefinition(card?.cardDefinition)
            val definitionTexts = cardDefinitionsToStrings(correctDefinition)

            cardRoot.setOnLongClickListener { v: View ->
                showMenu(
                    context,
                    v,
                    R.menu.card_popup_menu,
                    editCardClickListener,
                    deleteCardClickListener,
                    card
                )
                true
            }

            tvDescriptions.forEachIndexed { index, tv ->
                if (index < definitionTexts.size) {
                    tv.visibility = View.VISIBLE
                    cardDescriptionError.visibility = View.GONE
                    tv.text = definitionTexts[index]
                    tv.setOnClickListener {
                        it as TextView
                        val text = it.text.toString()
                        val language =
                            if (card?.cardDefinitionLanguage != null) card.cardDefinitionLanguage else deck.cardDefinitionDefaultLanguage
                        onReadDefinition(
                            TextClickedModel(
                                TextWithLanguageModel(
                                    card?.cardId!!,
                                    text,
                                    DEFINITION,
                                    language
                                ),
                                it,
                                DEFINITION
                            )
                        )
                    }
                    tv.setOnLongClickListener { v: View ->
                        showMenu(
                            context,
                            v,
                            R.menu.card_popup_menu,
                            editCardClickListener,
                            deleteCardClickListener,
                            card
                        )
                        true
                    }
                } else {
                    tv.visibility = View.GONE
                }
            }

            popUpBT.setOnClickListener { v: View ->
                showMenu(
                    context,
                    v,
                    R.menu.card_popup_menu,
                    editCardClickListener,
                    deleteCardClickListener,
                    card
                )
            }


            onCardText.apply {
                setOnClickListener {
                    it as TextView
                    val text = it.text.toString()
                    val language =
                        if (card?.cardContentLanguage != null) card.cardContentLanguage else deck.cardContentDefaultLanguage
                    onReadContent(
                        TextClickedModel(
                            TextWithLanguageModel(
                                card?.cardId!!,
                                text,
                                CONTENT,
                                language
                            ),
                            it,
                            CONTENT
                        )
                    )
                }
                setOnLongClickListener { v: View ->
                    showMenu(
                        context,
                        v,
                        R.menu.card_popup_menu,
                        editCardClickListener,
                        deleteCardClickListener,
                        card
                    )
                    true
                }
            }

        }

        private fun onDarkTheme(
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            card: ImmutableCard?,
            context: Context
        ) {
            val actualBoxLevel =
                SpaceRepetitionAlgorithmHelper().getBoxLevelByStatus(boxLevels, card?.cardStatus!!)
            val statusColor =
                SpaceRepetitionAlgorithmHelper().selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor =
                SpaceRepetitionAlgorithmHelper().selectBackgroundLevelColor(actualBoxLevel.levelColor)

            val colorStateList = ContextCompat.getColorStateList(context, statusColor!!)
            val cardBackgroundStateList =
                ContextCompat.getColorStateList(context, cardBackgroundStatusColor!!)
            cvContainerCard.backgroundTintList = cardBackgroundStateList
            cardStatus.apply {
                text = card.cardStatus
                backgroundTintList = colorStateList
            }
            onCardText.text = card.cardContent?.content
        }

        private fun onBrightTheme(
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            card: ImmutableCard?,
            context: Context
        ) {
            val actualBoxLevel =
                SpaceRepetitionAlgorithmHelper().getBoxLevelByStatus(boxLevels, card?.cardStatus!!)
            val statusColor =
                SpaceRepetitionAlgorithmHelper().selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor =
                SpaceRepetitionAlgorithmHelper().selectBackgroundLevelColor(actualBoxLevel.levelColor)

            val colorStateList = ContextCompat.getColorStateList(context, statusColor!!)
            val cardBackgroundStateList =
                ContextCompat.getColorStateList(context, cardBackgroundStatusColor!!)
            cvContainerCard.backgroundTintList = colorStateList
            cardStatus.apply {
                text = card.cardStatus
                backgroundTintList = cardBackgroundStateList
            }
            onCardText.apply {
                text = card.cardContent?.content
                setTextColor(context.getColor(R.color.white))
            }
            cardStatus.setTextColor(context.getColor(R.color.black))
        }

        private fun cardDefinitionsToStrings(cardDefinitions: List<CardDefinition>?): List<String> {
            val definitionStrings = mutableListOf<String>()
            cardDefinitions?.let { defins ->
                defins.forEach {
                    definitionStrings.add(it.definition)
                }
            }
            return definitionStrings
        }

        private fun getCorrectDefinition(definitions: List<CardDefinition>?): List<CardDefinition>? {
            definitions?.let { defins ->
                return defins.filter { it.isCorrectDefinition == 1 }
            }
            return null
        }

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            v: View,
            @MenuRes menuRes: Int,
            editCardClickListener: (ImmutableCard?) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            card: ImmutableCard?
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
                    R.id.edit_card_DM -> {
                        editCardClickListener(card)
                        true
                    }

                    R.id.delete_card_DM -> {
                        deleteCardClickListener(card)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

            popup.setOnDismissListener {
            }
            popup.show()

        }

        companion object {
            fun create(parent: ViewGroup): CardViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_card_fragment_item, parent, false)
                return CardViewHolder(view)
            }
        }
    }

    class DeckDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardSum: TextView = view.findViewById(R.id.tv_card_sum)
        private val known: TextView = view.findViewById(R.id.tv_known_card_sum)
        private val unknown: TextView = view.findViewById(R.id.tv_unknown_card_sum)
        private val contentLanguage: TextView = view.findViewById(R.id.bt_content_language)
        private val definitionLanguage: TextView = view.findViewById(R.id.bt_definition_language)
        private val contentLanguageContainer: RelativeLayout =
            view.findViewById(R.id.rl_container_content_language)
        private val definitionLanguageContainer: RelativeLayout =
            view.findViewById(R.id.rl_container_definition_language)

        fun bind(
            context: Context,
            deck: ImmutableDeck,
            onContentLanguageClicked: (RelativeLayout) -> Unit,
            onDefinitionLanguageClicked: (RelativeLayout) -> Unit
        ) {
            cardSum.text = "${deck.cardSum}"
            known.text = "${deck.knownCardCount}"
            unknown.text = "${deck.unKnownCardCount}"
            contentLanguage.apply {
                text = if (deck.cardContentDefaultLanguage.isNullOrBlank()) context.getString(R.string.text_content_language) else deck.cardContentDefaultLanguage
                setOnClickListener {
                    onContentLanguageClicked(contentLanguageContainer)
                }
            }
            contentLanguageContainer.setOnClickListener {
                onContentLanguageClicked(contentLanguageContainer)
            }
            definitionLanguage.apply {
                text = if (deck.cardDefinitionDefaultLanguage.isNullOrBlank()) context.getString(R.string.text_definition_language) else deck.cardDefinitionDefaultLanguage
                setOnClickListener {
                    onDefinitionLanguageClicked(definitionLanguageContainer)
                }
            }
            definitionLanguageContainer.setOnClickListener {
                onDefinitionLanguageClicked(definitionLanguageContainer)
            }
        }

        companion object {
            fun create(parent: ViewGroup): DeckDetailsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_deck_details, parent, false)
                return DeckDetailsViewHolder(view)
            }
        }
    }

}