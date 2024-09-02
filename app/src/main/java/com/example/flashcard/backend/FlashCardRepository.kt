package com.example.flashcard.backend

import androidx.annotation.WorkerThread
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.Model.ImmutableUser
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

    @WorkerThread
    fun allDecks(): Flow<List<ImmutableDeck>> {
        return flashCardDao.getAllDecks().map { decks ->
            decks.toExternal()
        }
    }

    @WorkerThread
    suspend fun allCards(): Flow<List<ImmutableCard?>> {
        val cards = flashCardDao.getAllCards().map { cardList ->
            cardList.map { card ->
                card.cardId?.let {cardId ->
                    val cardContent = flashCardDao.getCardAndContent(cardId).cardContent
                    val cardDefinitions = flashCardDao.getCardWithDefinition(cardId).definition
                    card.toExternal(cardContent, cardDefinitions)
                }
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

    @WorkerThread
    suspend fun getDeckByName(title: String): ImmutableDeck {
        val deck = flashCardDao.getDeckName(title)
        return deck.toExternal()
    }

    // Cards

    /*
    @WorkerThread
    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards> {
        return flashCardDao.getDeckWithCards(deckId)
    }

     */

    @WorkerThread
    suspend fun getImmutableDeckWithCards(deckId: Int): Flow<ImmutableDeckWithCards> {
        val deckWithCards = flashCardDao.getDeckWithCards(deckId)
        val immutableDeckWithCards =
            deckWithCards.map { localDeckWithCards ->
            val deck = localDeckWithCards.deck.toExternal()
            val cardList = localDeckWithCards.cards.map {card ->
                card.cardId?.let {cardId ->
                    val cardContent = flashCardDao.getCardAndContent(cardId).cardContent
                    val cardDefinitions = flashCardDao.getCardWithDefinition(cardId).definition
                    card.toExternal(cardContent, cardDefinitions)
                }
            }
            localDeckWithCards.toExternal(deck, cardList)
        }

        return immutableDeckWithCards
    }

    /*
    @WorkerThread
    suspend fun getCardAndContent(cardId: Int): CardAndContent {
        return flashCardDao.getCardAndContent(cardId)
    }

    @WorkerThread
    suspend fun getCardWithDefinition(cardId: Int): CardWithDefinitions {
        return flashCardDao.getCardWithDefinition(cardId)
    }
     */

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
        val actualCard = card.creationDateTime?.let { flashCardDao.getCardByCreationDateTime(it) }
        val cardContent = card.cardContent
        cardContent?.cardId = actualCard?.cardId
        flashCardDao.insertCardContent(cardContent!!)
        val cardDefinition = card.cardDefinition
        cardDefinition?.forEach {
            it.cardId = actualCard?.cardId
            flashCardDao.insertCardDefinition(it)
        }
    }

    @WorkerThread
    suspend fun insertCards(cards: List<ImmutableCard>, deck: Deck) {
        cards.forEach { card ->
            insertCard(card, deck)
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
    suspend fun searchCardImmutable(searchQuery: String, deckId: Int): Flow<List<ImmutableCard?>> {

        val cards = flashCardDao.searchCardNoFlow(searchQuery, deckId)
        val immutableCardList = cards.map { card ->
            card.cardId?.let {cardId ->
                val cardContent = flashCardDao.getCardAndContent(cardId).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }

        }

        return flowOf(immutableCardList)
    }

    @WorkerThread
    suspend fun getCard(deckId: Int): ImmutableCard? {
        val card = flashCardDao.getCard(deckId)
        return card.cardId?.let {cardId ->
            val cardContent = flashCardDao.getCardAndContent(cardId).cardContent
            val cardDefinitions = flashCardDao.getCardWithDefinition(cardId).definition
            card.toExternal(cardContent, cardDefinitions)
        }
    }

    @WorkerThread
    suspend fun getCards(deckId: Int): List<ImmutableCard?> {
        val cards = flashCardDao.getCards(deckId)
        return cards.map { card ->
            card.cardId?.let {cardId ->
                val cardContent = flashCardDao.getCardAndContent(cardId).cardContent
                val cardDefinitions = flashCardDao.getCardWithDefinition(cardId).definition
                card.toExternal(cardContent, cardDefinitions)
            }

        }
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
            when {
                it.definition.isNullOrEmpty() || it.definition == "" -> {
                    flashCardDao.deleteCardDefinition(it)
                }
                it.definitionId == null -> {
                    val definition = it
                    definition.cardId = card.cardId
                    flashCardDao.insertCardDefinition(definition)
                }
                else -> {
                    flashCardDao.updateCardDefinition(it)
                }
            }

            /*
            if (it.definition.isNullOrEmpty() || it.definition == "") {
                flashCardDao.deleteCardDefinition(it)
            } else {
                flashCardDao.updateCardDefinition(it)
            }
             */

        }
        flashCardDao.updateCard(card.toLocal())
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
    suspend fun deleteCard(card: ImmutableCard?, deck: Deck) {
        card?.let { actualCard ->
            actualCard.cardContent?.let { it1 -> flashCardDao.deleteCardContent(it1) }
            actualCard.cardDefinition?.forEach {
                flashCardDao.deleteCardDefinition(it)
            }
            flashCardDao.deleteCard(card.toLocal())
            deck.cardSum = deck.cardSum?.minus(1)
            flashCardDao.updateDeck(deck)
        }

    }

    @WorkerThread
    suspend fun deleteCards(deck: ImmutableDeck) {
        deck.deckId?.let {deckId ->
            val localDeck = flashCardDao.getDeckById(deckId)
            val cards = getCards(deckId)
            cards.forEach {card ->
                if (card != null) {
                    deleteCard(card, localDeck)
                }
            }
        }

    }

    @WorkerThread
    suspend fun deleteDeck(deck: ImmutableDeck){
        deck.deckId?.let { deckId ->
            val localDeck = flashCardDao.getDeckById(deckId)
            if (localDeck.cardSum == null || localDeck.cardSum == 0) {
                flashCardDao.deleteDeck(deck.toLocal())
            }
        }
    }

}