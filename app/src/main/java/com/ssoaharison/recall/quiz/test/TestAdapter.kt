package com.ssoaharison.recall.quiz.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.quiz.quizGame.QuizSpeakModel
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextWithLanguageModel

class TestAdapter(
    val context: Context,
    val cardList: List<TestCardModel>,
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
        private val tvHint: TextView = view.findViewById(R.id.tv_hint)

        private val btAlternatives = listOf(
            btAlternative1, btAlternative2, btAlternative3, btAlternative4, btAlternative5,
            btAlternative6, btAlternative7, btAlternative8, btAlternative9, btAlternative10,
        )


        fun bind(
            context: Context,
            card: TestCardModel,
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
            tvContent.text = card.cardContent.content
            tvCardType.text = card.cardType
            if (card.cardType == MULTIPLE_ANSWER_CARD) {
                tvHint.text = ContextCompat.getString(context, R.string.text_not_answered_2)
            } else {
                tvHint.text = ContextCompat.getString(context, R.string.text_not_answered)
            }
            val deckColor =
                DeckColorCategorySelector().selectDeckColorSurfaceContainerLow(context, deck.deckColorCode!!) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < card.cardDefinition.size) {
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

            bindAnswerAlternatives(card, onUserAnswered, onSpeak)

        }

        private fun bindAnswerAlternatives(
            card: TestCardModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit
        ) {
            val texts = arrayListOf(
                TextWithLanguageModel(
                    card.cardId,
                    card.cardContent.content,
                    CONTENT,
                    card.cardContentLanguage
                )
            )
            val views = arrayListOf(tvContent)
            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < card.cardDefinition.size) {
                    materialButton.apply {
                        visibility = View.VISIBLE
                        text = card.cardDefinition[index].definition.text
                        setOnClickListener {
                            selectAnswer(card.cardDefinition[index], onUserAnswered)
                        }
                    }
                    texts.add(card.cardDefinition[index].definition)
                    views.add(materialButton)
                } else {
                    materialButton.visibility = View.GONE
                }
            }

            btSpeak.setOnClickListener {
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                    )
                )
            }
        }

        private fun selectAnswer(
            selectedAnswer: TestCardDefinitionModel,
            onUserAnswered: (TestCardDefinitionModel) -> Unit
        ) {
            selectedAnswer.isSelected = !selectedAnswer.isSelected
            onUserAnswered(selectedAnswer)
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