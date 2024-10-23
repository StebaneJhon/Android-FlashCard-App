package com.ssoaharison.recall.deck

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.Deck
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnSaveDeckWithCationModel(
    val deck: Deck,
    val action: String
): Parcelable
