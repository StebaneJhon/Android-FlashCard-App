package com.ssoaharison.recall.quiz.writingQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssoaharison.recall.backend.FlashCardRepository

class WritingQuizGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

//    private var fetchJob: Job? = null
//    private var currentCardPosition: Int = 0
//    var progress: Int = 0
//    var attemptTime: Int = 0
//    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
//
//    //    private val _actualCard = MutableStateFlow<UiState<List<WritingQuizGameModel>>>(UiState.Loading)
////    val actualCard: StateFlow<UiState<List<WritingQuizGameModel>>> = _actualCard.asStateFlow()
//    private lateinit var cardList: MutableList<ImmutableCard?>
//    lateinit var deck: ImmutableDeck
//    private var originalCardList: List<ImmutableCard?>? = null
//    private var passedCards: Int = 0
//    private var restCard = 0
//    private var revisedCardsCount = 0
//    private var missedAnswersCount = 0
//
//
//    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
//    private val roteLearningHelper = RoteLearningAlgorithmHelper()
//
//    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
//        cardList = gameCards
//        restCard = cardSum()
//    }
//
//    fun initDeck(gameDeck: ImmutableDeck) {
//        deck = gameDeck
//    }
//
//    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
//        originalCardList = gameCards
//    }
//
//    fun initCurrentCardPosition() {
//        currentCardPosition = 0
//    }
//
//    fun initProgress() {
//        progress = 0
//    }
//
//    private fun initPassedCards() {
//        passedCards = 0
//    }
//
//    private fun initRestCards() {
//        restCard = cardSum()
//    }
//
//    fun initMissedCards() {
//        missedCards.clear()
//    }
//
//    fun onRestartQuiz() {
//        initWritingQuizGame()
//        initPassedCards()
//        initRestCards()
//    }
//
//    fun onCardMissed(cardId: String) {
//        missedAnswersCount++
//        val missedCard = getLocalCardById(cardId)?.card
//        if (missedCard !in missedCards) {
//            missedCards.add(missedCard)
//        }
//        increaseAttemptTime()
//    }
//
//    private fun getLocalCardById(cardId: String): ImmutableCardWithPosition? {
//        var i = 0
//        var result: ImmutableCardWithPosition? = null
//        while (true) {
//            val actualCard = cardList[i]
//            if (actualCard?.cardId == cardId) {
//                result = ImmutableCardWithPosition(actualCard, i)
//                break
//            }
//            i++
//        }
//        return result
//    }
//
//    fun getMissedCard(): MutableList<ImmutableCard?> {
//        val newCards = arrayListOf<ImmutableCard?>()
//        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
//        return newCards
//    }
//
//    fun getRevisedCardsCount() = revisedCardsCount
//
//    fun getAnswersCount(): Int {
//        var answersCount = 0
//        localWritingCards.forEach { card ->
//            answersCount += card.answers.size
//        }
//        return answersCount
//    }
//
//    fun getUserAnswerAccuracyFraction() = Calculations().fractionOfPart(getAnswersCount(), missedAnswersCount)
//
//    fun getUserAnswerAccuracy() = Calculations().percentageOfRest(getAnswersCount(), missedAnswersCount)
//
//    fun cardLeft() = restCard
//
//    fun cardSum() = cardList.size
//    fun getMissedCardSum() = missedCards.size
//    fun getKnownCardSum(cardCount: Int) = cardCount - getMissedCardSum()
//    fun initWritingQuizGame() {
//        missedCards.clear()
//        progress = 0
//        currentCardPosition = 0
//        localWritingCards.forEach {
//            it.attemptTime = 0
//            it.isCorrectlyAnswered = false
//        }
//        revisedCardsCount = localWritingCards.size
//        missedAnswersCount = 0
//        stopTimer()
//    }
//
//    fun increaseAttemptTime() {
//        attemptTime += 1
//    }
//
//    fun swipe(cardCount: Int): Boolean {
//        if (attemptTime == 0) {
//            progress += 100 / cardSum()
//        } else {
//            attemptTime = 0
//        }
//        currentCardPosition += 1
//        return currentCardPosition != cardCount
//    }
//
//    fun getCurrentCardPosition() = currentCardPosition
//
//    fun isUserAnswerCorrect(
//        userAnswer: String,
//        correctAnswers: List<TextWithLanguageModel>,
//        cardId: String,
//        currentCardPosition: Int
//    ): Boolean {
//        val temporaryCard = localWritingCards[currentCardPosition]
//        val isAnswerCorrect = temporaryCard.onUserAnswered(userAnswer)
//        when {
//            isAnswerCorrect && temporaryCard.attemptTime <= 1 -> {
//                onUserAnswered(true, cardId)
//                return true
//            }
//            isAnswerCorrect -> {
//                return true
//            }
//            !isAnswerCorrect && temporaryCard.attemptTime <= 1 -> {
//                onUserAnswered(false, cardId)
//                val newCard = initWritingQuizGameModel(temporaryCard)
//                val cardToReviseNewPosition = roteLearningHelper.onRepeatCardPosition(localWritingCards, currentCardPosition)
//                localWritingCards.add(cardToReviseNewPosition, newCard)
//            }
//        }
//        onCardMissed(cardId)
//        return false
//    }
//
//    private fun initWritingQuizGameModel(card: WritingQuizGameModel): WritingQuizGameModel {
//        return card.copy(
//            attemptTime = 0,
//            isCorrectlyAnswered = false,
//            userAnswer = null,
//            isActualOrPassed = false
//        )
//    }
//
//    fun sortCardsByLevel() {
//        cardList.sortBy { it?.cardStatus }
//    }
//
//    fun shuffleCards() {
//        cardList.shuffle()
//    }
//
//    fun sortByCreationDate() {
//        cardList.sortBy { it?.cardId }
//    }
//
//    fun unknownCardsOnly() {
//        cardList = cardList.filter { it?.cardStatus == CardLevel.L1 } as MutableList<ImmutableCard?>
//    }
//
//    fun cardToReviseOnly() {
//        cardList =
//            cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
//    }
//
//    fun restoreCardList() {
//        cardList = originalCardList!!.toMutableList()
//    }
//
//    private fun cardsToWritingQuizGameItem(
//        cards: List<ImmutableCard?>,
//        cardOrientation: String
//    ): List<WritingQuizGameModel> {
//        val newList = mutableListOf<WritingQuizGameModel>()
//        cards.forEach { item ->
//            newList.add(cardToWritingQuizGameItem(item!!, cardOrientation))
//        }
//        return newList
//    }
//
//
//    fun cardToWritingQuizGameItem(
//        item: ImmutableCard,
//        cardOrientation: String
//    ): WritingQuizGameModel {
//        return if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
//            val correctAlternatives = getCorrectDefinitions(
//                item.cardDefinition,
//                item.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
//            )
//            WritingQuizGameModel(
//                item.cardId,
//                TextWithLanguageModel(
//                    item.cardId,
//                    item.cardContent?.content!!,
//                    CONTENT,
//                    item.cardContentLanguage ?: deck.cardContentDefaultLanguage
//                ),
//                correctAlternatives
//            )
//        } else {
//            val correctAlternatives = getCorrectDefinitions(
//                item.cardDefinition,
//                item.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
//            )
//            WritingQuizGameModel(
//                item.cardId,
//                correctAlternatives.random(),
//                listOf(
//                    TextWithLanguageModel(
//                        item.cardId,
//                        item.cardContent?.content!!,
//                        CONTENT,
//                        item.cardContentLanguage ?: deck.cardContentDefaultLanguage!!
//                    )
//                )
//            )
//        }
//    }
//
//    private fun getCorrectDefinitions(
//        definitions: List<CardDefinition>?,
//        definitionLanguage: String?
//    ): List<TextWithLanguageModel> {
//        val correctDefinitions =
//            definitions?.let { defins -> defins.filter { isCorrect(it.isCorrectDefinition) } }
//        val correctAlternative = mutableListOf<TextWithLanguageModel>()
//        correctDefinitions?.forEach {
//            correctAlternative.add(
//                TextWithLanguageModel(
//                    it.cardId,
//                    it.definition,
//                    DEFINITION,
//                    definitionLanguage
//                )
//            )
//        }
//        return correctAlternative
//    }
//
//    fun isCorrect(index: Int?) = index == 1
//    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0
//
////    fun updateOnscreenCard(cardOrientation: String, cardCount: Int) {
////        when {
////            passedCards == cardList.size -> {
////                _actualCard.value = UiState.Error("Quiz Complete")
////            }
////
////            cardList.size == 0 -> {
////                _actualCard.value = UiState.Error("No Cards To Revise")
////            }
////
////            else -> {
////                fetchJob?.cancel()
////                fetchJob = viewModelScope.launch {
////                    val cards = getCardsAmount(cardList, cardCount)
////                    if (cards.isNullOrEmpty()) {
////                        _actualCard.value = UiState.Error("Too Few Cards")
////                    } else {
////                        _actualCard.value = UiState.Success(
////                            cardToWritingQuizGameItem(cards, cardOrientation)
////                        )
////                    }
////                }
////            }
////        }
////    }
//
////    fun updateCardOnReviseMissedCards(cardOrientation: String) {
////        when {
////            cardList.size == 0 -> {
////                _actualCard.value = UiState.Error("No Cards To Revise")
////            }
////
////            else -> {
////                fetchJob?.cancel()
////                fetchJob = viewModelScope.launch {
////                    _actualCard.value = UiState.Success(
////                        cardToWritingQuizGameItem(missedCards, cardOrientation)
////                    )
////                    missedCards.clear()
////                }
////            }
////        }
////    }
//
//    private lateinit var localWritingCards: MutableList<WritingQuizGameModel>
//    private var flowOfLocalWritingCards: Flow<List<WritingQuizGameModel>> = flow {
//        emit(localWritingCards)
//    }
//    private var _externalWritingCards =
//        MutableStateFlow<UiState<List<WritingQuizGameModel>>>(UiState.Loading)
//    val externalWritingCards: StateFlow<UiState<List<WritingQuizGameModel>>> =
//        _externalWritingCards.asStateFlow()
//
//    fun getWritingCards() {
//        fetchJob?.cancel()
//        fetchJob = viewModelScope.launch {
//            try {
//                flowOfLocalWritingCards.collect {
//                    if (it.isEmpty()) {
//                        _externalWritingCards.value = UiState.Error("No Cards To Revise")
//                    } else {
//                        _externalWritingCards.value = UiState.Success(it)
//                    }
//                }
//            } catch (e: IOException) {
//                _externalWritingCards.value = UiState.Error(e.message.toString())
//            }
//        }
//    }
//
//    fun updateActualCards(amount: Int, cardOrientation: String) {
//        val cards = getCardsAmount(cardList, amount)
//        localWritingCards = cardsToWritingQuizGameItem(cards!!, cardOrientation).toMutableList()
//        localWritingCards.first().setAsActualOrPassed()
//        revisedCardsCount = localWritingCards.size
//    }
//
//    fun setCardAsActualOrPassedByPosition(position: Int) {
//        localWritingCards[position].setAsActualOrPassed()
//    }
//
//    fun setCardAsNotActualOrNotPassedByPosition(position: Int) {
//        localWritingCards[position].setAsNotActualOrNotPassed()
//    }
//
//
//    fun updateActualCardsWithMissedCards(cardOrientation: String) {
//        val cards = missedCards
//        localWritingCards = cardsToWritingQuizGameItem(cards, cardOrientation).toMutableList()
//        revisedCardsCount = localWritingCards.size
//    }
//
//    fun getCardByPosition(position: Int) = localWritingCards[position]
//
//
//    private fun getCardsAmount(
//        quizCardList: List<ImmutableCard?>,
//        amount: Int
//    ): List<ImmutableCard?>? {
//
//        return when {
//            quizCardList.isEmpty() -> {
//                null
//            }
//
//            quizCardList.size == amount -> {
//                quizCardList
//            }
//
//            else -> {
//                var result = listOf<ImmutableCard?>()
//                if (passedCards <= quizCardList.size) {
//                    if (restCard > amount) {
//                        result = quizCardList.slice(passedCards..passedCards.plus(amount).minus(1))
//                        passedCards += amount
//                    } else {
//                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1))
//                            .toMutableList()
//                        passedCards += quizCardList.size.plus(50)
//                    }
//                }
//                restCard = quizCardList.size - passedCards
//                result
//            }
//        }
//    }
//
//    private fun onUserAnswered(
//        isKnown: Boolean,
//        cardId: String
//    ) {
//        val cardWithPosition = getLocalCardById(cardId)
//        val card = getLocalCardById(cardId)?.card
//        val cardPosition = getLocalCardById(cardId)?.position
//        if (card != null) {
//            val newCard = spaceRepetitionHelper.rescheduleCard(card, isKnown)
//            updateCard(newCard, cardPosition!!)
//        }
//    }
//
//    private fun updateCard(
//        card: ImmutableCard,
//        position: Int
//    ) = viewModelScope.launch {
//        cardList[position] = card
//        repository.updateCardWithContentAndDefinition(card)
//    }
//
//    fun updateCardContentLanguage(cardId: String, language: String) = viewModelScope.launch {
//        repository.updateCardContentLanguage(cardId, language)
//    }
//
//    fun updateCardDefinitionLanguage(cardId: String, language: String) = viewModelScope.launch {
//        repository.updateCardDefinitionLanguage(cardId, language)
//    }
//
//    private val _timer = MutableStateFlow(0L)
//    val timer = _timer.asStateFlow()
//    private var timerJob: Job? = null
//
//    fun startTimer() {
//        timerJob?.cancel()
//        timerJob = viewModelScope.launch {
//            while (true) {
//                delay(1000)
//                _timer.value++
//            }
//        }
//    }
//
//    fun pauseTimer() {
//        timerJob?.cancel()
//    }
//
//    fun stopTimer() {
//        _timer.value = 0
//        timerJob?.cancel()
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        timerJob?.cancel()
//    }
//
//    fun formatTime(seconds: Long): String {
//        return formatElapsedTime(seconds)
//    }

}

class WritingQuizGameViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WritingQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WritingQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}