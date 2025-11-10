package com.ssoaharison.recall.card

import androidx.constraintlayout.widget.ConstraintLayout
import com.ssoaharison.recall.databinding.LyAddCardFieldBinding

data class FieldModel(
    val container: ConstraintLayout,
    val ly: LyAddCardFieldBinding,
    var imageName: String? = null,
    var audioName: String? = null
)
