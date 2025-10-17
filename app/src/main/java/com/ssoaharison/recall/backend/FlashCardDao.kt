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
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.entities.relations.DeckWithCardsAndContentAndDefinitions
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoxLevel(boxLevel: SpaceRepetitionBox)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card)

//    @Transaction
//    suspend fun insertCardWithDefinition(card: ImmutableCard) {
//        val localCard = card.toLocal()
//        insertCard(localCard)
//        card.cardContent?.let { content ->
//            insertCardContent(content)
//        }
//        card.cardDefinition?.let { definitions ->
//            definitions.forEach { definition ->
//                insertCardDefinition(definition)
//            }
//        }
//    }

    @Transaction
    suspend fun insertCardWithContentAndDefinitions(cardWithContentAndDefinitions: CardWithContentAndDefinitions) {
        insertCard(cardWithContentAndDefinitions.card)
        insertCardContent(cardWithContentAndDefinitions.contentWithDefinitions?.content!!)
        cardWithContentAndDefinitions.contentWithDefinitions.definitions.forEach { definition ->
            insertCardDefinition(definition)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardContent(cardContent: CardContent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardDefinition(cardDefinition: CardDefinition)

    @Query("SELECT * FROM deck")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM deck WHERE parentDeckId IS NULL")
    fun getPrimaryDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM deck WHERE deckId = '000000'")
    suspend fun getMainDeck(): Deck?

    @Query("SELECT * FROM deck WHERE parentDeckId = :deckId")
    fun getSubdecks(deckId: String): Flow<List<Deck>>

    @Query("SELECT COUNT(*) FROM deck")
    suspend fun getDeckCount(): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckOwnerId = :deckId")
    suspend fun countCardsInDeck(deckId: String): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckOwnerId = :deckId AND cardLevel <> 'L1'")
    suspend fun countKnownCardsInDeck(deckId: String): Int

    @Query("SELECT COUNT(*) FROM card WHERE deckOwnerId = :deckId AND cardLevel = 'L1'")
    suspend fun countUnKnownCardsInDeck(deckId: String): Int

    @Query("SELECT * FROM deck WHERE deckName LIKE :searchQuery OR deckDescription LIKE :searchQuery OR cardContentDefaultLanguage LIKE :searchQuery OR cardDefinitionDefaultLanguage LIKE :searchQuery OR deckColorCode LIKE :searchQuery")
    fun searchDeck(searchQuery: String): Flow<List<Deck>>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    fun getDeckWithCards(deckId: String): Flow<DeckWithCardsAndContentAndDefinitions>

//    @Query("SELECT * FROM deck WHERE parentDeckId = :deckId")
//    fun getSubdecks(deckId: String): Flow<List<Deck>>

    @Transaction
    @Query("SELECT * FROM deck WHERE deckId = :deckId")
    suspend fun gettDeckWithCards(deckId: String): DeckWithCardsAndContentAndDefinitions

//    @Transaction
//    @Query("SELECT * FROM cardContent WHERE cardOwnerId = :cardId")
//    suspend fun getCardAndContent(cardId: String): CardAndContent
//
//    @Transaction
//    @Query("SELECT * FROM cardDefinition WHERE cardOwnerId = :cardId")
//    suspend fun getCardWithDefinition(cardId: String): CardWithDefinitions

    @Query(
        "SELECT * FROM card " +
        "JOIN cardContent ON cardContent.cardOwnerId = card.cardId " +
        "JOIN cardDefinition ON cardDefinition.cardOwnerId = card.cardId " +
        "WHERE cardContent.contentText LIKE :searchQuery AND card.deckOwnerId = :deckId OR cardDefinition.definitionText LIKE :searchQuery AND card.deckOwnerId = :deckId OR card.cardType LIKE :searchQuery AND card.deckOwnerId = :deckId"
    )
    fun searchCard(searchQuery: String, deckId: String): Flow<List<CardWithContentAndDefinitions>>

    @Query("SELECT * FROM card WHERE deckOwnerId = :deckId")
    suspend fun getCards(deckId: String): List<Card>

    @Query("SELECT * FROM card")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM card")
    suspend fun getCardCount(): Int

    @Query("SELECT COUNT(*) FROM card WHERE cardLevel <> 'L1' ")
    suspend fun getKnownCardCount(): Int

    @Query("SELECT * FROM spaceRepetitionBox")
    fun getBox(): Flow<List<SpaceRepetitionBox>>

    @Update()
    suspend fun updateDeck(deck: Deck)

    @Query("UPDATE deck SET cardContentDefaultLanguage = :language WHERE deckId = :deckId")
    suspend fun updateDefaultCardContentLanguage(deckId: String, language: String)

    @Query("UPDATE deck SET cardDefinitionDefaultLanguage = :language WHERE deckId = :deckId")
    suspend fun updateDefaultCardDefinitionLanguage(deckId: String, language: String)

    @Update()
    suspend fun updateCard(card: Card)

//    @Transaction
//    suspend fun updateCardWithContentAndDefinition(card: ImmutableCard) {
//        card.cardContent?.let { content ->
//            updateCardContent(content)
//        }
//        card.cardDefinition?.let { definitions ->
//            definitions.forEach { actualDefinition ->
//                when {
//                    actualDefinition.definitionText != null && actualDefinition.definitionText.isEmpty()  -> {
//                        deleteCardDefinition(actualDefinition)
//                    }
//                    actualDefinition.definitionId == null -> {
//                        insertCardDefinition(actualDefinition)
//                    }
//                    else -> {
//                        updateCardDefinition(actualDefinition)
//                    }
//                }
//            }
//        }
//        updateCard(card.toLocal())
//    }

    @Transaction
    suspend fun updateCardWithContentAndDefinition(cardWithContentAndDefinitions: CardWithContentAndDefinitions) {
        cardWithContentAndDefinitions.contentWithDefinitions?.content.let { content ->
            updateCardContent(content!!)
        }
        cardWithContentAndDefinitions.contentWithDefinitions?.definitions.let { definitions ->
            definitions?.forEach { actualDefinition ->
                when {
                    actualDefinition.definitionText != null && actualDefinition.definitionText.isEmpty()  -> {
                        deleteCardDefinition(actualDefinition)
                    }

                    actualDefinition.definitionId == null -> {
                        insertCardDefinition(actualDefinition)
                    }

                    else -> {
                        updateCardDefinition(actualDefinition)
                    }
                }
            }
        }
        updateCard(cardWithContentAndDefinitions.card)
    }

    @Query("UPDATE card SET cardContentLanguage = :language WHERE cardId = :cardId")
    suspend fun updateCardContentLanguage(cardId: String, language: String)

    @Query("UPDATE card SET cardDefinitionLanguage = :language WHERE cardId = :cardId")
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
    suspend fun deleteCardsWithContentAndDefinition(cards: List<CardWithContentAndDefinitions>) {
        cards.forEach { cardWithDefinitions ->
            deleteCardWithContentAndDefinition(cardWithDefinitions)
        }
    }

//    @Transaction
//    suspend fun deleteCardWithContentAndDefinition(card: Card) {
//        val cardContent = getCardAndContent(card.cardId).cardContent
//        val cardDefinitions = getCardWithDefinition(card.cardId).definition
//        cardDefinitions.forEach { cardDefinition ->
//            deleteCardDefinition(cardDefinition)
//        }
//        deleteCardContent(cardContent)
//        deleteCard(card)
//    }

    @Transaction
    suspend fun deleteCardWithContentAndDefinition(cardWithContentAndDefinition: CardWithContentAndDefinitions) {
        cardWithContentAndDefinition.contentWithDefinitions?.definitions?.forEach { cardDefinition ->
            deleteCardDefinition(cardDefinition)
        }
        deleteCardContent(cardWithContentAndDefinition.contentWithDefinitions!!.content)
        deleteCard(cardWithContentAndDefinition.card)
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
        deleteCardsWithContentAndDefinition(deckWithCards.cards!!)
        deleteDeck(deckWithCards.deck)
    }
}