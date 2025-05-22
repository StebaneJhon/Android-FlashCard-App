package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.models.ImmutableCard

data class ImmutableCardWithPosition(
    val card: ImmutableCard,
    val position: Int
)
