package com.example.flashcard.backend

import androidx.annotation.WorkerThread
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    @WorkerThread
    fun allDecks(): Flow<List<ImmutableDeck>> {
        return flashCardDao.getAllDecks().map { decks ->
            decks.toExternal()
        }
    }

    @WorkerThread
    suspend fun allCards(): Flow<List<ImmutableCard?>> {
        val cards = flashCardDao.getAllCards().map { cardList ->
            cardList.map { card ->
                card.cardId.let { cardId ->
                    cardId.let { id ->
                        val cardContent = flashCardDao.getCardAndContent(id).cardContent
                        val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
                        card.toExternal(cardContent, cardDefinitions)
                    }
                }
            }
        }
        return cards
    }

    @WorkerThread
    suspend fun getDeckCount() = flashCardDao.getDeckCount()

    @WorkerThread
    suspend fun getCardCount() = flashCardDao.getCardCount()

    @WorkerThread
    suspend fun getKnownCardCount() = flashCardDao.getKnownCardCount()

    @WorkerThread
    fun searchDeck(searchQuery: String): Flow<List<ImmutableDeck>> {
        return flashCardDao.searchDeck(searchQuery).map {
            it.toExternal()
        }
    }

    @WorkerThread
    suspend fun getImmutableDeckWithCards(deckId: String): Flow<ImmutableDeckWithCards> {
        val deckWithCards = flashCardDao.getDeckWithCards(deckId)
        val immutableDeckWithCards =
            deckWithCards.map { localDeckWithCards ->
                val deck = localDeckWithCards.deck.toExternal()
                val cardList = localDeckWithCards.cards.map { card ->
                    card.cardId.let { cardId ->
                        cardId.let { id ->
                            val cardContent = flashCardDao.getCardAndContent(id).cardContent
                            val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
                            card.toExternal(cardContent, cardDefinitions)
                        }

                    }
                }
                localDeckWithCards.toExternal(deck, cardList)
            }

        return immutableDeckWithCards
    }

    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

    @WorkerThread
    suspend fun insertCard(
        card: ImmutableCard,
        deck: ImmutableDeck,
        incrementDeckCardSumByOne: Boolean
    ) {

        val localDeck = deck.toLocal()
        if (incrementDeckCardSumByOne) {
            localDeck.cardSum = localDeck.cardSum?.plus(1)
            flashCardDao.updateDeck(localDeck)
        }
        val localCard = card.toLocal()
        flashCardDao.insertCard(localCard)
        val cardContent = card.cardContent
        flashCardDao.insertCardContent(cardContent!!)
        val cardDefinition = card.cardDefinition
        cardDefinition?.forEach {
            flashCardDao.insertCardDefinition(it)
        }
    }

    @WorkerThread
    suspend fun insertCards(cards: List<ImmutableCard>, deck: ImmutableDeck) {
        val localDeck = deck.toLocal()
        localDeck.cardSum = localDeck.cardSum?.plus(cards.size)
        flashCardDao.updateDeck(localDeck)
        cards.forEach { card ->
//            delay(300)
            insertCard(card, deck, false)
        }
    }

    @WorkerThread
    fun searchCard(searchQuery: String): Flow<List<ImmutableCard>> {
        return flashCardDao.searchCard(searchQuery).map { cardList ->
            cardList.map { card ->
                val cardContent = flashCardDao.getCardAndContent(card.cardId).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }
        }
    }

    @WorkerThread
    suspend fun getCards(deckId: String): List<ImmutableCard?> {
        val cards = flashCardDao.getCards(deckId)
        return cards.map { card ->
            card.cardId.let { cardId ->
                cardId.let { id ->
                    val cardContent = flashCardDao.getCardAndContent(id).cardContent
                    val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
                    card.toExternal(cardContent, cardDefinitions)
                }
            }

        }
    }

    @WorkerThread
    fun getBox(): Flow<List<ImmutableSpaceRepetitionBox>> {
        return flashCardDao.getBox().map { it.toExternal() }
    }

    @WorkerThread
    suspend fun insertBoxLevel(boxLevel: SpaceRepetitionBox) {
        flashCardDao.insertBoxLevel(boxLevel)
    }


    @WorkerThread
    suspend fun updateDeck(deck: Deck) {
        flashCardDao.updateDeck(deck)
    }

    @WorkerThread
    suspend fun updateCard(card: ImmutableCard) {
        flashCardDao.updateCardContent(card.cardContent!!)
        card.cardDefinition?.forEach {
            when {
                it.definition.isEmpty() -> {
                    flashCardDao.deleteCardDefinition(it)
                }

                it.definitionId == null -> {
                    flashCardDao.insertCardDefinition(it)
                }

                else -> {
                    flashCardDao.updateCardDefinition(it)
                }
            }

        }
        flashCardDao.updateCard(card.toLocal())
    }

    @WorkerThread
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox) {
        flashCardDao.updateBoxLevel(boxLevel)
    }

    @WorkerThread
    suspend fun deleteCard(card: ImmutableCard?, deck: Deck) {
        card?.let { actualCard ->
            actualCard.cardContent?.let { it1 ->
                flashCardDao.deleteCardContent(it1)
            }
            actualCard.cardDefinition?.forEach {
                flashCardDao.deleteCardDefinition(it)
            }
            flashCardDao.deleteCard(card.toLocal())
            deck.cardSum = deck.cardSum?.minus(1)
            flashCardDao.updateDeck(deck)
        }
    }

    @WorkerThread
    suspend fun deleteCards(deck: ImmutableDeck) {
        val localDeck = deck.toLocal()
        val cards = getCards(localDeck.deckId)
        cards.forEach { card ->
            delay(300)
            deleteCard(card, localDeck)
        }
    }

    @WorkerThread
    suspend fun deleteDeckWithCards(deck: ImmutableDeck) {
        deleteCards(deck)
        flashCardDao.deleteDeck(deck.toLocal())
    }

}