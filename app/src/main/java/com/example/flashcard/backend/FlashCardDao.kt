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

    @Query("SELECT COUNT(*) FROM deck")
    suspend fun getDeckCount(): Int

    @Query("SELECT * FROM deck WHERE deck_name LIKE :searchQuery OR deck_description LIKE :searchQuery OR deck_first_language LIKE :searchQuery OR deck_second_language LIKE :searchQuery OR deck_color_code LIKE :searchQuery")
    fun searchDeck(searchQuery: String): Flow<List<Deck>>

    @Query("SELECT * FROM deck WHERE deck_name = :deckName")
    fun getDeckName(deckName: String): Deck

    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    suspend fun getDeckById(deckId: String): Deck

    @Query("DELETE FROM card WHERE deckId = :deckId")
    suspend fun deleteCards(deckId: Int)

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckWithCards(deckId: String): Flow<DeckWithCards>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    suspend fun getSimpleDeckWithCards(deckId: Int): DeckWithCards

    @Transaction
    @Query("SELECT * FROM cardContent WHERE cardId = :cardId")
    suspend fun getCardAndContent(cardId: String): CardAndContent

    @Transaction
    @Query("SELECT * FROM cardDefinition WHERE cardId = :cardId")
    suspend fun getCardWithDefinition(cardId: String): CardWithDefinitions

    @Query(
        "SELECT * FROM card " +
        "JOIN cardContent ON cardContent.cardId = card.cardId " +
        "JOIN cardDefinition ON cardDefinition.cardId = card.cardId " +
        "WHERE cardContent.content LIKE :searchQuery OR cardDefinition.definition LIKE :searchQuery OR card.card_type LIKE :searchQuery"
    )
    fun searchCard(searchQuery: String): Flow<List<Card>>

    @Query("SELECT * FROM card WHERE deckId = :deckId")
    fun getCard(deckId: Int): Card

    @Query("SELECT * FROM card WHERE deckId = :deckId")
    suspend fun getCards(deckId: String): List<Card>

    @Query("SELECT * FROM card WHERE cardId = :cardId")
    suspend fun getCardById(cardId: String): Card

    @Query("SELECT * FROM card")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM card")
    suspend fun getCardCount(): Int

    @Query("SELECT COUNT(*) FROM card WHERE card_status <> 'L1' ")
    suspend fun getKnownCardCount(): Int

    @Query("SELECT * FROM card")
    fun getAllCardsNoFlow(): List<Card>

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