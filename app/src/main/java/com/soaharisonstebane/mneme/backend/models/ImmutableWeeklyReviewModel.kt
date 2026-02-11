package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableWeeklyReviewModel(
    var sunday: ImmutableDayModel? = null,
    var monday: ImmutableDayModel? = null,
    var tuesday: ImmutableDayModel? = null,
    var wednesday: ImmutableDayModel? = null,
    var thursday: ImmutableDayModel? = null,
    var friday: ImmutableDayModel? = null,
    var saturday: ImmutableDayModel? = null,
): Parcelable
