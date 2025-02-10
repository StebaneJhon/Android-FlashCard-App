package com.ssoaharison.recall.quiz.writingQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class WritingQuizGameAdapter(
    val context: Context,
    val cardList: List<WritingQuizGameModel>,
    val deckColorCode: String,
    private val userAnswerAndView: (WritingQuizGameUserResponseModel) -> Unit,
    private val onSpeak: (WritingQuizSpeakModel) -> Unit,
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
            deckColorCode,
            onSpeak,
        )
    }

    inner class WritingQuizGameAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var imm: InputMethodManager? = null

        private val tvOnCardWord: TextView = itemView.findViewById(R.id.tv_top_on_card_word)
        //private val tvOnCardWordOnWrongAnswer: TextView = itemView.findViewById(R.id.tv_on_card_word_on_wrong_answer)
        private val tieTopCard: TextInputEditText = itemView.findViewById(R.id.ti_top_card_content)
        private val tilTopCard: TextInputLayout = itemView.findViewById(R.id.tilTopCardContent)
        //private val tiOnWrongAnswer: TextInputEditText = itemView.findViewById(R.id.ti_card_content_on_wrong_answer)
        private val cvCardFront: MaterialCardView = itemView.findViewById(R.id.cv_card_front)
        //private val cvCardOnWrongAnswer: MaterialCardView = itemView.findViewById(R.id.cv_card_on_wrong_answer)
        private val tvProgressionFrontCard: TextView = itemView.findViewById(R.id.tv_writing_quiz_front_progression)
        //private val tvProgressionOnWrongAnswer: TextView = itemView.findViewById(R.id.tv_writing_quiz_progression_on_wrong_answer)
        private val btSpeak: Button = itemView.findViewById(R.id.bt_card_front_speak)
        private val btShowAnswer: MaterialButton = itemView.findViewById(R.id.bt_show_answer)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tv_answer)

        fun bind(
            context: Context,
            card: WritingQuizGameModel,
            cardNumber: Int,
            cardSum: Int,
            userAnswer: (WritingQuizGameUserResponseModel) -> Unit,
            deckColorCode: String,
            onSpeak: (WritingQuizSpeakModel) -> Unit,
        ) {
            imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            tieTopCard.text?.clear()
            //tiOnWrongAnswer.text?.clear()
            tvOnCardWord.text = card.onCardWord.text
            //tvOnCardWordOnWrongAnswer.text = card.onCardWord.text
            tvProgressionFrontCard.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            //tvProgressionOnWrongAnswer.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            //val deckColor = DeckColorCategorySelector().selectDeckColorSurfaceContainerLow(context, deckColorCode) ?: R.color.black
            //cvCardFront.backgroundTintList =  ContextCompat.getColorStateList(context, deckColor)
            tvAnswer.text = context.getString(R.string.text_correct_answer, card.answer.first().text)

            when {
                card.attemptTime == 0 -> {
                    fieldStateOnNoAnswer(context)
                }
                card.attemptTime > 0 && card.isCorrectlyAnswered -> {
                    fieldStateOnWrightAnswer(context, card)
                }
                card.attemptTime > 0 && !card.isCorrectlyAnswered -> {
                    fieldStateOnWrongAnswer(context)
                }
            }

            tieTopCard.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    //tiOnWrongAnswer.setText(v.text)
                    val userInput = v.text?.trim().toString().lowercase()
                    val correctAnswer = card.answer
                    userAnswer(
                        WritingQuizGameUserResponseModel(
                            card.cardId,
                            userInput,
                            correctAnswer,
                        )
                    )
                    true
                } else {
                    false
                }
            }

            btSpeak.setOnClickListener { v ->
                onSpeak(
                    WritingQuizSpeakModel(
                        text = card.onCardWord,
                        views = tvOnCardWord,
                        v as Button
                    )
                )
            }

        }

        private fun fieldStateOnNoAnswer(context: Context) {
            tilTopCard.setBoxStrokeColorStateList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.writing_quiz_text_field_color
                )!!
            )
            tieTopCard.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
            tilTopCard.setDefaultHintTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.neutral400
                )
            )
            tilTopCard.hint = ContextCompat.getString(context, R.string.et_user_answer_hint)
            tieTopCard.text?.clear()
            btShowAnswer.visibility = View.GONE
            tvAnswer.visibility = View.GONE
        }

        private fun fieldStateOnWrightAnswer(
            context: Context,
            card: WritingQuizGameModel
        ) {
            tilTopCard.setBoxStrokeColorStateList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.writing_quiz_text_field_color_on_right
                )!!
            )
            tieTopCard.setTextColor(ContextCompat.getColor(context, R.color.green950))
            tilTopCard.setDefaultHintTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.green500
                )
            )
            tilTopCard.hint = ContextCompat.getString(context, R.string.text_correct)
            tieTopCard.setText(card.userAnswer)
            btShowAnswer.visibility = View.GONE
            tvAnswer.visibility = View.GONE
        }

        private fun fieldStateOnWrongAnswer(context: Context) {
            tilTopCard.setBoxStrokeColorStateList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.writing_quiz_text_field_color_on_wrong
                )!!
            )
            tieTopCard.setTextColor(ContextCompat.getColor(context, R.color.red950))
            tilTopCard.setDefaultHintTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.red500
                )
            )
            tilTopCard.hint = ContextCompat.getString(context, R.string.text_try_again)
            btShowAnswer.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    if (tvAnswer.isVisible) {
                        tvAnswer.visibility = View.GONE
                        btShowAnswer.icon =
                            ContextCompat.getDrawable(context, R.drawable.icon_expand_more)
                    } else {
                        tvAnswer.visibility = View.VISIBLE
                        btShowAnswer.icon =
                            ContextCompat.getDrawable(context, R.drawable.icon_expand_less)
                    }
                }
            }
        }
    }

}