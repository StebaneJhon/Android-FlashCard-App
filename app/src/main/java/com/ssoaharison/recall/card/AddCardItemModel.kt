package com.ssoaharison.recall.card

import com.ssoaharison.recall.helper.PhotoModel

sealed class AddCardItemModel {

    data class DefinitionFieldModel (
        var definitionId: String,
        var definitionText: String?,
        var definitionImage: PhotoModel?,
        var isCorrectDefinition: Boolean,
        var hasFocus: Boolean,
    ) : AddCardItemModel()

    data class ContentFieldModel (
        var contentId: String?,
        var contentText: String?,
        var contentImage: PhotoModel?,
        var hasFocus: Boolean,
    ): AddCardItemModel()

    data class LanguageModel(
        val type: String,
        val language: String?,
    ): AddCardItemModel()

}