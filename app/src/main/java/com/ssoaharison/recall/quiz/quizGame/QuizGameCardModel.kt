package com.ssoaharison.recall.quiz.quizGame

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardModel(
    val cardId: String,
    val cardContent: CardContent?,
    val cardContentLanguage: String?,
    val cardDefinition: List<QuizGameCardDefinitionModel>,
    val cardDefinitionLanguage: String?,
    val cardType: String?,
    val cardStatus: String?,
    var isFlipped: Boolean = false,
    var attemptTime: Int = 0,
    var isCorrectlyAnswered: Boolean = false,
    var flipCount: Int = 0,
    var isActualOrPassed: Boolean = false,
): Parcelable {
    fun isAllAnswerSelected(): Boolean {
        cardDefinition.forEach { definition ->
            if (definition.ifCorrectIsSelected()) {
                return false
            }
        }
        return true
    }
    fun onDefinitionSelected(definitionPosition: Int, selectionState: Boolean) {
        when (cardType) {
            MULTIPLE_ANSWER_CARD -> {
                cardDefinition[definitionPosition].isSelected = selectionState
                attemptTime++
                isCorrectlyAnswered = isAllAnswerSelected()
            }
            SINGLE_ANSWER_CARD -> {
                cardDefinition.first().isSelected = selectionState
                isFlipped = !isFlipped
                attemptTime++
                flipCount++
            }
            else -> {
                cardDefinition.forEachIndexed { index, definition ->
                    if (index == definitionPosition) {
                        definition.isSelected = selectionState
                    } else {
                        definition.isSelected = false
                    }
                }
                attemptTime++
                isCorrectlyAnswered = isAllAnswerSelected()
            }
        }
    }
    fun setAsActualOrPassed() {
        isActualOrPassed = true
    }
    fun setAsNotActualOrNotPassed() {
        isActualOrPassed = false
    }
}