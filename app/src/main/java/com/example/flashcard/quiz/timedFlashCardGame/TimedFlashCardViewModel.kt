package com.example.flashcard.quiz.timedFlashCardGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimedFlashCardViewModel(private val repository: FlashCardRepository) : ViewModel() {
    private var fetchJob: Job? = null

    private var cardPosition: Int = 0
    private var knownCardsSum: Int = 0
    private val unKnownCards: ArrayList<ImmutableCard> = arrayListOf()

    private val _actualCard = MutableStateFlow<UiState<ImmutableCard>>(UiState.Loading)
    val actualCard: StateFlow<UiState<ImmutableCard>> = _actualCard.asStateFlow()

    fun getActualCard(cardList: List<ImmutableCard>) {
        if (cardPosition >= cardList.size-1) {
            _actualCard.value = UiState.Error("Quiz Complete")
            cardPosition = 0
        } else {
            fetchJob?.cancel()
            _actualCard.value = UiState.Loading
            fetchJob = viewModelScope.launch {
                _actualCard.value = UiState.Success(cardList[cardPosition])
                cardPosition += 1
            }
        }
    }

    fun onCardKnown() {
        knownCardsSum += 1
    }

    fun onCardUnknown(cardList: List<ImmutableCard>) {
        //knownCardsSum -= 1
        unKnownCards.add(cardList[cardPosition])

    }

    fun getKnownCardSum(cardList: List<ImmutableCard>) = cardList.size - unKnownCards.size
    fun getUnknownCards(): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        unKnownCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards.toList()
    }

    fun initFlashCard() {
        unKnownCards.clear()
    }
}

class TimedFlashCardViewModeFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimedFlashCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimedFlashCardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}