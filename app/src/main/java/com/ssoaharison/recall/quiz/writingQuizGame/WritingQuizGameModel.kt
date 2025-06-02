package com.ssoaharison.recall.quiz.writingQuizGame

import com.ssoaharison.recall.util.TextWithLanguageModel

data class WritingQuizGameModel (
    val cardId: String,
    val onCardWord: TextWithLanguageModel,
    val answers: List<TextWithLanguageModel>,
    var attemptTime: Int = 0,
    var isCorrectlyAnswered: Boolean = false,
    var userAnswer: String? = null,
    var isActualOrPassed: Boolean = false,
) {

    private fun isUserAnswerCorrect(temporaryUserAnswer: String): Boolean {
        val normalizedTemporaryUserAnswer = temporaryUserAnswer.trim().lowercase()
        answers.forEach { answer ->
            val normalizedCorrectAnswer = answer.text.trim().lowercase()
            if (normalizedCorrectAnswer == normalizedTemporaryUserAnswer) {
                return true
            }
        }
        return false
    }

    fun onUserAnswered(answer: String): Boolean {
        attemptTime++
        if (isUserAnswerCorrect(answer)) {
            isCorrectlyAnswered = true
            userAnswer = answer
            return true
        } else {
            isCorrectlyAnswered = false
            userAnswer = answer
            return false
        }
    }

    fun setAsActualOrPassed() {
        isActualOrPassed = true
    }

    fun setAsNotActualOrNotPassed() {
        isActualOrPassed = false
    }

}