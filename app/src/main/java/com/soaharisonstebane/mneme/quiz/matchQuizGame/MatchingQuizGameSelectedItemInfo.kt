package com.soaharisonstebane.mneme.quiz.matchQuizGame

import android.widget.LinearLayout
import com.soaharisonstebane.mneme.backend.models.MatchQuizGameItemModel
import com.google.android.material.card.MaterialCardView

data class MatchingQuizGameSelectedItemInfo (
    val item: MatchQuizGameItemModel,
    val itemContainerRoot: MaterialCardView,
    val itemContainerActive: LinearLayout,
    val itemContainerInactive: LinearLayout,
    val itemContainerWrong: LinearLayout,
)