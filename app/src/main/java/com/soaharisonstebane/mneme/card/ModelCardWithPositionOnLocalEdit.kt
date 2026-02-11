package com.soaharisonstebane.mneme.card

import com.soaharisonstebane.mneme.backend.models.ImmutableCard

data class ModelCardWithPositionOnLocalEdit (
    val cardToEdit: ImmutableCard,
    val position: Int,
)