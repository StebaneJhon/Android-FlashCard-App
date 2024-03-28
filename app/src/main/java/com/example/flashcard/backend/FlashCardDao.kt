package com.example.flashcard.backend

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.relations.CardAndContent
import com.example.flashcard.backend.entities.relations.CardWithDefinitions
import com.example.flashcard.backend.entities.relations.DeckWithCards
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoxLevel(boxLevel: SpaceRepetitionBox)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardContent(cardContent: CardContent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardDefinition(cardDefinition: CardDefinition)

    @Query("SELECT * FROM deck")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM deck WHERE deck_name LIKE :searchQuery OR deck_description LIKE :searchQuery OR deck_first_language LIKE :searchQuery OR deck_second_language LIKE :searchQuery OR deck_color_code LIKE :searchQuery")
    fun searchDeck(searchQuery: String): Flow<List<Deck>>

    @Query("DELETE FROM card WHERE deckId = :deckId")
    suspend fun deleteCards(deckId: Int)

    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckById(deckId: Int): Deck

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getSimpleDeckWithCards(deckId: Int): DeckWithCards

    @Transaction
    @Query("SELECT * FROM cardContent WHERE cardId = :cardId")
    fun getCardAndContent(cardId: Int): CardAndContent

    @Transaction
    @Query("SELECT * FROM cardDefinition WHERE cardId = :cardId")
    fun getCardWithDefinition(cardId: Int): CardWithDefinitions

    @Query("SELECT * FROM card WHERE deckId = :deckId AND (card_content LIKE :searchQuery OR content_definition LIKE :searchQuery OR card_value LIKE :searchQuery OR value_definition LIKE :searchQuery)")
    fun searchCard(searchQuery: String, deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM card WHERE deckId = :deckId")
    fun getCards(deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM card")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM user")
    fun getUser(): Flow<List<User>>

    @Query("SELECT * FROM spaceRepetitionBox")
    fun getBox(): Flow<List<SpaceRepetitionBox>>


    @Update()
    suspend fun updateDeck(deck: Deck)

    @Update()
    suspend fun updateCard(card: Card)

    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox)

    @Update
    suspend fun updateCardContent(cardContent: CardContent)

    @Update
    suspend fun updateCardDefinition(cardDefinition: CardDefinition)

    @Delete()
    suspend fun deleteCard(card: Card)

    @Delete()
    suspend fun deleteDeck(deck: Deck)

    @Delete()
    suspend fun deleteCardContent(cardContent: CardContent)

    @Delete()
    suspend fun deleteCardDefinition(cardDefinition: CardDefinition)
}