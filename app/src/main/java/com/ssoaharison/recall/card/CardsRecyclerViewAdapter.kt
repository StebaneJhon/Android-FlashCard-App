package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCard
import com.ssoaharison.recall.backend.models.ExternalCardDefinition
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.toLocal
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel
import com.ssoaharison.recall.util.ThemePicker


class CardsRecyclerViewAdapter(
    private val context: Context,
    private val appTheme: String,
    private val deck: ExternalDeck,
    private val cardList: List<ExternalCardWithContentAndDefinitions>,
    private val boxLevels: List<ImmutableSpaceRepetitionBox>,
    private val editCardClickListener: (ExternalCardWithContentAndDefinitions) -> Unit,
    private val deleteCardClickListener: (CardWithContentAndDefinitions) -> Unit,
    private val onReadContent: (TextClickedModel) -> Unit,
    private val onReadDefinition: (TextClickedModel) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardViewHolder.create(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder as CardViewHolder).bind(
                context,
                appTheme,
                deck,
                cardList[position],
                boxLevels,
                editCardClickListener,
                deleteCardClickListener,
                onReadContent,
                onReadDefinition,
            )

    override fun getItemCount(): Int {
        return cardList.size
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

        val spaceRepetitionAlgorithmHelper = SpaceRepetitionAlgorithmHelper()

        private val ICON_MARGIN = 5

        var contentTextColor: Int = R.color.red950
        var definitionTextColor: Int = R.color.red900

        fun bind(
            context: Context,
            appTheme: String,
            deck: ExternalDeck,
            card: ExternalCardWithContentAndDefinitions,
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            editCardClickListener: (ExternalCardWithContentAndDefinitions) -> Unit,
            deleteCardClickListener: (CardWithContentAndDefinitions) -> Unit,
            onReadContent: (TextClickedModel) -> Unit,
            onReadDefinition: (TextClickedModel) -> Unit,
        ) {

            if (appTheme == DARK_THEME) {
                this.onDarkTheme(boxLevels, card, context)
            } else {
                this.onBrightTheme(boxLevels, card, context)
            }

            val correctDefinition = getCorrectDefinition(card.contentWithDefinitions.definitions)
            val definitionTexts = cardDefinitionsToStrings(correctDefinition)

            cardRoot.setOnLongClickListener { v: View ->
                showMenu(
                    context,
                    appTheme,
                    v,
                    R.menu.card_popup_menu,
                    editCardClickListener,
                    deleteCardClickListener,
                    card,
                    deck.deckColorCode
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
                        val language = card.card.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
                        onReadDefinition(
                            TextClickedModel(
                                TextWithLanguageModel(card.card.cardId, text, DEFINITION, language),
                                it,
                                definitionTextColor,
                                DEFINITION
                            )
                        )
                    }
                    tv.setOnLongClickListener { v: View ->
                        showMenu(
                            context,
                            appTheme,
                            v,
                            R.menu.card_popup_menu,
                            editCardClickListener,
                            deleteCardClickListener,
                            card,
                            deck.deckColorCode
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
                    appTheme,
                    v,
                    R.menu.card_popup_menu,
                    editCardClickListener,
                    deleteCardClickListener,
                    card,
                    deck.deckColorCode
                )
            }


            onCardText.apply {
                setOnClickListener {
                    it as TextView
                    val text = it.text.toString()
                    val language =
                        card.card.cardContentLanguage ?: deck.cardContentDefaultLanguage
                    onReadContent(
                        TextClickedModel(
                            TextWithLanguageModel(card.card.cardId, text, CONTENT, language),
                            it,
                            contentTextColor,
                            CONTENT
                        )
                    )
                }
                setOnLongClickListener { v: View ->
                    showMenu(
                        context,
                        appTheme,
                        v,
                        R.menu.card_popup_menu,
                        editCardClickListener,
                        deleteCardClickListener,
                        card,
                        deck.deckColorCode
                    )
                    true
                }
            }

        }

        private fun onBrightTheme(
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            card: ExternalCardWithContentAndDefinitions?,
            context: Context
        ) {
            val actualBoxLevel = spaceRepetitionAlgorithmHelper.getBoxLevelByStatus(boxLevels, card?.card?.cardLevel!!)
            val statusColor = spaceRepetitionAlgorithmHelper.selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor = spaceRepetitionAlgorithmHelper.selectBackgroundLevelColor(actualBoxLevel.levelColor)
            contentTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColor(actualBoxLevel.levelColor)
            definitionTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorVariant(actualBoxLevel.levelColor)

            val colorStateList = ContextCompat.getColorStateList(context, statusColor)
            val cardBackgroundStateList =
                ContextCompat.getColorStateList(context, cardBackgroundStatusColor)
            cvContainerCard.backgroundTintList = cardBackgroundStateList
            cardStatus.apply {
                text = card.card.cardLevel
                backgroundTintList = colorStateList
            }
            onCardText.apply {
                text = card.contentWithDefinitions.content.contentText
                setTextColor(context.getColor(contentTextColor))
            }
            tvDescriptions.forEach {
                it.setTextColor(context.getColor(definitionTextColor))
            }
        }

        private fun onDarkTheme(
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            card: ExternalCardWithContentAndDefinitions,
            context: Context
        ) {
            val actualBoxLevel = spaceRepetitionAlgorithmHelper.getBoxLevelByStatus(boxLevels, card.card.cardLevel!!)
            val statusColor = spaceRepetitionAlgorithmHelper.selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor = spaceRepetitionAlgorithmHelper.selectBackgroundLevelColor(actualBoxLevel.levelColor)
            contentTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorLight(actualBoxLevel.levelColor)
            definitionTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorLightVariant(actualBoxLevel.levelColor)

            val colorStateList = ContextCompat.getColorStateList(context, statusColor)
            val cardBackgroundStateList = ContextCompat.getColorStateList(context, cardBackgroundStatusColor)
            cvContainerCard.backgroundTintList = colorStateList
            cardStatus.apply {
                text = card.card.cardLevel
                backgroundTintList = cardBackgroundStateList
            }
            onCardText.apply {
                text = card.contentWithDefinitions.content.contentText
                setTextColor(context.getColor(contentTextColor))
            }
            tvDescriptions.forEach {
                it.setTextColor(context.getColor(definitionTextColor))
            }
            cardStatus.setTextColor(context.getColor(R.color.black))
        }

        private fun cardDefinitionsToStrings(cardDefinitions: List<ExternalCardDefinition>?): List<String> {
            val definitionStrings = mutableListOf<String>()
            cardDefinitions?.let { defins ->
                defins.forEach {
                    definitionStrings.add(it.definitionText!!)
                }
            }
            return definitionStrings
        }

        private fun getCorrectDefinition(definitions: List<ExternalCardDefinition>?): List<ExternalCardDefinition>? {
            definitions?.let { defins ->
                return defins.filter { it.isCorrectDefinition == 1 }
            }
            return null
        }

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            appTheme: String,
            v: View,
            @MenuRes menuRes: Int,
            editCardClickListener: (ExternalCardWithContentAndDefinitions) -> Unit,
            deleteCardClickListener: (CardWithContentAndDefinitions) -> Unit,
            card: ExternalCardWithContentAndDefinitions,
            deckThemeName: String?,
        ) {
            val themePicker = ThemePicker()
            val popup = if (deckThemeName.isNullOrBlank()) {
                PopupMenu(context, v)
            } else {
                val defaultTheme = themePicker.selectTheme(appTheme) ?: themePicker.getDefaultTheme()
                val deckTheme = if (appTheme == DARK_THEME) {
                    ThemePicker().selectDarkThemeByDeckColorCode(deckThemeName, defaultTheme)
                } else {
                    ThemePicker().selectThemeByDeckColorCode(deckThemeName, defaultTheme)
                }
                val wrapper = ContextThemeWrapper(context, deckTheme)
                PopupMenu(wrapper, v)
            }

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
                        val localCard = CardWithContentAndDefinitions(
                            card = card.card.toLocal(),
                            contentWithDefinitions = CardContentWithDefinitions(
                                content = card.contentWithDefinitions.content.toLocal(),
                                definitions = card.contentWithDefinitions.definitions.map { definition -> definition.toLocal() }
                            )
                        )
                        deleteCardClickListener(localCard)
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

}