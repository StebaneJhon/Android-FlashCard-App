package com.example.flashcard.quiz.multichoiceQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.flashcard.R
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MultiChoiceQuizGameAdapter(
    val context: Context,
    val cardList: List<MultiChoiceGameCardModel>,
    val deckColorCode: String,
    private val userChoiceModel: (MultiChoiceQuizGameUserChoiceModel) -> Unit
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
            deckColorCode
        )
    }

    inner class MultiChoiceQuizGameAdapterViewHolder(view: View):  RecyclerView.ViewHolder(view) {

        val cvCardItem: MaterialCardView = view.findViewById(R.id.cv_card)
        val cvCardItemOnWrongCard: MaterialCardView = view.findViewById(R.id.cv_card_on_wrong_answer)
        val tvProgressionFrontCard: TextView = view.findViewById(R.id.tv_multi_Choice_quiz_front_progression)
        val tvProgressionOnWrongCard: TextView = view.findViewById(R.id.tv_multi_choice_quiz_front_progression_on_wrong_answer)
        val tvOnCardWord: TextView = view.findViewById(R.id.tv_on_card_word)
        val tvOnCardWordOnWrongCard: TextView = view.findViewById(R.id.tv_on_card_word_on_wrong_answer)
        val btAlternative1: MaterialButton = view.findViewById(R.id.bt_alternative1)
        val btAlternative2: MaterialButton = view.findViewById(R.id.bt_alternative2)
        val btAlternative3: MaterialButton = view.findViewById(R.id.bt_alternative3)
        val btAlternative4: MaterialButton = view.findViewById(R.id.bt_alternative4)
        val btAlternative1OnWrongCard: MaterialButton = view.findViewById(R.id.bt_alternative1_on_wrong_answer)
        val btAlternative2OnWrongCard: MaterialButton = view.findViewById(R.id.bt_alternative2_on_wrong_answer)
        val btAlternative3OnWrongCard: MaterialButton = view.findViewById(R.id.bt_alternative3_on_wrong_answer)
        val btAlternative4OnWrongCard: MaterialButton = view.findViewById(R.id.bt_alternative4_on_wrong_answer)

        fun bind(
            context: Context,
            card: MultiChoiceGameCardModel,
            cardNumber: Int,
            cardSum: Int,
            userChoiceModel: (MultiChoiceQuizGameUserChoiceModel) -> Unit,
            deckColorCode: String
        ) {

            tvProgressionFrontCard.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvProgressionOnWrongCard.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvOnCardWord.text = card.onCardWord
            tvOnCardWordOnWrongCard.text = card.onCardWord
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardItem.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            btAlternative1.apply {
                text = card.alternative1
                setOnClickListener {
                    userChoiceModel(
                        MultiChoiceQuizGameUserChoiceModel(
                            this.text.toString(),
                            card.answer,
                            cvCardItem,
                            cvCardItemOnWrongCard
                        )
                    )
                }
            }

            btAlternative2.apply {
                text = card.alternative2
                setOnClickListener {
                    userChoiceModel(
                        MultiChoiceQuizGameUserChoiceModel(
                            this.text.toString(),
                            card.answer,
                            cvCardItem,
                            cvCardItemOnWrongCard
                        )
                    )
                }
            }

            btAlternative3.apply {
                text = card.alternative3
                setOnClickListener {
                    userChoiceModel(
                        MultiChoiceQuizGameUserChoiceModel(
                            this.text.toString(),
                            card.answer,
                            cvCardItem,
                            cvCardItemOnWrongCard
                        )
                    )
                }
            }

            btAlternative4.apply {
                text = card.alternative4
                setOnClickListener {
                    userChoiceModel(
                        MultiChoiceQuizGameUserChoiceModel(
                            this.text.toString(),
                            card.answer,
                            cvCardItem,
                            cvCardItemOnWrongCard
                        )
                    )
                }
            }

            btAlternative1OnWrongCard.text = card.alternative1
            btAlternative2OnWrongCard.text = card.alternative2
            btAlternative3OnWrongCard.text = card.alternative3
            btAlternative4OnWrongCard.text = card.alternative4

        }

    }

}