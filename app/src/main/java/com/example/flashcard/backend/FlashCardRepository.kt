package com.example.flashcard.backend

import androidx.annotation.WorkerThread
import com.example.flashcard.entities.Card
import com.example.flashcard.entities.Deck
import com.example.flashcard.entities.relations.DeckWithCards
import kotlinx.coroutines.flow.Flow

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    val allDecks: Flow<List<Deck>> = flashCardDao.getAllDecks()

    @WorkerThread
    fun getDeckWithCards(deckId: Int): Flow<List<DeckWithCards>> {
        return flashCardDao.getDeckWithCards(deckId)
    }

    @WorkerThread
    suspend fun deleteCard(card: Card) {
        flashCardDao.deleteCard(card)
    }

    @WorkerThread
    suspend fun deleteDeck(deck: Deck): String {
        return if (deck.cardSum == null || deck.cardSum == 0) {
            flashCardDao.deleteDeck(deck)
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
    suspend fun insertCard(card: Card) {
        flashCardDao.insertCard(card)
    }

    @WorkerThread
    suspend fun updateCard(card: Card) {
        flashCardDao.updateCard(card)
    }

    @WorkerThread
    suspend fun updateDeck(deck: Deck) {
        flashCardDao.updateDeck(deck)
    }

    @WorkerThread
    fun searchDeck(searchQuery: String): Flow<List<Deck>> {
        return flashCardDao.searchDeck(searchQuery)
    }

}