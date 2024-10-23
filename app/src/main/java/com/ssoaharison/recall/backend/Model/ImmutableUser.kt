package com.ssoaharison.recall.backend.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableUser(
    val userId: Int? = null,
    val name: String? = null,
    val initial: String? = null,
    val status: String? = null,
    val creation: String? = null,
): Parcelable
