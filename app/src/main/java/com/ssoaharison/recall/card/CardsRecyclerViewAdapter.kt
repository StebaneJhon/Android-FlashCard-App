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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardDefinition
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.toLocal
import com.ssoaharison.recall.databinding.LyAudioPlayerBinding
import com.ssoaharison.recall.databinding.LyCardDefinitionBinding
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.playback.AndroidAudioPlayer
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
    private val onPlayAudio: (AudioModel, LinearLayout) -> Unit,
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
                onPlayAudio,
            )

    override fun getItemCount(): Int {
        return cardList.size
    }

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val content: View = view.findViewById(R.id.in_content)
        private val cardDefinition1: View = view.findViewById(R.id.in_definition_1)
        private val cardDefinition2: View = view.findViewById(R.id.in_definition_2)
        private val cardDefinition3: View = view.findViewById(R.id.in_definition_3)
        private val cardDefinition4: View = view.findViewById(R.id.in_definition_4)
        private val cardDefinition5: View = view.findViewById(R.id.in_definition_5)
        private val cardDefinition6: View = view.findViewById(R.id.in_definition_6)
        private val cardDefinition7: View = view.findViewById(R.id.in_definition_7)
        private val cardDefinition8: View = view.findViewById(R.id.in_definition_8)
        private val cardDefinition9: View = view.findViewById(R.id.in_definition_9)
        private val cardDefinition10: View = view.findViewById(R.id.in_definition_10)
//        private val cardStatus: TextView = view.findViewById(R.id.tv_card_status)
        private val cardStatus: LinearLayout = view.findViewById(R.id.in_level)
        private val popUpBT: Button = view.findViewById(R.id.pupUpBT)
        private val cardRoot: MaterialCardView = view.findViewById(R.id.card_root)
        private val cvContainerCard: ConstraintLayout = view.findViewById(R.id.cl_container_card)
        private val cardDescriptionError: TextView =
            view.findViewById(R.id.tv_card_description_error)

        private val descriptionBinding = listOf(
            cardDefinition1,
            cardDefinition2,
            cardDefinition3,
            cardDefinition4,
            cardDefinition5,
            cardDefinition6,
            cardDefinition7,
            cardDefinition8,
            cardDefinition9,
            cardDefinition10,
        )
        private val descriptionContainer = listOf(
            view.findViewById<FrameLayout>(R.id.container_definition_1),
            view.findViewById<FrameLayout>(R.id.container_definition_2),
            view.findViewById<FrameLayout>(R.id.container_definition_3),
            view.findViewById<FrameLayout>(R.id.container_definition_4),
            view.findViewById<FrameLayout>(R.id.container_definition_5),
            view.findViewById<FrameLayout>(R.id.container_definition_6),
            view.findViewById<FrameLayout>(R.id.container_definition_7),
            view.findViewById<FrameLayout>(R.id.container_definition_8),
            view.findViewById<FrameLayout>(R.id.container_definition_9),
            view.findViewById<FrameLayout>(R.id.container_definition_10),
        )

        private val spaceRepetitionAlgorithmHelper = SpaceRepetitionAlgorithmHelper()

        private val ICON_MARGIN = 5

//        var contentTextColor: Int = R.color.red950
//        var definitionTextColor: Int = R.color.red900

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
            onPlayAudio: (AudioModel, LinearLayout) -> Unit
        ) {

            if (appTheme == DARK_THEME) {
                this.onDarkTheme(boxLevels, card, context, onPlayAudio)
            } else {
                this.onBrightTheme(boxLevels, card, context, onPlayAudio)
            }

            val correctDefinition = getCorrectDefinition(card.contentWithDefinitions.definitions)
//            val definitionTexts = cardDefinitionsToStrings(correctDefinition)

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

            descriptionBinding.forEachIndexed { index, descriptionView ->
                if (index <  correctDefinition!!.size) {
                    descriptionContainer[index].visibility = View.VISIBLE
                    cardDescriptionError.visibility = View.GONE
                    if (correctDefinition[index].definitionText != null) {
                        val tvDefinition: TextView = descriptionView.findViewById(R.id.tv_definition)
                        tvDefinition.text = correctDefinition[index].definitionText
                    }

                    if ( card.contentWithDefinitions.definitions[index].definitionImage != null) {
                        val imageView: ImageView = descriptionView.findViewById(R.id.img_photo)
                        imageView.apply {
                            setImageBitmap(correctDefinition[index].definitionImage?.bmp)
                            visibility = View.VISIBLE
                        }
                    }

                    val audioContainer: LinearLayout = descriptionView.findViewById(R.id.ll_definition_container_audio)
//                    val btPlayDefinitionAudio: MaterialButton = descriptionView.findViewById(R.id.bt_play_audio)
                    val btPlayDefinitionAudio: MaterialButton = descriptionView.findViewById(R.id.bt_play)
                    val lpiAudioProgression: LinearLayout? = descriptionView.findViewById(R.id.ly_content_audio)
                    if (card.contentWithDefinitions.definitions[index].definitionAudio != null) {
                        audioContainer.visibility = View.VISIBLE
                        btPlayDefinitionAudio.setOnClickListener {
                            onPlayAudio(card.contentWithDefinitions.definitions[index].definitionAudio!!, lpiAudioProgression ?: return@setOnClickListener)
                        }
                    } else {
                        audioContainer.visibility = View.GONE
                    }


//                    descriptionView.text = definitionTexts[index]
//                    descriptionView.setOnClickListener {
//                        it as TextView
//                        val text = it.text.toString()
//                        val language = card.card.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
//                        onReadDefinition(
//                            TextClickedModel(
//                                TextWithLanguageModel(card.card.cardId, text, DEFINITION, language),
//                                it,
//                                definitionTextColor,
//                                DEFINITION
//                            )
//                        )
//                    }
//                    descriptionView.setOnLongClickListener { v: View ->
//                        showMenu(
//                            context,
//                            appTheme,
//                            v,
//                            R.menu.card_popup_menu,
//                            editCardClickListener,
//                            deleteCardClickListener,
//                            card,
//                            deck.deckColorCode
//                        )
//                        true
//                    }
                } else {
//                    descriptionView.visibility = View.GONE
                    descriptionContainer[index].visibility = View.GONE
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


            content.apply {
                setOnClickListener {
                    it as TextView
                    val text = it.text.toString()
                    val language =
                        card.card.cardContentLanguage ?: deck.cardContentDefaultLanguage
                    onReadContent(
                        TextClickedModel(
                            TextWithLanguageModel(card.card.cardId, text, CONTENT, language),
                            it,
                            R.color.red950,
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
            context: Context,
            onPlayAudio: (AudioModel, LinearLayout) -> Unit
        ) {
            val actualBoxLevel = spaceRepetitionAlgorithmHelper.getBoxLevelByStatus(boxLevels, card?.card?.cardLevel!!)
            val statusColor = spaceRepetitionAlgorithmHelper.selectBoxLevelColor(actualBoxLevel?.levelColor!!)
//            val cardBackgroundStatusColor = spaceRepetitionAlgorithmHelper.selectBackgroundLevelColor(actualBoxLevel.levelColor)
//            contentTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColor(actualBoxLevel.levelColor)
//            definitionTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorVariant(actualBoxLevel.levelColor)

            val colorStateList = ContextCompat.getColorStateList(context, statusColor)
//            val cardBackgroundStateList = ContextCompat.getColorStateList(context, cardBackgroundStatusColor)
//            cvContainerCard.backgroundTintList = cardBackgroundStateList
            cardStatus.apply {
                findViewById<View>(R.id.vw_level_color).backgroundTintList = colorStateList
                findViewById<TextView>(R.id.tv_level_text).text = context.getString(R.string.card_level, card.card.cardLevel)
//                text = card.card.cardLevel
//                backgroundTintList = colorStateList
            }
            if (card.contentWithDefinitions.content.contentAudio != null) {
                content.findViewById<LinearLayout>(R.id.ll_content_container_audio).visibility = View.VISIBLE
//                content.findViewById<MaterialButton>(R.id.bt_play_audio).setOnClickListener {
//                    onPlayAudio(card.contentWithDefinitions.content.contentAudio)
//                }
                content.findViewById<MaterialButton>(R.id.bt_play).setOnClickListener {
                    onPlayAudio(card.contentWithDefinitions.content.contentAudio, content.findViewById<LinearLayout>(R.id.ly_content_audio))
                }
            }

            if (card.contentWithDefinitions.content.contentText != null) {
                content.findViewById<TextView>(R.id.tv_content).apply {
                    text = card.contentWithDefinitions.content.contentText
//                    setTextColor(context.getColor(contentTextColor))
                }
            }
            if (card.contentWithDefinitions.content.contentImage != null) {
                content.findViewById<ImageView>(R.id.img_photo).apply {
                    setImageBitmap(card.contentWithDefinitions.content.contentImage.bmp)
                    visibility = View.VISIBLE
                }
            }
//            descriptionBinding.forEach {
//                it.findViewById<TextView>(R.id.tv_definition).setTextColor(context.getColor(definitionTextColor))
//            }
        }

        private fun onDarkTheme(
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            card: ExternalCardWithContentAndDefinitions,
            context: Context,
            onPlayAudio: (AudioModel, LinearLayout) -> Unit
        ) {
            val actualBoxLevel = spaceRepetitionAlgorithmHelper.getBoxLevelByStatus(boxLevels, card.card.cardLevel!!)
//            val statusColor = spaceRepetitionAlgorithmHelper.selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor = spaceRepetitionAlgorithmHelper.selectBoxLevelColor(actualBoxLevel?.levelColor!!)
//            contentTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorLight(actualBoxLevel.levelColor)
//            definitionTextColor = spaceRepetitionAlgorithmHelper.selectOnSurfaceColorLightVariant(actualBoxLevel.levelColor)

//            val colorStateList = ContextCompat.getColorStateList(context, statusColor)
            val cardBackgroundStateList = ContextCompat.getColorStateList(context, cardBackgroundStatusColor)
//            cvContainerCard.backgroundTintList = colorStateList
            cardStatus.apply {
                findViewById<View>(R.id.vw_level_color).backgroundTintList = cardBackgroundStateList
                findViewById<TextView>(R.id.tv_level_text).text = context.getString(R.string.card_level, card.card.cardLevel)
//                text = context.getString(R.string.card_level, card.card.cardLevel)
//                backgroundTintList = cardBackgroundStateList
            }

            // TODO: Include Audio
            if (card.contentWithDefinitions.content.contentAudio != null) {
                content.findViewById<LinearLayout>(R.id.ll_content_container_audio).visibility = View.VISIBLE
//                content.findViewById<MaterialButton>(R.id.bt_play_audio).setOnClickListener {
//                    onPlayAudio(card.contentWithDefinitions.content.contentAudio)
//                }
                content.findViewById<MaterialButton>(R.id.bt_play).setOnClickListener {
                    onPlayAudio(card.contentWithDefinitions.content.contentAudio, content.findViewById<LinearLayout>(R.id.ly_content_audio))
                }
            }
            if (card.contentWithDefinitions.content.contentText != null) {
                content.findViewById<TextView>(R.id.tv_content).apply {
                    text = card.contentWithDefinitions.content.contentText
//                    setTextColor(context.getColor(contentTextColor))
                }
            }
            if (card.contentWithDefinitions.content.contentImage != null) {
                content.findViewById<ImageView>(R.id.img_photo).apply {
                    setImageBitmap(card.contentWithDefinitions.content.contentImage.bmp)
                    visibility = View.VISIBLE
                }
            }

//            descriptionBinding.forEach {
//                it.findViewById<TextView>(R.id.tv_definition).setTextColor(context.getColor(definitionTextColor))
//            }
//            cardStatus.setTextColor(context.getColor(R.color.black))
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