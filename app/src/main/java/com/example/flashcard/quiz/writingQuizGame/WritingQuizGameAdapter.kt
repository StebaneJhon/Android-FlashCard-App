package com.example.flashcard.quiz.writingQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class WritingQuizGameAdapter(
    val context: Context,
    val cardList: List<WritingQuizGameModel>,
    val deckColorCode: String,
    private val userAnswerAndView: (WritingQuizGameUserResponseModel) -> Unit
): RecyclerView.Adapter<WritingQuizGameAdapter.WritingQuizGameAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WritingQuizGameAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_card_writing_quiz_item, parent, false)
        return WritingQuizGameAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: WritingQuizGameAdapterViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            position.plus(1),
            cardList.size,
            userAnswerAndView,
            deckColorCode
        )
    }

    inner class WritingQuizGameAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var imm: InputMethodManager? = null

        private val LyCard: ConstraintLayout = itemView.findViewById(R.id.root_card)
        private val tvOnCardWord: TextView = itemView.findViewById(R.id.tv_top_on_card_word)
        private val tvOnCardWordOnWrongAnswer: TextView = itemView.findViewById(R.id.tv_on_card_word_on_wrong_answer)
        private val tiTopCard: TextInputEditText = itemView.findViewById(R.id.ti_top_card_content)
        private val tilTopCardContent: TextInputLayout = itemView.findViewById(R.id.tilTopCardContent)
        private val tiOnWrongAnswer: TextInputEditText = itemView.findViewById(R.id.ti_card_content_on_wrong_answer)
        private val cvCardFront: MaterialCardView = itemView.findViewById(R.id.cv_card_front)
        private val cvCardOnWrongAnswer: MaterialCardView = itemView.findViewById(R.id.cv_card_on_wrong_answer)
        private val tvProgressionFrontCard: TextView = itemView.findViewById(R.id.tv_writing_quiz_front_progression)
        private val tvProgressionOnWrongAnswer: TextView = itemView.findViewById(R.id.tv_writing_quiz_progression_on_wrong_answer)

        fun bind(
            context: Context,
            card: WritingQuizGameModel,
            cardNumber: Int,
            cardSum: Int,
            userAnswer: (WritingQuizGameUserResponseModel) -> Unit,
            deckColorCode: String
        ) {

            imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            tiTopCard.setText("")
            tiOnWrongAnswer.setText("")
            tvOnCardWord.text = card.onCardWord
            tvOnCardWordOnWrongAnswer.text = card.onCardWord
            tvProgressionFrontCard.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvProgressionOnWrongAnswer.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardFront.backgroundTintList =  ContextCompat.getColorStateList(context, deckColor)

            tiTopCard.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    tiOnWrongAnswer.setText(v.text)
                    val userInput = v.text?.trim().toString().lowercase()
                    val correctAnswer = card.answer.lowercase()
                    userAnswer(
                        WritingQuizGameUserResponseModel(
                            userInput,
                            correctAnswer,
                            cvCardFront,
                            cvCardOnWrongAnswer
                        )
                    )
                    true
                } else {
                    false
                }
            }

        }
    }

}