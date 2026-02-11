package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardWithContentAndDefinitions(
    val card: ExternalCard,
    val contentWithDefinitions: ExternalCardContentWithDefinitions
): Parcelable
