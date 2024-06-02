package com.example.flashcard.util

import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardLevel.L2
import com.example.flashcard.util.CardLevel.L3
import com.example.flashcard.util.CardLevel.L4
import com.example.flashcard.util.CardLevel.L5
import com.example.flashcard.util.CardLevel.L6
import com.example.flashcard.util.CardLevel.L7
import com.example.flashcard.util.LevelColors.BROWNE
import com.example.flashcard.util.LevelColors.GREEN500
import com.example.flashcard.util.LevelColors.GREEN700
import com.example.flashcard.util.LevelColors.ORANGE
import com.example.flashcard.util.LevelColors.RED
import com.example.flashcard.util.LevelColors.YELLOW500
import com.example.flashcard.util.LevelColors.YELLOW700
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class SpaceRepetitionAlgorithmHelper{

    var boxLevels: List<ImmutableSpaceRepetitionBox>? = null
    private val box = FlashCardApplication().repository.getBox()
    private var fetchJob: Job? = null

    init {
        initSpaceRepetitionAlgorithmHelper()
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun initSpaceRepetitionAlgorithmHelper() {
        fetchJob?.cancel()
        fetchJob = GlobalScope.launch {
            box.collect {
                boxLevels = it
            }
        }
    }

    fun getActualBoxLevels(): List<ImmutableSpaceRepetitionBox>? {
        return boxLevels
    }

    private val colors = mapOf(
        RED to R.color.red700,
        ORANGE to R.color.orange,
        BROWNE to R.color.brown500,
        YELLOW700 to R.color.yellow700,
        YELLOW500 to R.color.yellow500,
        GREEN500 to R.color.green500,
        GREEN700 to R.color.green700
    )

    private val carBackgroundColors = mapOf(
        RED to R.color.red50,
        ORANGE to R.color.orange50,
        BROWNE to R.color.brown50,
        YELLOW700 to R.color.yellow50,
        YELLOW500 to R.color.yellow100,
        GREEN500 to R.color.green50,
        GREEN700 to R.color.green100
    )



    fun selectBoxLevelColor(color: String): Int? {
        return if (color in colors.keys) {
            colors[color]
        } else {
            R.color.red700
        }
    }

    fun selectBackgroundLevelColor(color: String): Int? {
        return if (color in carBackgroundColors.keys) {
            carBackgroundColors[color]
        } else {
            R.color.red100
        }
    }

    fun getInitialSpaceRepetitionBox(): List<SpaceRepetitionBox> {
        return listOf(
            SpaceRepetitionBox(null, L1, "Red", 0, revisionMargin(0)),
            SpaceRepetitionBox(null, L2, "Orange", 1, revisionMargin(1)),
            SpaceRepetitionBox(null, L3, "Brown", 2, revisionMargin(2)),
            SpaceRepetitionBox(null, L4, "Yellow700", 4, revisionMargin(4)),
            SpaceRepetitionBox(null, L5, "Yellow500", 7, revisionMargin(7)),
            SpaceRepetitionBox(null, L6, "Green500", 14, revisionMargin(14)),
            SpaceRepetitionBox(null, L7, "Green700", 31, revisionMargin(31)),
        )
    }

    fun revisionMargin(repeatDay: Int): Int {
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
                val newRepeatDay = boxLevels?.get(0)?.levelRepeatIn?.plus(boxLevels!![0].levelRevisionMargin!!) ?: 1
                //val days = box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin)
                val period = Period.of(0, 0, newRepeatDay)
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
            val newRepeatDay = boxLevels?.get(0)?.levelRepeatIn?.plus(boxLevels!![0].levelRevisionMargin!!) ?: 1
            //val days = box["L1"]?.repeatDay!!.plus(box[L1]!!.revisionMargin)
            val period = Period.of(0, 0, newRepeatDay)
            val nextDate = today.plus(period)
            val result = formatter.format(nextDate)
            return result
        }

        card.cardStatus?.let {
            val newBoxLevel = boxLevels?.let { it1 -> getBoxLevelByStatus(it1, newCardStatus) }
            val newRepeatDay = newBoxLevel?.levelRepeatIn?.plus(newBoxLevel.levelRevisionMargin!!) ?: 1
            //val days = box[newCardStatus]?.repeatDay!!.plus(box[newCardStatus]!!.revisionMargin)
            val period = Period.of(0, 0, newRepeatDay)
            val newDate = today.plus(period)
            return formatter.format(newDate)
        }

        val newBoxLevel = boxLevels?.let { getBoxLevelByStatus(it, L2) }
        val newRepeatDay = newBoxLevel?.levelRepeatIn?.plus(newBoxLevel.levelRevisionMargin!!) ?: 2
        //val days = box["L2"]?.repeatDay!!.plus(box[L2]!!.revisionMargin)
        val period = Period.of(0, 0, newRepeatDay)
        return formatter.format(today.plus(period))
    }

    fun getBoxLevelByStatus(
        boxLevels: List<ImmutableSpaceRepetitionBox>,
        statusLevel: String
    ): ImmutableSpaceRepetitionBox? {
        boxLevels.forEach { boxLevel ->
            if (boxLevel.levelName == statusLevel) {
                return boxLevel
            }
        }
        return null
    }

    fun nextRevisionDate(card: ImmutableCard, isKnown: Boolean, newCardStatus: String): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        val lastRevised = lastRevisionDate(card)
        val nextRevision = getNextRevisionDate(card)

        lastRevised?.let {
            if (it == today && !isKnown) {
                val repeatIn = boxLevels?.let { it1 -> getBoxLevelByStatus(it1, L1)?.levelRepeatIn }
                    ?: 1
                val period = Period.of(0, 0, repeatIn)
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
            val repeatIn = boxLevels?.let { getBoxLevelByStatus(it, L1)?.levelRepeatIn } ?: 1
            val period = Period.of(0, 0, repeatIn)
            val nextDate = today.plus(period)
            val result = formatter.format(nextDate)
            return result
        }

        card.cardStatus?.let {
            val newBoxLevel = boxLevels?.let { it1 -> getBoxLevelByStatus(it1, newCardStatus) }
            val repeatIn = newBoxLevel?.levelRepeatIn ?: 1
            val period = Period.of(0, 0, repeatIn)
            val newDate = today.plus(period)
            return formatter.format(newDate)
        }
        val repeatIn = boxLevels?.let { getBoxLevelByStatus(it, L2)?.levelRepeatIn } ?: 1
        val period = Period.of(0, 0, repeatIn)
        return formatter.format(today.plus(period))
    }

}