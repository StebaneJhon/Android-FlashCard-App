package com.example.flashcard.backend

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.relations.DeckWithCards
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {

    // Deck Query
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeck(deck: Deck)

    @Delete()
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT * FROM deck")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM deck WHERE deck_name LIKE :searchQuery OR deck_description LIKE :searchQuery OR deck_first_language LIKE :searchQuery OR deck_second_language LIKE :searchQuery OR deck_color_code LIKE :searchQuery")
    fun searchDeck(searchQuery: String): Flow<List<Deck>>

    @Update()
    suspend fun updateDeck(deck: Deck)

    // Cards Query
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card)

    @Delete()
    suspend fun deleteCard(card: Card)

    @Query("DELETE FROM card WHERE deckId = :deckId")
    suspend fun deleteCards(deckId: Int)

    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckById(deckId: Int): Deck

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards>

    @Update()
    suspend fun updateCard(card: Card)

    @Query("SELECT * FROM card WHERE deckId = :deckId AND (card_content LIKE :searchQuery OR content_definition LIKE :searchQuery OR card_value LIKE :searchQuery OR value_definition LIKE :searchQuery)")
    fun searchCard(searchQuery: String, deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM card WHERE deckId = :deckId")
    fun getCards(deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM card")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM user")
    fun getUser(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM spaceRepetitionBox")
    fun getBox(): Flow<List<SpaceRepetitionBox>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoxLevel(boxLevel: SpaceRepetitionBox)

    @Update
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox)
}