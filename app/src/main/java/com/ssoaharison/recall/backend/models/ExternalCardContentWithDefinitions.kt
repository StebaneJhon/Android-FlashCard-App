package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardContentWithDefinitions(
    val content: ExternalCardContent,
    val definitions: List<ExternalCardDefinition>
): Parcelable
