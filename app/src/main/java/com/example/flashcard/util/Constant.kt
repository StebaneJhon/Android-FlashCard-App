package com.example.flashcard.util

object Constant {
    val ADD = "add"
    val UPDATE = "update"
    const val SUCCEED = "SUCCEED"
    const val FAILED = "FAILED"
    const val MIN_CARD_FOR_MULTI_CHOICE_QUIZ = 4
    const val MIN_CARD_FOR_MATCHING_QUIZ = 5
}

object deckCategoryColorConst {
    const val WHITE = "white"
    const val RED = "red"
    const val PINK = "pink"
    const val PURPLE = "purple"
    const val BLUE = "blue"
    const val TEAL = "teal"
    const val GREEN = "green"
    const val YELLOW = "yellow"
    const val BROWN = "brown"
    const val BLACK = "black"
    const val GREY = "grey"
    const val ORANGE = "orange"
    const val LIME = "lime"
    const val EMERALD = "emerald"
    const val CYAN = "cyan"
    const val SKY = "sky"
    const val INDIGO = "indigo"
    const val VIOLET = "violet"
    const val FUCHSIA = "fuchsia"
    const val ROSE = "rose"
}

object themeConst {
    val DARK_THEME = "DARK THEME"
    val PURPLE_THEME = "PURPLE THEME"
    val WHITE_THEME = "WHITE THEM"
    val BLUE_THEME = "BLUE THEME"
    val PINK_THEME = "PINK THEME"
    val RED_THEME = "RED THEME"
    val TEAL_THEME = "TEAL THEME"
    val GREEN_THEME = "GREEN THEME"
    val YELLOW_THEME = "YELLOW THEME"
    val BROWN_THEME = "BROWN THEME"
}

object LevelColors {
    const val RED = "Red"
    const val ORANGE = "Orange"
    const val BROWNE = "Brown"
    const val YELLOW700 = "Yellow700"
    const val YELLOW500 = "Yellow500"
    const val GREEN500 = "Green500"
    const val GREEN700 = "Green700"
}

object MatchQuizGameClickStatus {
    val FIRST_TRY = "First try"
    val MATCHE = "Match"
    val MATCH_NOT = "Match not"
}

object FlashCardTimedTimerStatus {
    val TIMER_FINISHED = "Timer Finished"
}

object FlashCardMiniGameRef {
    const val FLASH_CARD_QUIZ = "Flash card quiz"
    const val TIMED_FLASH_CARD_QUIZ = "Timed flash card quiz"
    const val MULTIPLE_CHOICE_QUIZ = "Multiple choice quiz"
    const val WRITING_QUIZ = "Writing quiz"
    const val MATCHING_QUIZ = "Matching quiz"
    const val QUIZ = "Quiz"
    const val TEST = "Test"
    const val FLASH_CARD_MINI_GAME_REF = "Flash Card MiniGame Ref"
    const val CHECKED_FILTER = "Checked Filter"
    const val CHECKED_CARD_ORIENTATION = "Checked Card Orientation"
    const val IS_UNKNOWN_CARD_FIRST = "Is Unknown Card First"
    const val IS_UNKNOWN_CARD_ONLY = "Is Unknown Card Only"
    const val FILTER_RANDOM = "Filter Random"
    const val FILTER_CREATION_DATE = "Filter Creation Date"
    const val FILTER_BY_LEVEL = "Filter By Level"
    const val CARD_ORIENTATION_BACK_AND_FRONT = "Orientation Back and Front"
    const val CARD_ORIENTATION_FRONT_AND_BACK = "Orientation Front and Back"
}

object DeckRef {
    const val DECK_SORT_ALPHABETICALLY = "Deck sort alphabetically"
    const val DECK_SORT_BY_CARD_SUM = "Deck sort by card sum"
    const val DECK_SORT_BY_CREATION_DATE = "Deck sort by creation date"
}

object CardLevel {
    const val L1 = "L1"
    const val L2 = "L2"
    const val L3 = "L3"
    const val L4 = "L4"
    const val L5 = "L5"
    const val L6 = "L6"
    const val L7 = "L7"
}

object CardType {
    const val SINGLE_ANSWER_CARD = "Single answer card"
    const val MULTIPLE_ANSWER_CARD = "Multiple answer card"
    const val MULTIPLE_CHOICE_CARD = "Multiple choice card"
}

object DeckAdditionAction {
    const val ADD = "add the deck"
    const val ADD_DECK_FORWARD_TO_CARD_ADDITION = "add deck and forward to card addition"
}

object ContactActions {
    const val HELP = "Help"
    const val CONTACT = "Contact"
}

object TestResultAction {
    const val BACK_TO_DECK = "Back to deck"
    const val RETAKE_TEST = "Retake test"
}