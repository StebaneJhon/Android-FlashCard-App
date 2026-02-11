package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalDeckWithCardsAndContentAndDefinitions (
    val deck: ExternalDeck,
    val cards: List<ExternalCardWithContentAndDefinitions>
): Parcelable