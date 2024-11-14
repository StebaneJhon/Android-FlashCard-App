package com.ssoaharison.recall.quiz.multichoiceQuizGame

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.ssoaharison.recall.util.TextWithLanguageModel

class MultiChoiceQuizGameAdapter(
    val context: Context,
    val cardList: List<MultiChoiceGameCardModel>,
    val deckColorCode: String,
    private val userChoiceModel: (MultiChoiceCardDefinitionModel) -> Unit,
    private val onSpeak: (SpeakModel) -> Unit,
): RecyclerView.Adapter<MultiChoiceQuizGameAdapter.MultiChoiceQuizGameAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiChoiceQuizGameAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_card_item_multichoice_quiz_game, parent, false)
        return MultiChoiceQuizGameAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: MultiChoiceQuizGameAdapterViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            position.plus(1),
            cardList.size,
            userChoiceModel,
            deckColorCode,
            onSpeak
        )
    }

    inner class MultiChoiceQuizGameAdapterViewHolder(view: View):  RecyclerView.ViewHolder(view) {

        private val cvCardItem: MaterialCardView = view.findViewById(R.id.cv_card)
        private val tvProgressionFrontCard: TextView = view.findViewById(R.id.tv_multi_Choice_quiz_front_progression)
        private val tvOnCardWord: TextView = view.findViewById(R.id.tv_on_card_word)
        private val btAlternative1: MaterialButton = view.findViewById(R.id.bt_alternative1)
        private val btAlternative2: MaterialButton = view.findViewById(R.id.bt_alternative2)
        private val btAlternative3: MaterialButton = view.findViewById(R.id.bt_alternative3)
        private val btAlternative4: MaterialButton = view.findViewById(R.id.bt_alternative4)
        private val btSpeakFront: Button = view.findViewById(R.id.bt_card_front_speak)
        private val btAlternativesList = listOf(btAlternative1, btAlternative2, btAlternative3, btAlternative4)

        fun bind(
            context: Context,
            card: MultiChoiceGameCardModel,
            cardNumber: Int,
            cardSum: Int,
            userChoiceModel: (MultiChoiceCardDefinitionModel) -> Unit,
            deckColorCode: String,
            onSpeak: (SpeakModel) -> Unit
        ) {

            tvProgressionFrontCard.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvOnCardWord.text = card.onCardWord.text
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardItem.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            val views: ArrayList<View> = arrayListOf(tvOnCardWord)
            val texts: ArrayList<TextWithLanguageModel> = arrayListOf(card.onCardWord)

            btAlternativesList.forEachIndexed { index, button ->
                val actualDefinition = card.alternatives[index]
                button.text = actualDefinition.definition.text

                if (actualDefinition.isSelected) {
                    onButtonClicked(button, actualDefinition.isCorrect)
                } else {
                    onButtonUnClicked(button, context)
                }
                button.setOnClickListener {
                    actualDefinition.isSelected = !actualDefinition.isSelected
                    userChoiceModel( actualDefinition )
                }
                views.add(button)
                texts.add(card.alternatives[index].definition)
            }

            btSpeakFront.setOnClickListener { v ->
                onSpeak(
                    SpeakModel(
                        text = texts,
                        views = views,
                        v as Button,
                    ))
            }

        }

        private fun onButtonUnClicked(
            button: MaterialButton,
            context: Context
        ) {
            button.background.setTint(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceContainerLowest,
                    Color.GRAY
                )
            )
            button.strokeColor = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerHigh,
                ContextCompat.getColorStateList(context, R.color.neutral500)!!
            )
        }

    }

    private fun onButtonClicked(
        button: MaterialButton,
        isAnswerCorrect: Boolean
    ) {
        if (isAnswerCorrect) {
            button.background.setTint(ContextCompat.getColor(context, R.color.green50))
            button.setStrokeColorResource(R.color.green500)
            button.setIconTintResource(R.color.green500)
        } else {
            button.background.setTint(ContextCompat.getColor(context, R.color.red50))
            button.setStrokeColorResource(R.color.red500)
            button.setIconTintResource(R.color.red500)
        }
    }

}