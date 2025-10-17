package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardWithContentAndDefinitions(
    val card: ExternalCard,
    val contentWithDefinitions: ExternalCardContentWithDefinitions
): Parcelable
