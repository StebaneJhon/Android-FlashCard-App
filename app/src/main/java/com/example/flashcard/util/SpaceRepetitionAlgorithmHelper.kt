package com.example.flashcard.util

import com.example.flashcard.backend.Model.ImmutableCard
import java.text.SimpleDateFormat
import java.util.Calendar

class SpaceRepetitionAlgorithmHelper {

    val box = mapOf(
        "L1" to 1,
        "L2" to 2,
        "L3" to 4,
        "L4" to 7,
        "L5" to 14,
        "L6" to 31,
        "L7" to 0
    )

    fun isForgotten(card: ImmutableCard): Boolean {
        val today = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val todayDate = formatter.format(today)
        return if (!card.lastRevisionDate.isNullOrEmpty()) {
            val lastRevised = formatter.format(card.lastRevisionDate)
            val comparison = todayDate.compareTo(lastRevised)
            comparison > 0
        } else {
            val creationDate = formatter.format(card.creationDate)
            val comparison = todayDate.compareTo(creationDate)
            comparison > 0
        }
    }

    fun status(card: ImmutableCard, isKnown: Boolean): String {
            return if (isKnown) {
                when (card.cardStatus) {
                    "L1"  -> { "L2" }
                    "L2"  -> { "L3" }
                    "L3"  -> { "L4" }
                    "L4"  -> { "L5" }
                    "L6"  -> { "L7" }
                    else -> {"L7"}
                }
            } else {
                "L1"
            }
    }

    fun nextRevisionDate(card: ImmutableCard, isKnown: Boolean): String {
        val today = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        if (!isKnown) {
            return formatter.format(today.add(Calendar.DATE, box["L1"]!!))
        }
        card.cardStatus?.let {
            val newDate = today.add(Calendar.DATE, box[it]!!)
            return formatter.format(newDate)
        }
        return formatter.format(today.add(Calendar.DATE, box["L2"]!!))
    }

}