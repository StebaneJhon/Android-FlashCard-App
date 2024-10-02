package com.example.flashcard.card

import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class DefinitionFieldModel(
    val fieldLy: TextInputLayout,
    val fieldEd: TextInputEditText,
    val chip: Chip
)
