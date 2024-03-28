package com.example.flashcard.backend

import androidx.annotation.WorkerThread
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.Model.ImmutableUser
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.relations.CardAndContent
import com.example.flashcard.backend.entities.relations.CardWithDefinitions
import com.example.flashcard.backend.entities.relations.DeckWithCards
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    // Decks
    @WorkerThread
    fun allDecks(): Flow<List<ImmutableDeck>> {
        return flashCardDao.getAllDecks().map { decks ->
            decks.toExternal()
        }
    }

    @WorkerThread
    fun allCards(): Flow<List<ImmutableCard>> {
        return flashCardDao.getAllCards().map { cards ->
            cards.toExternal()
        }
    }

    @WorkerThread
    fun searchDeck(searchQuery: String): Flow<List<ImmutableDeck>> {
        return flashCardDao.searchDeck(searchQuery).map {
            it.toExternal()
        }
    }

    // Cards
    @WorkerThread
    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards> {
        return flashCardDao.getDeckWithCards(deckId)
    }

    @WorkerThread
    fun getImmutableDeckWithCards(deckId: Int): Flow<ImmutableDeckWithCards> {
        val deckWithCards = flashCardDao.getSimpleDeckWithCards(deckId)
        val immutableDeck = deckWithCards.deck.toExternal()
        val immutableCardList = mutableListOf<ImmutableCard>()
        deckWithCards.cards.forEach {
            val cardContent = flashCardDao.getCardAndContent(it.cardId!!).cardContent.content
            val cardDefinitions = flashCardDao.getCardWithDefinition(it.cardId).definition
            val immutableCard = ImmutableCard(
                it.cardId,
                cardContent,
                it.contentDescription,
                it.cardDefinition,
                it.valueDefinition,
                it.deckId,
                it.backgroundImg,
                it.isFavorite,
                it.revisionTime,
                it.missedTime,
                it.creationDate,
                it.lastRevisionDate,
                it.cardStatus,
                it.nextMissMemorisationDate,
                it.nextRevisionDate,
                it.cardType,
                cardDefinitions
            )
            
            immutableCardList.add(immutableCard)
        }
        return flowOf( ImmutableDeckWithCards(
            immutableDeck,
            immutableCardList
        ))
    }

    @WorkerThread
    fun getCardAndContent(cardId: Int): CardAndContent {
        return flashCardDao.getCardAndContent(cardId)
    }

    @WorkerThread
    fun getCardWithDefinition(cardId: Int): CardWithDefinitions {
        return flashCardDao.getCardWithDefinition(cardId)
    }

    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

    @WorkerThread
    suspend fun insertCard(card: Card, deck: Deck) {
        deck.cardSum = deck.cardSum?.plus(1)
        flashCardDao.updateDeck(deck)
        flashCardDao.insertCard(card)
    }

    @WorkerThread
    fun searchCard(searchQuery: String, deckId: Int): Flow<List<ImmutableCard>> {
        return flashCardDao.searchCard(searchQuery, deckId).map {
            it.toExternal()
        }
    }

    @WorkerThread
    fun getCards(deckId: Int): Flow<List<ImmutableCard>> {
        return flashCardDao.getCards(deckId).map { it.toExternal() }
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
    suspend fun updateCard(card: Card) {
        flashCardDao.updateCard(card)
    }

    @WorkerThread
    suspend fun deleteCards(deckId: Int) {
        flashCardDao.deleteCards(deckId)
    }

    @WorkerThread
    suspend fun createUser(user: User) {
        flashCardDao.createUser(user)
    }

    @WorkerThread
    fun getUser(): Flow<List<ImmutableUser>> {
        return flashCardDao.getUser().map { it.toExternal() }
    }

    @WorkerThread
    suspend fun updateUser(user: User) {
        flashCardDao.updateUser(user)
    }

    @WorkerThread
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox) {
        flashCardDao.updateBoxLevel(boxLevel)
    }

    @WorkerThread
    suspend fun deleteCard(card: Card, deck: Deck) {
        if (deck.cardSum!! > 0) {
            deck.cardSum = deck.cardSum?.minus(1)
            flashCardDao.updateDeck(deck)
            flashCardDao.deleteCard(card)
        }
    }

    @WorkerThread
    suspend fun deleteDeck(deck: ImmutableDeck): String {
        return if (deck.cardSum == null || deck.cardSum == 0) {
            flashCardDao.deleteDeck(deck.toLocal())
            "Deck deleted successfully"
        } else {
            "Deck deletion failed"
        }
    }

}