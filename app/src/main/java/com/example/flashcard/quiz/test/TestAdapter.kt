package com.example.flashcard.quiz.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.quiz.quizGame.QuizSpeakModel
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class TestAdapter(
    val context: Context,
    val cardList: List<TestCardModel?>,
    val deck: ImmutableDeck,
    private val onUserAnswered: (TestCardDefinitionModel) -> Unit,
    private val onSpeak: (QuizSpeakModel) -> Unit
) : RecyclerView.Adapter<TestAdapter.ViewHolder>() {

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
            position,
            cardList.size,
            onUserAnswered,
            onSpeak
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val flCardRoot: FrameLayout = view.findViewById(R.id.fl_card_root)
        private val cvCardContainer: MaterialCardView = view.findViewById(R.id.cv_card_container)
        private val tvContent: TextView = view.findViewById(R.id.tv_content)
        private val btAlternative1: MaterialButton = view.findViewById(R.id.bt_alternative1)
        private val btAlternative2: MaterialButton = view.findViewById(R.id.bt_alternative2)
        private val btAlternative3: MaterialButton = view.findViewById(R.id.bt_alternative3)
        private val btAlternative4: MaterialButton = view.findViewById(R.id.bt_alternative4)
        private val btAlternative5: MaterialButton = view.findViewById(R.id.bt_alternative5)
        private val btAlternative6: MaterialButton = view.findViewById(R.id.bt_alternative6)
        private val btAlternative7: MaterialButton = view.findViewById(R.id.bt_alternative7)
        private val btAlternative8: MaterialButton = view.findViewById(R.id.bt_alternative8)
        private val btAlternative9: MaterialButton = view.findViewById(R.id.bt_alternative9)
        private val btAlternative10: MaterialButton = view.findViewById(R.id.bt_alternative10)
        private val tvFrontProgression: TextView = view.findViewById(R.id.tv_front_progression)
        private val tvCardType: TextView = view.findViewById(R.id.tv_card_type)
        private val btSpeak: MaterialButton = view.findViewById(R.id.bt_speak)
        private val tvDefinition: TextView = view.findViewById(R.id.tv_definition)

        private val btAlternatives = listOf(
            btAlternative1, btAlternative2, btAlternative3, btAlternative4, btAlternative5,
            btAlternative6, btAlternative7, btAlternative8, btAlternative9, btAlternative10,
        )


        fun bind(
            context: Context,
            card: TestCardModel?,
            deck: ImmutableDeck,
            cardPosition: Int,
            cardSum: Int,
            onUserAnswered: (TestCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit
        ) {
            tvFrontProgression.text = context.getString(
                R.string.tx_flash_card_game_progression,
                "${cardPosition.plus(1)}",
                "$cardSum"
            )
            tvContent.text = card?.cardContent?.content
            tvCardType.text = card?.cardType
            val deckColor =
                DeckColorCategorySelector().selectColor(deck.deckColorCode!!) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < card?.cardDefinition?.size!!) {
                    materialButton.visibility = View.VISIBLE
                    if (card.cardDefinition[index].isSelected) {
                        onButtonClicked(materialButton, card.cardType, context)
                    } else {
                        onButtonUnClicked(materialButton, card.cardType, context)
                    }
                } else {
                    materialButton.visibility = View.GONE
                }
            }

            /*
            card?.cardDefinition?.forEachIndexed {index, definition ->
                when (index) {
                    0 -> {
                        if (definition.isSelected) {
                            onButtonClicked(btAlternative1, card.cardType, context)
                        } else {
                            onButtonUnClicked(btAlternative1, card.cardType, context)
                        }
                    }
                    1 -> {
                        if (definition.isSelected) {
                            onButtonClicked(btAlternative2, card.cardType, context)
                        } else {
                            onButtonUnClicked(btAlternative2, card.cardType, context)
                        }
                    }
                    2 -> {
                        if (definition.isSelected) {
                            onButtonClicked(btAlternative3, card.cardType, context)
                        } else {
                            onButtonUnClicked(btAlternative3, card.cardType, context)
                        }
                    }
                    3 -> {
                        if (definition.isSelected) {
                            onButtonClicked(btAlternative4, card.cardType, context)
                        } else {
                            onButtonUnClicked(btAlternative4, card.cardType, context)
                        }
                    }
                }
            }

             */

            when (card?.cardType) {
                SINGLE_ANSWER_CARD -> {
                    onOneAnsweredCard(context, card, onUserAnswered, onSpeak)
                }

                TRUE_OR_FALSE_CARD -> {
                    onTrueOrFalseCard(context, card, onUserAnswered, onSpeak)
                }

                MULTIPLE_ANSWER_CARD -> {
                    onMultipleAnsweredCard(context, card, onUserAnswered, onSpeak)
                }

                else -> {
                    tvDefinition.text = card?.cardDefinition?.first()?.definition
                }
            }

        }

        fun onOneAnsweredCard(
            context: Context,
            card: TestCardModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit
        ) {

            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < card.cardDefinition.size) {
                    materialButton.apply {
                        visibility = View.VISIBLE
                        text = card.cardDefinition[index].definition
                        setOnClickListener { v ->
                            selectAnswer(card.cardDefinition[index], onUserAnswered)
                        }
                    }
                }
            }
            /*
            btAlternative1.apply {
                isVisible = true
                text = card.cardDefinition[0].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[0], onUserAnswered)
                }
            }
            btAlternative2.apply {
                isVisible = true
                text = card.cardDefinition[1].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[1], onUserAnswered)
                }
            }
            btAlternative3.apply {
                isVisible = true
                text = card.cardDefinition[2].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[2], onUserAnswered)
                }
            }
            btAlternative4.apply {
                isVisible = true
                text = card.cardDefinition[3].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[3], onUserAnswered)
                }
            }
             */

            btSpeak.setOnClickListener {
                val texts = listOf(
                    card.cardContent.content,
                    card.cardDefinition[0].definition,
                    card.cardDefinition[1].definition,
                    card.cardDefinition[2].definition,
                    card.cardDefinition[3].definition,
                )
                val views = listOf(
                    tvContent,
                    btAlternative1,
                    btAlternative2,
                    btAlternative3,
                    btAlternative4,
                )

                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        ""
                    )
                )
            }
        }

        fun onMultipleAnsweredCard(
            context: Context,
            card: TestCardModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit
        ) {
            val texts = arrayListOf(card.cardContent.content,)
            val views = arrayListOf(tvContent)
            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < card.cardDefinition.size) {
                    materialButton.apply {
                        visibility = View.VISIBLE
                        text = card.cardDefinition[index].definition
                        setOnClickListener { v ->
                            selectAnswer(card.cardDefinition[index], onUserAnswered)
                        }
                    }
                    texts.add(card.cardDefinition[index].toString())
                    views.add(materialButton)
                } else {
                    materialButton.visibility = View.GONE
                }
            }

            onSpeak(
                QuizSpeakModel(
                    text = texts,
                    views = views,
                    ""
                )
            )
            /*
            when (card.cardDefinition.size) {
                2 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = card.cardDefinition[0].definition
                        setOnClickListener { v ->
                            selectAnswer(card.cardDefinition[0], onUserAnswered)
                        }
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = card.cardDefinition[1].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[1], onUserAnswered)
                        }
                    }
                    btAlternative3.isVisible = false
                    btAlternative4.isVisible = false

                    btSpeak.setOnClickListener {
                        val texts = listOf(
                            card.cardContent.content,
                            card.cardDefinition[0].definition,
                            card.cardDefinition[1].definition,
                        )
                        val views = listOf(
                            tvContent,
                            btAlternative1,
                            btAlternative2,
                        )

                        onSpeak(
                            QuizSpeakModel(
                                text = texts,
                                views = views,
                                ""
                            )
                        )
                    }

                }

                3 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = card.cardDefinition[0].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[0], onUserAnswered)
                        }
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = card.cardDefinition[1].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[1], onUserAnswered)
                        }
                    }
                    btAlternative3.apply {
                        isVisible = true
                        text = card.cardDefinition[2].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[2], onUserAnswered)
                        }
                    }
                    btAlternative4.isVisible = false

                    btSpeak.setOnClickListener {
                        val texts = listOf(
                            card.cardContent.content,
                            card.cardDefinition[0].definition,
                            card.cardDefinition[1].definition,
                            card.cardDefinition[2].definition,
                        )
                        val views = listOf(
                            tvContent,
                            btAlternative1,
                            btAlternative2,
                            btAlternative3,
                        )

                        onSpeak(
                            QuizSpeakModel(
                                text = texts,
                                views = views,
                                ""
                            )
                        )
                    }

                }

                4 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = card.cardDefinition[0].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[0], onUserAnswered)
                        }
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = card.cardDefinition[1].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[1], onUserAnswered)
                        }
                    }
                    btAlternative3.apply {
                        isVisible = true
                        text = card.cardDefinition[2].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[2], onUserAnswered)
                        }
                    }
                    btAlternative4.apply {
                        isVisible = true
                        text = card.cardDefinition[3].definition
                        setOnClickListener { v ->
//                            onButtonClicked(v as MaterialButton, context)
                            selectAnswer(card.cardDefinition[3], onUserAnswered)
                        }
                    }

                    btSpeak.setOnClickListener {
                        val texts = listOf(
                            card.cardContent.content,
                            card.cardDefinition[0].definition,
                            card.cardDefinition[1].definition,
                            card.cardDefinition[2].definition,
                            card.cardDefinition[3].definition,
                        )
                        val views = listOf(
                            tvContent,
                            btAlternative1,
                            btAlternative2,
                            btAlternative3,
                            btAlternative4,
                        )

                        onSpeak(
                            QuizSpeakModel(
                                text = texts,
                                views = views,
                                ""
                            )
                        )
                    }

                }
            }
            */
        }

        private fun selectAnswer(
            selectedAnswer: TestCardDefinitionModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit
        ) {
            selectedAnswer.isSelected = !selectedAnswer.isSelected
            onUserAnswered(selectedAnswer)
        }

        fun onTrueOrFalseCard(
            context: Context,
            card: TestCardModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit
        ) {
            btAlternative1.apply {
                isVisible = true
                text = card.cardDefinition[0].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[0], onUserAnswered)
                }
            }
            btAlternative2.apply {
                isVisible = true
                text = card.cardDefinition[1].definition
                setOnClickListener { v ->
//                    onButtonClicked(v as MaterialButton, context)
                    selectAnswer(card.cardDefinition[1], onUserAnswered)
                }
            }
            btAlternative3.isVisible = false
            btAlternative4.isVisible = false

            btSpeak.setOnClickListener {
                val texts = listOf(
                    card.cardContent.content,
                    card.cardDefinition[0].definition,
                    card.cardDefinition[1].definition,
                )
                val views = listOf(
                    tvContent,
                    btAlternative1,
                    btAlternative2,
                )

                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        ""
                    )
                )
            }
        }

        private fun onButtonClicked(button: MaterialButton, cardType: String, context: Context) {
            if (cardType == MULTIPLE_ANSWER_CARD) {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box)
            } else {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_radio_button_checked)
            }
            button.backgroundTintList = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainer,
                ContextCompat.getColorStateList(context, R.color.neutral950)!!
            )
            button.strokeColor = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerHighest,
                ContextCompat.getColorStateList(context, R.color.neutral700)!!
            )
        }

        private fun onButtonUnClicked(button: MaterialButton, cardType: String, context: Context) {
            if (cardType == MULTIPLE_ANSWER_CARD) {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box_outline_blank)
            } else {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_radio_button_unchecked)
            }
            button.backgroundTintList = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerLowest,
                ContextCompat.getColorStateList(context, R.color.neutral300)!!
            )
            button.strokeColor = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerHigh,
                ContextCompat.getColorStateList(context, R.color.neutral500)!!
            )
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_card_test, parent, false)
                return ViewHolder(view)
            }
        }
    }
}