package com.ssoaharison.recall.backend

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.relations.CardAndContent
import com.ssoaharison.recall.backend.entities.relations.CardWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.DeckWithCards
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.toLocal
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
    suspend fun insertCardContent(cardContent: CardContent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardDefinition(cardDefinition: CardDefinition)

    @Query("SELECT * FROM deck")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT COUNT(*) FROM deck")
    suspend fun getDeckCount(): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckId = :deckId")
    suspend fun countCardsInDeck(deckId: String): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckId = :deckId AND card_status <> 'L1'")
    suspend fun countKnownCardsInDeck(deckId: String): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckId = :deckId AND card_status = 'L1'")
    suspend fun countUnKnownCardsInDeck(deckId: String): Int

    @Query("SELECT * FROM deck WHERE deck_name LIKE :searchQuery OR deck_description LIKE :searchQuery OR card_content_default_language LIKE :searchQuery OR card_definition_default_language LIKE :searchQuery OR deck_color_code LIKE :searchQuery")
    fun searchDeck(searchQuery: String): Flow<List<Deck>>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckWithCards(deckId: String): Flow<DeckWithCards>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    suspend fun gettDeckWithCards(deckId: String): DeckWithCards

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
        "WHERE cardContent.content LIKE :searchQuery AND card.deckId = :deckId OR cardDefinition.definition LIKE :searchQuery AND card.deckId = :deckId OR card.card_type LIKE :searchQuery AND card.deckId = :deckId"
    )
    fun searchCard(searchQuery: String, deckId: String): Flow<List<Card>>

    @Query("SELECT * FROM card WHERE deckId = :deckId")
    suspend fun getCards(deckId: String): List<Card>

    @Query("SELECT * FROM card")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM card")
    suspend fun getCardCount(): Int

    @Query("SELECT COUNT(*) FROM card WHERE card_status <> 'L1' ")
    suspend fun getKnownCardCount(): Int

    @Query("SELECT * FROM spaceRepetitionBox")
    fun getBox(): Flow<List<SpaceRepetitionBox>>

    @Update()
    suspend fun updateDeck(deck: Deck)

    @Query("UPDATE deck SET card_content_default_language = :language WHERE deckId = :deckId")
    suspend fun updateDefaultCardContentLanguage(deckId: String, language: String)

    @Query("UPDATE deck SET card_definition_default_language = :language WHERE deckId = :deckId")
    suspend fun updateDefaultCardDefinitionLanguage(deckId: String, language: String)

    @Update()
    suspend fun updateCard(card: Card)

    @Query("UPDATE card SET card_content_language = :language WHERE cardId = :cardId")
    suspend fun updateCardContentLanguage(cardId: String, language: String)

    @Query("UPDATE card SET card_definition_language = :language WHERE cardId = :cardId")
    suspend fun updateCardDefinitionLanguage(cardId: String, language: String)

    @Update
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox)

    @Update
    suspend fun updateCardContent(cardContent: CardContent)

    @Update
    suspend fun updateCardDefinition(cardDefinition: CardDefinition)

    @Delete()
    suspend fun deleteCard(card: Card)

    @Transaction()
    suspend fun deleteCardsWithContentAndDefinition(cards: List<Card?>) {
        cards.forEach { card ->
            card?.let {
                deleteCardWithContentAndDefinition(it)
            }
        }
    }

    @Transaction
    suspend fun deleteCardWithContentAndDefinition(card: Card) {
        val cardContent = getCardAndContent(card.cardId).cardContent
        val cardDefinitions = getCardWithDefinition(card.cardId).definition
        cardDefinitions.forEach { cardDefinition ->
            deleteCardDefinition(cardDefinition)
        }
        deleteCardContent(cardContent)
        deleteCard(card)
    }

    @Delete()
    suspend fun deleteDeck(deck: Deck)

    @Delete()
    suspend fun deleteCardContent(cardContent: CardContent)

    @Delete()
    suspend fun deleteCardDefinition(cardDefinition: CardDefinition)

    @Transaction
    suspend fun deleteDeckWithCards(deckId: String) {
        val deckWithCards = gettDeckWithCards(deckId)
        deleteCardsWithContentAndDefinition(deckWithCards.cards)
        deleteDeck(deckWithCards.deck)
    }
}