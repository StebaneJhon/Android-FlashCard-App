package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCard(
    val cardId: String,
    val deckOwnerId: String,
    val cardLevel: String?,
    val cardType: String?,
    val revisionTime: Int?,
    val missedTime: Int?,
    val creationDate: String?,
    val lastRevisionDate: String?,
    val nextMissMemorisationDate: String?,
    val nextRevisionDate: String?,
    val cardContentLanguage: String?,
    val cardDefinitionLanguage: String?,
): Parcelable
