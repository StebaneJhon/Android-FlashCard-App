package com.ssoaharison.recall.card

import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class DefinitionFieldModel(
    val container: LinearLayout,
    val fieldLy: TextInputLayout,
    val fieldEd: TextInputEditText,
    val chip: TextView,
    val btDeleteField: MaterialButton?,
)
