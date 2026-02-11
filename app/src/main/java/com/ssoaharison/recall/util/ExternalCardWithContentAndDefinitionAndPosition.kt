package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions

data class ExternalCardWithContentAndDefinitionAndPosition(
    val card: ExternalCardWithContentAndDefinitions,
    val position: Int
)
