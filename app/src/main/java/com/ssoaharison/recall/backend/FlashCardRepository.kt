package com.ssoaharison.recall.backend

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.models.toExternal
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCard
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardContentWithDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class FlashCardRepository(private val flashCardDao: FlashCardDao) {

//    @WorkerThread
//    fun allDecks(): Flow<List<ImmutableDeck>> {
//        return flashCardDao.getAllDecks().map { decks ->
//            decks.map { deck ->
//                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
//                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
//                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
//                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
//            }
//        }
//    }

    @WorkerThread
    fun getSubdecks(deckId: String): Flow<List<ExternalDeck>> {
        return flashCardDao.getSubdecks(deckId).map { decks ->
            decks.map { deck ->
                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            }
        }
    }

    @WorkerThread
    fun getPrimaryDecks(): Flow<List<ExternalDeck>> {
        return flashCardDao.getPrimaryDecks().map { decks ->
            decks.map { deck ->
                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            }
        }
    }

    @WorkerThread
    suspend fun getMainDeck(): ExternalDeck? {
        return flashCardDao.getMainDeck()?.toExternal(0, 0, 0)
    }

    @WorkerThread
    suspend fun getDeckById(deckId: String): ExternalDeck {
        return flashCardDao.getDeckById(deckId).toExternal(0, 0, 0)
    }

    @WorkerThread
    suspend fun getDeckPath(deck: ExternalDeck): List<ExternalDeck> {
        val result = mutableListOf<ExternalDeck>(deck)
        var actualDeck = deck
        while (actualDeck.parentDeckId != null) {
            actualDeck = getDeckById(actualDeck.parentDeckId)
            result.add(actualDeck)
        }
        return result
    }

//    @WorkerThread
//    suspend fun allCards(): Flow<List<ImmutableCard?>> {
//        val cards = flashCardDao.getAllCards().map { cardList ->
//            cardList.map { card ->
//                card.cardId.let { cardId ->
//                    cardId.let { id ->
//                        val cardContent = flashCardDao.getCardAndContent(id).cardContent
//                        val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
//                        card.toExternal(cardContent, cardDefinitions)
//                    }
//                }
//            }
//        }
//        return cards
//    }

    @WorkerThread
    suspend fun allCards(): Flow<List<ExternalCard?>> {
        val cards = flashCardDao.getAllCards().map { cardList ->
            cardList.map { card ->
                card.cardId.let { cardId ->
                    cardId.let { id ->
                        card.toExternal()
                    }
                }
            }
        }
        return cards
    }

    @WorkerThread
    suspend fun getDeckCount() = flashCardDao.getDeckCount()

    @WorkerThread
    suspend fun getCardCount() = flashCardDao.getCardCount()

    @WorkerThread
    suspend fun getKnownCardCount() = flashCardDao.getKnownCardCount()

//    @WorkerThread
//    fun searchDeck(searchQuery: String): Flow<Set<ImmutableDeck>> {
//        return flashCardDao.searchDeck(searchQuery).map { decks ->
//            decks.map { deck ->
//                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
//                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
//                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
//                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
//            }.toSet()
//        }
//    }

    @WorkerThread
    fun searchDeck(searchQuery: String): Flow<Set<ExternalDeck>> {
        return flashCardDao.searchDeck(searchQuery).map { decks ->
            decks.map { deck ->
                val cardCount = flashCardDao.countCardsInDeck(deck.deckId)
                val knownCardCount = flashCardDao.countKnownCardsInDeck(deck.deckId)
                val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(deck.deckId)
                deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            }.toSet()
        }
    }

//    @WorkerThread
//    suspend fun getImmutableDeckWithCards(deckId: String): Flow<ImmutableDeckWithCards> {
//        val deckWithCards = flashCardDao.getDeckWithCards(deckId) ?: emptyFlow()
//        return deckWithCards.map { localDeckWithCards ->
//            val cardCount = flashCardDao.countCardsInDeck(localDeckWithCards.deck.deckId)
//            val knownCardCount = flashCardDao.countKnownCardsInDeck(localDeckWithCards.deck.deckId)
//            val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(localDeckWithCards.deck.deckId)
//            val deck = localDeckWithCards.deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
//            val cardList = localDeckWithCards.cards.map { card ->
//                card.cardId.let { cardId ->
//                    cardId.let { id ->
//                        val cardContent = flashCardDao.getCardAndContent(id).cardContent
//                        val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
//                        card.toExternal(cardContent, cardDefinitions)
//                    }
//
//                }
//            }
//            localDeckWithCards.toExternal(deck, cardList)
//        }
//    }

    @WorkerThread
    suspend fun getExternalDeckWithCardsAndContentAndDefinitions(deckId: String, context: Context): Flow<ExternalDeckWithCardsAndContentAndDefinitions> {
        val result = flashCardDao.getDeckWithCards(deckId)
        return result.map { data ->
            val cardCount = flashCardDao.countCardsInDeck(data.deck.deckId)
            val knownCardCount = flashCardDao.countKnownCardsInDeck(data.deck.deckId)
            val unKnownCardCount = flashCardDao.countUnKnownCardsInDeck(data.deck.deckId)
            val externalDeck = data.deck.toExternal(cardCount, knownCardCount, unKnownCardCount)
            val externalCardList = data.cards.map { card  ->
                var photoModelContent: PhotoModel? = null
                card.contentWithDefinitions.content.contentImageName?.let {
                    val filePhotoContent = File(context.filesDir, card.contentWithDefinitions.content.contentImageName)
                    val bytesPhotoContent = filePhotoContent.readBytes()
                    val bmpPhotoContent = BitmapFactory.decodeByteArray(bytesPhotoContent, 0, bytesPhotoContent.size)
                    photoModelContent = PhotoModel(filePhotoContent.name, bmpPhotoContent)
                }
                //TODO: Get audio
                var audioModelContent: AudioModel? = null
                card.contentWithDefinitions.content.contentAudioName?.let { audioName ->
                    val fileAudioContent = File(context.cacheDir, audioName)
                    audioModelContent = AudioModel(fileAudioContent)
                }
                val externalContent = card.contentWithDefinitions.content.toExternal(photoModelContent, audioModelContent)

                val externalDefinitions = card.contentWithDefinitions.definitions.map { definition ->
                    var photoModelDefinition: PhotoModel? = null
                    definition.definitionImageName?.let {
                        val filePhotoDefinition = File(context.filesDir, definition.definitionImageName)
                        val bytesPhotoDefinition = filePhotoDefinition.readBytes()
                        val bmpPhotoDefinition = BitmapFactory.decodeByteArray(bytesPhotoDefinition, 0, bytesPhotoDefinition.size)
                        photoModelDefinition = PhotoModel(filePhotoDefinition.name, bmpPhotoDefinition)
                    }
                    //TODO: Get audio
                    var audioModelDefinition: AudioModel? = null
                    definition.definitionAudioName?.let { audioName ->
                        val fileAudioDefinition = File(context.cacheDir, audioName)
                        audioModelDefinition = AudioModel(fileAudioDefinition)
                    }
                    definition.toExternal(photoModelDefinition, audioModelDefinition)
                }
                val externalCard = card.card.toExternal()
                ExternalCardWithContentAndDefinitions(
                    card = externalCard,
                    contentWithDefinitions = ExternalCardContentWithDefinitions(
                            content = externalContent,
                            definitions = externalDefinitions
                        )
                    )
            }
            ExternalDeckWithCardsAndContentAndDefinitions(
                deck = externalDeck,
                cards = externalCardList
            )
        }
    }


    @WorkerThread
    suspend fun insertDeck(deck: Deck) {
        flashCardDao.insertDeck(deck)
    }

//    @WorkerThread
//    suspend fun insertCard(
//        card: ImmutableCard,
//    ) {
//
////        val localCard = card.toLocal()
////        flashCardDao.insertCard(localCard)
////        val cardContent = card.cardContent
////        flashCardDao.insertCardContent(cardContent!!)
////        val cardDefinition = card.cardDefinition
////        cardDefinition?.forEach {
////            flashCardDao.insertCardDefinition(it)
////        }
//        flashCardDao.insertCardWithDefinition(card)
//    }

    @WorkerThread
    suspend fun insertCardWithContentAndDefinition(
        cardWithContentAndDefinitions: CardWithContentAndDefinitions,
    ) {
        flashCardDao.insertCardWithContentAndDefinitions(cardWithContentAndDefinitions)
    }

//    @WorkerThread
//    suspend fun insertCards(cards: List<ImmutableCard>) {
//        cards.forEach { card ->
//            insertCard(card)
//        }
//    }

    @WorkerThread
    suspend fun insertCardsWithContentAndDefinition(cardsWithContentAndDefinitions: List<CardWithContentAndDefinitions>) {
        cardsWithContentAndDefinitions.forEach { cardWithContentAndDefinitions ->
            insertCardWithContentAndDefinition(cardWithContentAndDefinitions)
        }
    }

//    @WorkerThread
//    fun searchCard(searchQuery: String, deckId: String): Flow<Set<ImmutableCard>> {
//        return flashCardDao.searchCard(searchQuery, deckId).map { cardList ->
//            cardList.map { card ->
//                val cardContent = flashCardDao.getCardAndContent(card.cardId).cardContent
//                val cardDefinitions = flashCardDao.getCardWithDefinition(card.cardId).definition
//                card.toExternal(cardContent, cardDefinitions)
//            }.toSet()
//        }
//    }

    @WorkerThread
    fun searchCard(searchQuery: String, deckId: String): Flow<List<ExternalCardWithContentAndDefinitions>> {
        return flashCardDao.searchCard(searchQuery, deckId).map { cardList ->
            //TODO: To be updated to return ExternalCardWithContentAndDefinitions
            cardList.map { card ->
                ExternalCardWithContentAndDefinitions(
                    card = card.card.toExternal(),
                    contentWithDefinitions = ExternalCardContentWithDefinitions(
                        content = card.contentWithDefinitions.content.toExternal(null, null),
                        definitions = card.contentWithDefinitions.definitions.map { definition -> definition.toExternal(null, null) }
                    )
                )

            }
        }
    }

//    @WorkerThread
//    suspend fun getCards(deckId: String): List<ImmutableCard?> {
//        val cards = flashCardDao.getCards(deckId)
//        return cards.map { card ->
//            card.cardId.let { cardId ->
//                cardId.let { id ->
//                    val cardContent = flashCardDao.getCardAndContent(id).cardContent
//                    val cardDefinitions = flashCardDao.getCardWithDefinition(id).definition
//                    card.toExternal(cardContent, cardDefinitions)
//                }
//            }
//        }
//    }

    @WorkerThread
    suspend fun getCards(deckId: String): List<ExternalCard?> {
        val cards = flashCardDao.getCards(deckId)
        return cards.map { card ->
            card.cardId.let { cardId ->
                cardId.let { id ->
                    //TODO: To be updated to return ExternalCardWithContentAndDefinitions
                    card.toExternal()
                }
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

    suspend fun updateDefaultCardContentLanguage(deckId: String, language: String) {
        flashCardDao.updateDefaultCardContentLanguage(deckId, language)
    }

    suspend fun updateDefaultCardDefinitionLanguage(deckId: String, language: String) {
        flashCardDao.updateDefaultCardDefinitionLanguage(deckId, language)
    }

    suspend fun updateCardContentLanguage(cardId: String, language: String) {
        flashCardDao.updateCardContentLanguage(cardId, language)
    }

    suspend fun updateCardDefinitionLanguage(cardId: String, language: String) {
        flashCardDao.updateCardDefinitionLanguage(cardId, language)
    }

//    @WorkerThread
//    suspend fun updateCardWithContentAndDefinition(card: ImmutableCard) {
////        flashCardDao.updateCardContent(card.cardContent!!)
////        card.cardDefinition?.forEach {
////            when {
////                it.definition.isEmpty() -> {
////                    flashCardDao.deleteCardDefinition(it)
////                }
////
////                it.definitionId == null -> {
////                    flashCardDao.insertCardDefinition(it)
////                }
////
////                else -> {
////                    flashCardDao.updateCardDefinition(it)
////                }
////            }
////
////        }
////        flashCardDao.updateCard(card.toLocal())
//        flashCardDao.updateCardWithContentAndDefinition(card)
//    }

    @WorkerThread
    suspend fun updateCardWithContentAndDefinition(cardWithContentAndDefinitions: CardWithContentAndDefinitions) {
        flashCardDao.updateCardWithContentAndDefinition(cardWithContentAndDefinitions)
    }

    @WorkerThread
    suspend fun updateCard(card: Card) {
        flashCardDao.updateCard(card)
    }

    @WorkerThread
    suspend fun updateBoxLevel(boxLevel: SpaceRepetitionBox) {
        flashCardDao.updateBoxLevel(boxLevel)
    }

//    @WorkerThread
//    suspend fun deleteCard(card: ImmutableCard) {
////        card?.let { actualCard ->
////            actualCard.cardContent?.let { it1 ->
////                flashCardDao.deleteCardContent(it1)
////            }
////            actualCard.cardDefinition?.forEach {
////                flashCardDao.deleteCardDefinition(it)
////            }
////            flashCardDao.deleteCard(card.toLocal())
////        }
//        flashCardDao.deleteCardWithContentAndDefinition(card.toLocal())
//    }

    @WorkerThread
    suspend fun deleteCardWithContentAndDefinitions(cardWithContentAndDefinitions: CardWithContentAndDefinitions,context: Context) {
        cardWithContentAndDefinitions.contentWithDefinitions.content.contentImageName?.let { imageName ->
            context.deleteFile(imageName)
        }
        cardWithContentAndDefinitions.contentWithDefinitions.content.contentAudioName?.let { audioName ->
            // TODO: Delete Audio from storage
        }
        cardWithContentAndDefinitions.contentWithDefinitions.definitions.forEach { definition ->
            definition.definitionImageName?.let { imageName ->
                context.deleteFile(imageName)
            }
            definition.definitionAudioName?.let { audioName ->
                // TODO: Delete Audio from storage
            }
        }
        flashCardDao.deleteCardWithContentAndDefinition(cardWithContentAndDefinitions)
    }

//    @WorkerThread
//    suspend fun deleteCards(cards: List<Card?>) {
////        val localDeck = deck.toLocal()
////        val cards = getCards(localDeck.deckId)
//
//        flashCardDao.deleteCardsWithContentAndDefinition(cards)
////        cards.forEach { card ->
//////            delay(300)
////            deleteCard(card!!)
////        }
//    }

    @WorkerThread
    suspend fun deleteCardsWithContentAndDefinitions(cardsWithContentAndDefinitions: List<CardWithContentAndDefinitions>) {
        flashCardDao.deleteCardsWithContentAndDefinition(cardsWithContentAndDefinitions)
    }


    @WorkerThread
    suspend fun deleteDeckWithCards(deckId: String) {
//        val deckWithCards = gettDeckWithCards(d.deckId)
//        deleteCards(deckWithCards.cards)
//        flashCardDao.deleteDeck(deckWithCards.deck)
        flashCardDao.deleteDeckWithCards(deckId)
    }

}