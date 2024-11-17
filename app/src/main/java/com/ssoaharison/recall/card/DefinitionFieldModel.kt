package com.ssoaharison.recall.card

import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class DefinitionFieldModel(
    val fieldLy: TextInputLayout,
    val fieldEd: TextInputEditText,
    val chip: MaterialCheckBox,
    val btDeleteField: MaterialButton?,
)
