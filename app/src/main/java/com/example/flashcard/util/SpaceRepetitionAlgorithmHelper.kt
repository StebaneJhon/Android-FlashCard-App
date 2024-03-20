package com.example.flashcard.util

import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardLevel.L2
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class SpaceRepetitionAlgorithmHelper {

    val box = mapOf<String, LevelModel>(
        "L1" to LevelModel(0, R.color.red700, revisionMargin(0)),
        "L2" to LevelModel(1, R.color.orange, revisionMargin(1)),
        "L3" to LevelModel(2, R.color.brown500, revisionMargin(2)),
        "L4" to LevelModel(4, R.color.yellow700, revisionMargin(4)),
        "L5" to LevelModel(7, R.color.yellow500, revisionMargin(7)),
        "L6" to LevelModel(14, R.color.green500, revisionMargin(14)),
        "L7" to LevelModel(31, R.color.green700, revisionMargin(31))
    )

    private fun revisionMargin(repeatDay: Int): Int {
        if (repeatDay == 0) {
            return 1
        }
        //val levelModel = box.getOrDefault(cardLevel, L2) as LevelModel
        var result: Double = repeatDay / 2.0
        result = result.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
        return result.toInt()
    }

    fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

    fun isForgotten(card: ImmutableCard): Boolean {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextMissMemorisationDate.isNullOrEmpty() && card.nextMissMemorisationDate != "0") {
            val nextMissMemorisationDate = LocalDate.parse(card.nextMissMemorisationDate, formatter)
            today > nextMissMemorisationDate
        } else {

            val creationDate = if (!card.creationDate.isNullOrEmpty() && card.creationDate == "0") {
                today
            } else {
                LocalDate.parse(card.creationDate, formatter)
            }
            today > creationDate
        }
    }

    fun isToBeRevised(card: ImmutableCard): Boolean {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextRevisionDate.isNullOrEmpty() && card.nextRevisionDate != "0") {
            val nextMissMemorisationDate = LocalDate.parse(card.nextRevisionDate, formatter)
            today >= nextMissMemorisationDate
        } else {

            val creationDate = if (!card.creationDate.isNullOrEmpty() && card.creationDate == "0") {
                today
            } else {
                LocalDate.parse(card.creationDate, formatter)
            }
            today >= creationDate
        }
    }

    fun status(card: ImmutableCard, isKnown: Boolean): String {
        val today = LocalDate.now()
        val lastRevision = lastRevisionDate(card)
        val nextForgetting = getNextForgottenDate(card)
        lastRevision?.let {
            if (lastRevision == today && isKnown && nextForgetting != today && !isToBeRevised(card)) {
                return card.cardStatus!!
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

    fun getNextForgottenDate(card: ImmutableCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextMissMemorisationDate.isNullOrEmpty() && card.nextMissMemorisationDate != "0") {
            LocalDate.parse(card.nextMissMemorisationDate, formatter)
        } else {
            null
        }
    }

    fun getNextRevisionDate(card: ImmutableCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextRevisionDate.isNullOrEmpty() && card.nextRevisionDate != "0") {
            LocalDate.parse(card.nextRevisionDate, formatter)
        } else {
            null
        }
    }

    fun nextForgettingDate(card: ImmutableCard, isKnown: Boolean, newCardStatus: String): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        val lastRevised = lastRevisionDate(card)
        val nextRevision = getNextForgottenDate(card)

        lastRevised?.let {
            if (it == today && !isKnown) {
                val days = box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin)
                val period = Period.of(0, 0, box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin))
                val nextDate = today.plus(period)
                val result = formatter.format(nextDate)
                return result
            }

            if (it == today && isKnown && nextRevision != today && !isToBeRevised(card)) {
                card.nextMissMemorisationDate?.let {
                    return card.nextMissMemorisationDate
                }
                return formatter.format(today)
            }
        }

        if (!isKnown) {
            val days = box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin)
            val period = Period.of(0, 0, box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin))
            val nextDate = today.plus(period)
            val result = formatter.format(nextDate)
            return result
        }

        card.cardStatus?.let {
            val days = box[newCardStatus]?.repeatDay!!.plus(box[newCardStatus]!!.revisionMargin)
            val period = Period.of(0, 0, box[newCardStatus]?.repeatDay!!.plus(box[newCardStatus]!!.revisionMargin))
            val newDate = today.plus(period)
            return formatter.format(newDate)
        }
        val days = box["L2"]?.repeatDay!!.plus(box[L2]!!.revisionMargin)
        val period = Period.of(0, 0, box["L2"]?.repeatDay!!.plus(box[L2]!!.revisionMargin))
        return formatter.format(today.plus(period))
    }

    fun nextRevisionDate(card: ImmutableCard, isKnown: Boolean, newCardStatus: String): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        val lastRevised = lastRevisionDate(card)
        val nextRevision = getNextRevisionDate(card)

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