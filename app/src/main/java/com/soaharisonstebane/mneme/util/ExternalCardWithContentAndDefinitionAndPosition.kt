package com.soaharisonstebane.mneme.util

import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions

data class ExternalCardWithContentAndDefinitionAndPosition(
    val card: ExternalCardWithContentAndDefinitions,
    val position: Int
)
