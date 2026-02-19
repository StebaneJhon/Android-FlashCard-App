package com.soaharisonstebane.mneme.home

import com.soaharisonstebane.mneme.backend.models.ImmutableCard

data class ModelCardWithPositionOnLocalEdit (
    val cardToEdit: ImmutableCard,
    val position: Int,
)