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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    // Decks
    @WorkerThread
    fun allDecks(): Flow<List<ImmutableDeck>> {
        return flashCardDao.getAllDecks().map { decks ->
            decks.toExternal()
        }
    }

    /*
    @WorkerThread
    fun allCards(): Flow<List<ImmutableCard>> {
        return flashCardDao.getAllCards().map { cards ->
            cards.toExternal()
        }
    }

     */

    @WorkerThread
    suspend fun allCards(): Flow<List<ImmutableCard>> {
        val cards = flashCardDao.getAllCards().map { cardList ->
            cardList.map { card ->
                val cardContent = flashCardDao.getCardAndContent(card.cardId!!).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }
        }
        return cards
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
    suspend fun getImmutableDeckWithCards(deckId: Int): Flow<ImmutableDeckWithCards> {
        val deckWithCards = flashCardDao.getDeckWithCards(deckId)
        val immutableDeckWithCards =
            deckWithCards.map { localDeckWithCards ->
            val deck = localDeckWithCards.deck.toExternal()
            val cardList = mutableListOf<ImmutableCard>()

            localDeckWithCards.cards.forEach {
                val cardContent = flashCardDao.getCardAndContent(it.cardId!!).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(it.cardId).definition

                val immutableCard = ImmutableCard(
                    it.cardId,
                    cardContent,
                    it.contentDescription,
                    cardDefinitions,
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
                    it.creationDateTime
                )

                cardList.add(immutableCard)

            }

            localDeckWithCards.toExternal(deck, cardList)
        }

        return immutableDeckWithCards
    }


    @WorkerThread
    suspend fun getCardAndContent(cardId: Int): CardAndContent {
        return flashCardDao.getCardAndContent(cardId)
    }

    @WorkerThread
    suspend fun getCardWithDefinition(cardId: Int): CardWithDefinitions {
        return flashCardDao.getCardWithDefinition(cardId)
    }

    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

    @WorkerThread
    suspend fun insertCard(card: ImmutableCard, deck: Deck) {

        val localDeck = flashCardDao.getDeckById(deck.deckId!!)
        val newDeck = Deck(
            deckId = localDeck.deckId,
            deckName = localDeck.deckName,
            deckDescription = localDeck.deckDescription,
            deckFirstLanguage = localDeck.deckFirstLanguage,
            deckSecondLanguage = localDeck.deckSecondLanguage,
            deckColorCode = localDeck.deckColorCode,
            cardSum = localDeck.cardSum?.plus(1),
            category = localDeck.category,
            isFavorite = localDeck.isFavorite,
            deckCreationDate = localDeck.deckCreationDate
        )
        flashCardDao.updateDeck(newDeck)

        val localCard = card.toLocal()
        flashCardDao.insertCard(localCard)
        val actualCard = flashCardDao.getCardByCreationDateTime(card.creationDateTime!!)
        val cardContent = card.cardContent
        cardContent?.cardId = actualCard.cardId
        flashCardDao.insertCardContent(cardContent!!)
        val cardDefinition = card.cardDefinition
        cardDefinition?.forEach {
            it.cardId = actualCard.cardId
            flashCardDao.insertCardDefinition(it)
        }
    }

    @WorkerThread
    fun searchCard(searchQuery: String, deckId: Int): Flow<List<ImmutableCard>> {
        return flashCardDao.searchCard(searchQuery, deckId).map {cardList ->
            cardList.map { card ->
                val cardContent = flashCardDao.getCardAndContent(card.cardId!!).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }
        }
    }

    @WorkerThread
    suspend fun searchCardImmutable(searchQuery: String, deckId: Int): Flow<List<ImmutableCard>> {

        val cards = flashCardDao.searchCardNoFlow(searchQuery, deckId)
        val immutableCardList = mutableListOf<ImmutableCard>()

        cards.forEach {
            val cardContent = flashCardDao.getCardAndContent(it.cardId!!).cardContent
            val cardDefinitions = flashCardDao.getCardWithDefinition(it.cardId).definition
            val immutableCard = ImmutableCard(
                it.cardId,
                cardContent,
                it.contentDescription,
                cardDefinitions,
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
                it.creationDateTime
            )

            immutableCardList.add(immutableCard)
        }

        return flowOf(immutableCardList)
    }

    @WorkerThread
    suspend fun getCards(deckId: Int): ImmutableCard {
        val card = flashCardDao.getCards(deckId)
        val cardContent = flashCardDao.getCardAndContent(card.cardId!!).cardContent
        val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
        return card.toExternal(cardContent, cardDefinitions)
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
    suspend fun updateCard(card: ImmutableCard) {
        flashCardDao.updateCardContent(card.cardContent!!)
        card.cardDefinition?.forEach {
            flashCardDao.updateCardDefinition(it)
        }
        flashCardDao.updateCard(card.toLocal())
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
    suspend fun deleteCard(card: ImmutableCard, deck: Deck) {
        flashCardDao.deleteCardContent(card.cardContent!!)
        card.cardDefinition?.forEach {
            flashCardDao.deleteCardDefinition(it)
        }
        flashCardDao.deleteCard(card.toLocal())
        deck.cardSum = deck.cardSum?.minus(1)
        flashCardDao.updateDeck(deck)
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