package com.soaharisonstebane.mneme.helper

import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.models.ImmutableSpaceRepetitionBox
import com.soaharisonstebane.mneme.backend.models.toExternal
import com.soaharisonstebane.mneme.backend.entities.SpaceRepetitionBox
import com.soaharisonstebane.mneme.backend.models.ExternalCard
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.util.CardLevel.L1
import com.soaharisonstebane.mneme.util.CardLevel.L2
import com.soaharisonstebane.mneme.util.CardLevel.L3
import com.soaharisonstebane.mneme.util.CardLevel.L4
import com.soaharisonstebane.mneme.util.CardLevel.L5
import com.soaharisonstebane.mneme.util.CardLevel.L6
import com.soaharisonstebane.mneme.util.CardLevel.L7
import com.soaharisonstebane.mneme.util.LevelColors.BLUE
import com.soaharisonstebane.mneme.util.LevelColors.CYAN
import com.soaharisonstebane.mneme.util.LevelColors.GREEN
import com.soaharisonstebane.mneme.util.LevelColors.LIME
import com.soaharisonstebane.mneme.util.LevelColors.ORANGE
import com.soaharisonstebane.mneme.util.LevelColors.RED
import com.soaharisonstebane.mneme.util.LevelColors.YELLOW
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
    private val repo = FlashCardApplication().repository
    private var fetchJob: Job? = null

    init {
        initSpaceRepetitionAlgorithmHelper()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun initSpaceRepetitionAlgorithmHelper() {
        fetchJob?.cancel()
        fetchJob = GlobalScope.launch {
            val box = repo.getBox()
            box.collect {
                if (it.isEmpty()) {
                    val localBoxLevel = getInitialSpaceRepetitionBox()
                    localBoxLevel.forEach { boxLevel ->
                        repo.insertBoxLevel(boxLevel)
                    }
                    boxLevels = localBoxLevel.toExternal()
                } else {
                    boxLevels = it
                }
            }
        }
    }

    fun getActualBoxLevels(): List<ImmutableSpaceRepetitionBox> {

        return boxLevels ?: getInitialSpaceRepetitionBox().toExternal()
    }

    private val colors = mapOf(
        RED to R.color.red600,
        ORANGE to R.color.orange600,
        YELLOW to R.color.yellow600,
        LIME to R.color.lime600,
        GREEN to R.color.green600,
        CYAN to R.color.cyan600,
        BLUE to R.color.blue600,
    )

    private val carBackgroundColors = mapOf(
        RED to R.color.red50,
        ORANGE to R.color.orange50,
        YELLOW to R.color.yellow50,
        LIME to R.color.lime50,
        GREEN to R.color.green50,
        CYAN to R.color.cyan50,
        BLUE to R.color.blue50,
    )

    private val cardOnSurfaceColors = mapOf(
        RED to R.color.red950,
        ORANGE to R.color.orange950,
        YELLOW to R.color.yellow950,
        LIME to R.color.lime950,
        GREEN to R.color.green950,
        CYAN to R.color.cyan950,
        BLUE to R.color.blue950,
    )

    private val cardOnSurfaceColorsVariant = mapOf(
        RED to R.color.red900,
        ORANGE to R.color.orange900,
        YELLOW to R.color.yellow900,
        LIME to R.color.lime900,
        GREEN to R.color.green900,
        CYAN to R.color.cyan900,
        BLUE to R.color.blue900,
    )

    private val cardOnSurfaceColorsLight = mapOf(
        RED to R.color.red50,
        ORANGE to R.color.orange50,
        YELLOW to R.color.yellow50,
        LIME to R.color.lime50,
        GREEN to R.color.green50,
        CYAN to R.color.cyan50,
        BLUE to R.color.blue50,
    )

    private val cardOnSurfaceColorsLightVariant = mapOf(
        RED to R.color.red100,
        ORANGE to R.color.orange100,
        YELLOW to R.color.yellow100,
        LIME to R.color.lime100,
        GREEN to R.color.green100,
        CYAN to R.color.cyan100,
        BLUE to R.color.blue100,
    )

    fun selectBoxLevelColor(level: String): Int {
        return when(level) {
            L1 -> {colors.getOrDefault(RED, R.color.red600)}
            L2 -> {colors.getOrDefault(ORANGE, R.color.orange600)}
            L3 -> {colors.getOrDefault(YELLOW, R.color.yellow600)}
            L4 -> {colors.getOrDefault(LIME, R.color.lime600)}
            L5 -> {colors.getOrDefault(GREEN, R.color.green600)}
            L6 -> {colors.getOrDefault(CYAN, R.color.cyan600)}
            L7 -> {colors.getOrDefault(BLUE, R.color.blue600)}
            else -> {colors.getOrDefault(RED, R.color.red600)}
        }

    }
    fun selectBackgroundLevelColor(color: String): Int {
        return carBackgroundColors.getOrDefault(color, R.color.red100)
    }
    fun selectOnSurfaceColor(color: String): Int {
        return cardOnSurfaceColors.getOrDefault(color, R.color.red950)
    }
    fun selectOnSurfaceColorVariant(color: String): Int {
        return cardOnSurfaceColorsVariant.getOrDefault(color, R.color.red900)
    }

    fun selectOnSurfaceColorLight(color: String): Int {
        return cardOnSurfaceColorsLight.getOrDefault(color, R.color.red50)
    }
    fun selectOnSurfaceColorLightVariant(color: String): Int {
        return cardOnSurfaceColorsLightVariant.getOrDefault(color, R.color.red100)
    }

    private fun getInitialSpaceRepetitionBox(): List<SpaceRepetitionBox> {
        return listOf(
            SpaceRepetitionBox(null, L1, RED, 0, revisionMargin(0)),
            SpaceRepetitionBox(null, L2, ORANGE, 1, revisionMargin(1)),
            SpaceRepetitionBox(null, L3, YELLOW, 2, revisionMargin(2)),
            SpaceRepetitionBox(null, L4, LIME, 4, revisionMargin(4)),
            SpaceRepetitionBox(null, L5, GREEN, 7, revisionMargin(7)),
            SpaceRepetitionBox(null, L6, CYAN, 14, revisionMargin(14)),
            SpaceRepetitionBox(null, L7, BLUE, 31, revisionMargin(31)),
        )
    }

    fun revisionMargin(repeatDay: Int): Int {
        if (repeatDay == 0) {
            return 1
        }
        var result: Double = repeatDay / 2.0
        result = result.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
        return result.toInt()
    }

    fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

    fun isForgotten(card: ExternalCard): Boolean {
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

    fun isToBeRevised(card: ExternalCard): Boolean {
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

    fun status(card: ExternalCard, isKnown: Boolean): String {
        val today = LocalDate.now()
        val lastRevision = lastRevisionDate(card)
        val nextForgetting = getNextForgottenDate(card)
        lastRevision?.let {
            if (lastRevision == today && isKnown && nextForgetting != today && !isToBeRevised(card)) {
                return card.cardLevel!!
            }
        }

        return nextLV(isKnown, card)
    }

    private fun nextLV(
        isKnown: Boolean,
        card: ExternalCard
    ): String {
        return if (isKnown) {
            when (card.cardLevel) {
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

    fun lastRevisionDate(card: ExternalCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.lastRevisionDate.isNullOrEmpty() && card.lastRevisionDate != "0") {
            LocalDate.parse(card.lastRevisionDate, formatter)
        } else {
            null
        }

    }

    fun getNextForgottenDate(card: ExternalCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextMissMemorisationDate.isNullOrEmpty() && card.nextMissMemorisationDate != "0") {
            LocalDate.parse(card.nextMissMemorisationDate, formatter)
        } else {
            null
        }
    }

    fun getNextRevisionDate(card: ExternalCard): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return if (!card.nextRevisionDate.isNullOrEmpty() && card.nextRevisionDate != "0") {
            LocalDate.parse(card.nextRevisionDate, formatter)
        } else {
            null
        }
    }

    fun nextForgettingDate(card: ExternalCard, isKnown: Boolean, newCardStatus: String): String {
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

        card.cardLevel?.let {
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

    fun nextRevisionDate(card: ExternalCard, isKnown: Boolean, newCardStatus: String): String {
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

        card.cardLevel?.let {
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

    fun rescheduleCard(
        card: ExternalCard,
        isKnown: Boolean
    ): Card {
        val newStatus = status(card, isKnown)
        val nextRevision = nextRevisionDate(card, isKnown, newStatus)
        val lastRevision = today()
        val nextForgettingDate = nextForgettingDate(card, isKnown, newStatus)

        return Card(
            cardId = card.cardId,
            deckOwnerId = card.deckOwnerId,
            cardLevel = newStatus,
            cardType = card.cardType,
            revisionTime = card.revisionTime,
            missedTime = card.missedTime,
            creationDate = card.creationDate,
            lastRevisionDate = lastRevision,
            nextMissMemorisationDate = nextForgettingDate,
            nextRevisionDate = nextRevision,
            cardContentLanguage = card.cardContentLanguage,
            cardDefinitionLanguage = card.cardDefinitionLanguage
        )
    }

    fun rescheduleExternalCardWithContentAndDefinitions(
        externalCardWithContentAndDefinitions: ExternalCardWithContentAndDefinitions,
        isKnown: Boolean
    ): ExternalCardWithContentAndDefinitions {
        val newStatus = status(externalCardWithContentAndDefinitions.card, isKnown)
        val nextRevision = nextRevisionDate(externalCardWithContentAndDefinitions.card, isKnown, newStatus)
        val lastRevision = today()
        val nextForgettingDate = nextForgettingDate(externalCardWithContentAndDefinitions.card, isKnown, newStatus)

        val card = externalCardWithContentAndDefinitions.card
        val contentWithDefinitions = externalCardWithContentAndDefinitions.contentWithDefinitions
        val updatedCard = ExternalCard(
            cardId = card.cardId,
            deckOwnerId = card.deckOwnerId,
            cardLevel = newStatus,
            cardType = card.cardType,
            revisionTime = card.revisionTime,
            missedTime = card.missedTime,
            creationDate = card.creationDate,
            lastRevisionDate = lastRevision,
            nextMissMemorisationDate = nextForgettingDate,
            nextRevisionDate = nextRevision,
            cardContentLanguage = card.cardContentLanguage,
            cardDefinitionLanguage = card.cardDefinitionLanguage
        )
        val updateCardWithContentAndDefinitions = ExternalCardWithContentAndDefinitions(
            card = updatedCard,
            contentWithDefinitions = contentWithDefinitions
        )
        return updateCardWithContentAndDefinitions
    }

}