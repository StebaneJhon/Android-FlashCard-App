package com.ssoaharison.recall.quiz.matchQuizGame

enum class MatchQuizGameBorderSize(val numChoice: Int) {
    BOARD_1(8),
    BOARD_2(12),
    BOARD_3(18);

    fun getWidth() = when (this) {
        BOARD_1 -> 2
        BOARD_2 -> 3
        BOARD_3 -> 3
    }
    fun getHeight() = numChoice/getWidth()
    fun getCardCount() = numChoice / 2
}