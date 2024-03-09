package com.example.flashcard.backend

import androidx.annotation.WorkerThread
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.relations.DeckWithCards
import kotlinx.coroutines.flow.Flow
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
    suspend fun deleteDeck(deck: ImmutableDeck): String {
        return if (deck.cardSum == null || deck.cardSum == 0) {
            flashCardDao.deleteDeck(deck.toLocal())
            "Deck deleted successfully"
        } else {
            "Deck deletion failed"
        }
    }

    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

    @WorkerThread
    suspend fun updateDeck(deck: Deck) {
        flashCardDao.updateDeck(deck)
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
    suspend fun deleteCard(card: Card, deck: Deck) {
        if (deck.cardSum!! > 0) {
            deck.cardSum = deck.cardSum?.minus(1)
            flashCardDao.updateDeck(deck)
            flashCardDao.deleteCard(card)
        }
    }

    @WorkerThread
    suspend fun insertCard(card: Card, deck: Deck) {
        deck.cardSum = deck.cardSum?.plus(1)
        flashCardDao.updateDeck(deck)
        flashCardDao.insertCard(card)
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
    fun searchCard(searchQuery: String, deckId: Int): Flow<List<ImmutableCard>> {
        return flashCardDao.searchCard(searchQuery, deckId).map {
            it.toExternal()
        }
    }

    @WorkerThread
    fun getCards(deckId: Int): Flow<List<ImmutableCard>> {
        return flashCardDao.getCards(deckId).map { it.toExternal() }
    }

}