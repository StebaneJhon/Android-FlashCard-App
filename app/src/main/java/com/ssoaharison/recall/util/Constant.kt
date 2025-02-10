package com.ssoaharison.recall.util

object Constant {
    const val ADD = "add"
    const val UPDATE = "update"
    const val ERROR = "error"
    const val MIN_CARD_FOR_MULTI_CHOICE_QUIZ = 4
    const val MIN_CARD_FOR_MATCHING_QUIZ = 4
    const val MIN_CARD_FOR_TEST = 4
}

object DeckCategoryColorConst {
    const val WHITE = "white"
    const val BLACK = "black"
    const val GREY = "grey"
    const val RED = "red"
    const val ORANGE = "orange"
    const val AMBER = "Amber"
    const val YELLOW = "yellow"
    const val LIME = "lime"
    const val GREEN = "green"
    const val EMERALD = "emerald"
    const val TEAL = "teal"
    const val CYAN = "cyan"
    const val SKY = "sky"
    const val BLUE = "blue"
    const val INDIGO = "indigo"
    const val VIOLET = "violet"
    const val PURPLE = "purple"
    const val FUCHSIA = "fuchsia"
    const val PINK = "pink"
    const val ROSE = "rose"
}

object ThemeConst {
    const val WHITE_THEME = "WHITE THEM"
    const val DARK_THEME = "DARK THEME"
    const val RED_THEME = "RED THEME"
    const val ORANGE_THEME = "ORANGE THEME"
    const val AMBER_THEME = "AMBER THEME"
    const val YELLOW_THEME = "YELLOW THEME"
    const val LIME_THEME = "LIME THEME"
    const val GREEN_THEME = "GREEN THEME"
    const val EMERALD_THEME = "EMERALD THEME"
    const val TEAL_THEME = "TEAL THEME"
    const val CYAN_THEME = "CYAN THEME"
    const val SKY_THEME = "SKY THEME"
    const val BLUE_THEME = "BLUE THEME"
    const val INDIGO_THEME = "INDIGO THEME"
    const val VIOLET_THEME = "VIOLET THEME"
    const val PURPLE_THEME = "PURPLE THEME"
    const val FUCHSIA_THEME = "FUCHSIA THEME"
    const val PINK_THEME = "PINK THEME"
    const val ROSE_THEME = "ROSE THEME"
    const val BROWN_THEME = "BROWN THEME"

    const val DARK_WHITE_THEME = "DARK WHITE THEM"
    const val DARK_DARK_THEME = "DARK DARK THEME"
    const val DARK_RED_THEME = "DARK RED THEME"
    const val DARK_ORANGE_THEME = "DARK ORANGE THEME"
    const val DARK_AMBER_THEME = "DARK AMBER THEME"
    const val DARK_YELLOW_THEME = "DARK YELLOW THEME"
    const val DARK_LIME_THEME = "DARK LIME THEME"
    const val DARK_GREEN_THEME = "DARK GREEN THEME"
    const val DARK_EMERALD_THEME = "DARK EMERALD THEME"
    const val DARK_TEAL_THEME = "DARK TEAL THEME"
    const val DARK_CYAN_THEME = "DARK CYAN THEME"
    const val DARK_SKY_THEME = "DARK SKY THEME"
    const val DARK_BLUE_THEME = "DARK BLUE THEME"
    const val DARK_INDIGO_THEME = "DARK INDIGO THEME"
    const val DARK_VIOLET_THEME = "DARK VIOLET THEME"
    const val DARK_PURPLE_THEME = "DARK PURPLE THEME"
    const val DARK_FUCHSIA_THEME = "DARK FUCHSIA THEME"
    const val DARK_PINK_THEME = "DARK PINK THEME"
    const val DARK_ROSE_THEME = "DARK ROSE THEME"
    const val DARK_BROWN_THEME = "DARK BROWN THEME"
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
    const val FIRST_TRY = "First try"
    const val MATCH = "Match"
    const val MATCH_NOT = "Match not"
}

object FlashCardTimedTimerStatus {
    const val TIMER_FINISHED = "Timer Finished"
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
    const val CARD_COUNT = "Card count"
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

object ItemLayoutManager {
    const val STAGGERED_GRID_LAYOUT_MANAGER = "STAGGERED GRID LAYOUT MANAGER"
    const val LINEAR_LAYOUT_MANAGER = "LINEAR LAYOUT MANAGER"
    const val LAYOUT_MANAGER = "LAYOUT_MANAGER"
}

object BoardSizes {
    const val BOARD_SIZE_1 = "4 : 2"
    const val BOARD_SIZE_2 = "4 : 3"
    const val BOARD_SIZE_3 = "6 : 3"
}

object TextType {
    const val CONTENT = "content"
    const val DEFINITION = "definition"
}