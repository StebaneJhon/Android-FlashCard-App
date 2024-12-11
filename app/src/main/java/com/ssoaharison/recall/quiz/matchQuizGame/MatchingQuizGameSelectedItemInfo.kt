package com.ssoaharison.recall.quiz.matchQuizGame

import android.widget.LinearLayout
import com.ssoaharison.recall.backend.models.MatchQuizGameItemModel
import com.google.android.material.card.MaterialCardView

data class MatchingQuizGameSelectedItemInfo (
    val item: MatchQuizGameItemModel,
    val itemContainerRoot: MaterialCardView,
    val itemContainerActive: LinearLayout,
    val itemContainerInactive: LinearLayout,
    val itemContainerWrong: LinearLayout,
)