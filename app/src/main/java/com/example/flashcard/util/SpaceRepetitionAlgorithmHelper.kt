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
        "L2" to LevelModel(1, R.color.brown500),
        "L3" to LevelModel(2, R.color.orange),
        "L4" to LevelModel(4, R.color.yellow700),
        "L5" to LevelModel(7, R.color.yellow500),
        "L6" to LevelModel(14, R.color.green500),
        "L7" to LevelModel(31, R.color.green700)
    )

    fun isForgotten(card: ImmutableCard): Boolean {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayDate = formatter.format(today)
        val dd = card.lastRevisionDate
        return if (!card.lastRevisionDate.isNullOrEmpty() && card.lastRevisionDate != "0") {
            val lastRevised = LocalDate.parse(card.lastRevisionDate, formatter)
            //val comparison = todayDate.compareTo(lastRevised)
            today >= lastRevised
        } else {

            val creationDate = if (!card.creationDate.isNullOrEmpty() && card.creationDate == "0") {
                today
            } else {
                LocalDate.parse(card.creationDate, formatter)
            }
            //val comparison = todayDate.compareTo(creationDate)
            today >= creationDate
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
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if (!isKnown) {
            val period = Period.of(0, 0, box["L1"]?.repeatDay!!)
            val nextDate = today.plus(period)
            val result = formatter.format(nextDate)
            return result
        }

        card.cardStatus?.let {
            val period = Period.of(0, 0, box[it]?.repeatDay!!)
            val newDate = today.plus(period)
            return formatter.format(newDate)
        }
        val period = Period.of(0, 0, box["L2"]?.repeatDay!!)
        return formatter.format(today.plus(period))
    }

}