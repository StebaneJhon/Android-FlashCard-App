package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableDeckWithCards(
    val deck: ImmutableDeck? = null,
    val cards: List<ImmutableCard?>? = null
): Parcelable
