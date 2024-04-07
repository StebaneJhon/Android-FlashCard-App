package com.example.flashcard.quiz.testQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.util.CardType.FLASHCARD
import com.example.flashcard.util.CardType.ONE_OR_MULTI_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TestQuizGameAdapter(
    val context: Context,
    val cardList: List<ImmutableCard?>,
    val deckColor: String,
    private val cardOnClick: (ImmutableCard) -> Unit
): RecyclerView.Adapter<TestQuizGameAdapter.TestQuizGameAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TestQuizGameAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_card_test, parent, false)
        return TestQuizGameAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: TestQuizGameAdapterViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            position.plus(1),
            cardList.size,
            deckColor,
            cardOnClick
        )
    }

    inner class TestQuizGameAdapterViewHolder(
        view: View
    ): RecyclerView.ViewHolder(view) {

        private val cvCardContainer: MaterialCardView = view.findViewById(R.id.cv_card_root)
        private val tvFrontProgression: TextView = view.findViewById(R.id.tv_front_progression)
        private val tvCardType: TextView = view.findViewById(R.id.tv_card_type)
        private val btHelp: ImageButton = view.findViewById(R.id.bt_help)
        private val tvContent: TextView = view.findViewById(R.id.tv_content)
        private val btAlternative1: MaterialButton = view.findViewById(R.id.bt_alternative1)
        private val btAlternative2: MaterialButton = view.findViewById(R.id.bt_alternative2)
        private val btAlternative3: MaterialButton = view.findViewById(R.id.bt_alternative3)
        private val btAlternative4: MaterialButton = view.findViewById(R.id.bt_alternative4)

        fun bind(
            context: Context,
            card: ImmutableCard?,
            cardNumber: Int,
            cardSum: Int,
            deckColorCode: String,
            cardOnClick: (ImmutableCard) -> Unit
        ) {

            tvFrontProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvContent.text = card?.cardContent?.content
            tvCardType.text = card?.cardType
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            when(card?.cardType) {
                FLASHCARD -> {
                    onFlashCard()
                }
                TRUE_OR_FALSE_CARD -> {
                    onTrueOrFalseCard(card)
                }
                ONE_OR_MULTI_ANSWER_CARD -> {
                    onOneOrMultiAnswer(card)
                }
                else -> {
                    onFlashCard()
                }
            }

        }

        fun onFlashCard() {

            btAlternative1.isVisible = false
            btAlternative2.isVisible = false
            btAlternative3.isVisible = false
            btAlternative4.isVisible = false

        }

        fun onTrueOrFalseCard(card: ImmutableCard?) {

            val cardModel = TrueOrFalseCardModel(card!!, cardList)
            val answers = cardModel.getCardAnswers()
            btAlternative1.apply {
                isVisible = true
                text = answers[0].definition
            }
            btAlternative2.apply {
                isVisible = true
                text = answers[1].definition
            }
            btAlternative3.isVisible = false
            btAlternative4.isVisible = false

        }

        fun onOneOrMultiAnswer(card: ImmutableCard) {
            val cardModel = OneOrMultipleAnswerCardModel(card, cardList)
            val answers = cardModel.getCardAnswers()
            when (answers.size ) {
                2 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = answers[0].definition
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = answers[1].definition
                    }
                    btAlternative3.isVisible = false
                    btAlternative4.isVisible = false
                }
                3 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = answers[0].definition
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = answers[1].definition
                    }
                    btAlternative3.apply {
                        isVisible = true
                        text = answers[2].definition
                    }
                    btAlternative4.isVisible = false
                }
                4 -> {
                    btAlternative1.apply {
                        isVisible = true
                        text = answers[0].definition
                    }
                    btAlternative2.apply {
                        isVisible = true
                        text = answers[1].definition
                    }
                    btAlternative3.apply {
                        isVisible = true
                        text = answers[2].definition
                    }
                    btAlternative4.apply {
                        isVisible = true
                        text = answers[3].definition
                    }
                }
                else -> {
                    onFlashCard()
                }
            }
        }

    }
}