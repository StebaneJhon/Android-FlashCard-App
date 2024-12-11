package com.ssoaharison.recall.card

import com.ssoaharison.recall.backend.models.ImmutableCard

data class ModelCardWithPositionOnLocalEdit (
    val cardToEdit: ImmutableCard,
    val position: Int,
)