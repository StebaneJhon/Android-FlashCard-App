package com.example.flashcard.backend

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.flashcard.entities.Card
import com.example.flashcard.entities.Deck
import com.example.flashcard.entities.relations.DeckWithCards

@Dao
interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeck(deck: Deck)

    @Delete()
    suspend fun deleteDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card)

    @Delete()
    suspend fun deleteCard(card: Card)

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    suspend fun getDeckWithCards(deckId: String): List<DeckWithCards>
}