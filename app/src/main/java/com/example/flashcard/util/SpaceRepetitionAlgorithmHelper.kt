package com.example.flashcard.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class SpaceRepetitionAlgorithmHelper {

    val box = mapOf(
        "L1" to LevelModel(0, R.color.red700),
        "L2" to LevelModel(1, R.color.orange),
        "L3" to LevelModel(2, R.color.brown500),
        "L4" to LevelModel(4, R.color.yellow700),
        "L5" to LevelModel(7, R.color.yellow500),
        "L6" to LevelModel(14, R.color.green500),
        "L7" to LevelModel(31, R.color.green700)
    )

    fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

    fun isForgotten(card: ImmutableCard): Boolean {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.lastRevisionDate.isNullOrEmpty() && card.lastRevisionDate != "0") {
            val lastRevised = LocalDate.parse(card.lastRevisionDate, formatter)
            today > lastRevised
        } else {

            val creationDate = if (!card.creationDate.isNullOrEmpty() && card.creationDate == "0") {
                today
            } else {
                LocalDate.parse(card.creationDate, formatter)
            }
            today > creationDate
        }
    }

    fun status(card: ImmutableCard, isKnown: Boolean): String {
        val today = LocalDate.now()
        val lastRevision = lastRevisionDate(card)
        val nextRevision = nextForgottenDate(card)
        lastRevision?.let {
            if (lastRevision == today && isKnown && nextRevision != today) {
                return card.cardStatus!!
            } else {
                return nextLV(isKnown, card)
            }
        }

        return nextLV(isKnown, card)

    }

    private fun nextLV(
        isKnown: Boolean,
        card: ImmutableCard
    ): String {
        return if (isKnown) {
            when (card.cardStatus) {
                "L1" -> { "L2" }
                "L2" -> { "L3" }
                "L3" -> { "L4" }
                "L4" -> { "L5" }
                "L6" -> { "L7" }
                else -> { "L7" }
            }
        } else {
            "L1"
        }
    }

    fun lastRevisionDate(card: ImmutableCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.lastRevisionDate.isNullOrEmpty() && card.lastRevisionDate != "0") {
            LocalDate.parse(card.lastRevisionDate, formatter)
        } else {
            null
        }

    }

    fun nextForgottenDate(card: ImmutableCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextMissMemorisationDate.isNullOrEmpty() && card.nextMissMemorisationDate != "0") {
            LocalDate.parse(card.nextMissMemorisationDate, formatter)
        } else {
            null
        }
    }

    fun nextRevisionDate(card: ImmutableCard, isKnown: Boolean, newCardStatus: String): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        val lastRevised = lastRevisionDate(card)
        val nextRevision = nextForgottenDate(card)

        lastRevised?.let {
            if (it == today && !isKnown) {
                val period = Period.of(0, 0, box["L1"]?.repeatDay!!)
                val nextDate = today.plus(period)
                val result = formatter.format(nextDate)
                return result
            }

            if (it == today && isKnown && nextRevision != today) {
                card.nextMissMemorisationDate?.let {
                    return card.nextMissMemorisationDate
                }
                return formatter.format(today)
            }
        }

        if (!isKnown) {
            val period = Period.of(0, 0, box["L1"]?.repeatDay!!)
            val nextDate = today.plus(period)
            val result = formatter.format(nextDate)
            return result
        }

        card.cardStatus?.let {
            val period = Period.of(0, 0, box[newCardStatus]?.repeatDay!!)
            val newDate = today.plus(period)
            return formatter.format(newDate)
        }
        val period = Period.of(0, 0, box["L2"]?.repeatDay!!)
        return formatter.format(today.plus(period))
    }

}