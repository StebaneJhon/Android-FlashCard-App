package com.ssoaharison.recall.card

import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssoaharison.recall.databinding.LyCardDefinitionFieldBinding

data class DefinitionFieldModel(
    val container: ConstraintLayout,
    val ly: LyCardDefinitionFieldBinding,
)
