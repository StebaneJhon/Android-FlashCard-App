package com.ssoaharison.recall.backend

import androidx.annotation.WorkerThread
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.models.toExternal
import com.ssoaharison.recall.backend.models.toLocal
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    @WorkerThread
    fun allDecks(): Flow<List<ImmutableDeck>> {
        return flashCardDao.getAllDecks().map { decks ->
            decks.map { deck ->
                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            }
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
    fun searchDeck(searchQuery: String): Flow<Set<ImmutableDeck>> {
        return flashCardDao.searchDeck(searchQuery).map { decks ->
            decks.map { deck ->
                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            }.toSet()
        }
    }

    @WorkerThread
    suspend fun getImmutableDeckWithCards(deckId: String): Flow<ImmutableDeckWithCards> {
        val deckWithCards = flashCardDao.getDeckWithCards(deckId) ?: emptyFlow()
        return deckWithCards.map { localDeckWithCards ->
            val cardCount = flashCardDao.countCardsInDeck(localDeckWithCards.deck.deckId)
            val knownCardCount = flashCardDao.countKnownCardsInDeck(localDeckWithCards.deck.deckId)
            val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(localDeckWithCards.deck.deckId)
            val deck = localDeckWithCards.deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
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
    }


    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

    @WorkerThread
    suspend fun insertCard(
        card: ImmutableCard,
    ) {

//        val localCard = card.toLocal()
//        flashCardDao.insertCard(localCard)
//        val cardContent = card.cardContent
//        flashCardDao.insertCardContent(cardContent!!)
//        val cardDefinition = card.cardDefinition
//        cardDefinition?.forEach {
//            flashCardDao.insertCardDefinition(it)
//        }
        flashCardDao.insertCardWithDefinition(card)
    }

    @WorkerThread
    suspend fun insertCards(cards: List<ImmutableCard>) {
        cards.forEach { card ->
            insertCard(card)
        }
    }

    @WorkerThread
    fun searchCard(searchQuery: String, deckId: String): Flow<Set<ImmutableCard>> {
        return flashCardDao.searchCard(searchQuery, deckId).map { cardList ->
            cardList.map { card ->
                val cardContent = flashCardDao.getCardAndContent(card.cardId).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }.toSet()
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

    suspend fun updateDefaultCardContentLanguage(deckId: String, language: String) {
        flashCardDao.updateDefaultCardContentLanguage(deckId, language)
    }

    suspend fun updateDefaultCardDefinitionLanguage(deckId: String, language: String) {
        flashCardDao.updateDefaultCardDefinitionLanguage(deckId, language)
    }

    suspend fun updateCardContentLanguage(cardId: String, language: String) {
        flashCardDao.updateCardContentLanguage(cardId, language)
    }

    suspend fun updateCardDefinitionLanguage(cardId: String, language: String) {
        flashCardDao.updateCardDefinitionLanguage(cardId, language)
    }

    @WorkerThread
    suspend fun updateCardWithContentAndDefinition(card: ImmutableCard) {
//        flashCardDao.updateCardContent(card.cardContent!!)
//        card.cardDefinition?.forEach {
//            when {
//                it.definition.isEmpty() -> {
//                    flashCardDao.deleteCardDefinition(it)
//                }
//
//                it.definitionId == null -> {
//                    flashCardDao.insertCardDefinition(it)
//                }
//
//                else -> {
//                    flashCardDao.updateCardDefinition(it)
//                }
//            }
//
//        }
//        flashCardDao.updateCard(card.toLocal())
        flashCardDao.updateCardWithContentAndDefinition(card)
    }

    @WorkerThread
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox) {
        flashCardDao.updateBoxLevel(boxLevel)
    }

    @WorkerThread
    suspend fun deleteCard(card: ImmutableCard) {
//        card?.let { actualCard ->
//            actualCard.cardContent?.let { it1 ->
//                flashCardDao.deleteCardContent(it1)
//            }
//            actualCard.cardDefinition?.forEach {
//                flashCardDao.deleteCardDefinition(it)
//            }
//            flashCardDao.deleteCard(card.toLocal())
//        }
        flashCardDao.deleteCardWithContentAndDefinition(card.toLocal())
    }

    @WorkerThread
    suspend fun deleteCards(cards: List<Card?>) {
//        val localDeck = deck.toLocal()
//        val cards = getCards(localDeck.deckId)

        flashCardDao.deleteCardsWithContentAndDefinition(cards)
//        cards.forEach { card ->
////            delay(300)
//            deleteCard(card!!)
//        }
    }


    @WorkerThread
    suspend fun deleteDeckWithCards(d: ImmutableDeck) {
//        val deckWithCards = gettDeckWithCards(d.deckId)
//        deleteCards(deckWithCards.cards)
//        flashCardDao.deleteDeck(deckWithCards.deck)
        flashCardDao.deleteDeckWithCards(d.deckId)
    }

}