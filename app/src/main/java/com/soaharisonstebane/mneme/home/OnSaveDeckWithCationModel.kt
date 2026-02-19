package com.soaharisonstebane.mneme.home

import android.os.Parcelable
import com.soaharisonstebane.mneme.backend.entities.Deck
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnSaveDeckWithCationModel(
    val deck: Deck,
    val action: String
): Parcelable
