package com.example.flashcard.card

import androidx.lifecycle.ViewModel
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NewCardDialogViewModel(): ViewModel() {

    private var _addedCards = MutableStateFlow<ArrayList<ImmutableCard>>(arrayListOf<ImmutableCard>())
    val addedCards: StateFlow<ArrayList<ImmutableCard>> = _addedCards.asStateFlow()
    private var fetchJob: Job? = null

    fun addCard(card: ImmutableCard) {
        _addedCards.value.add(card)
    }

    fun removeCard(card: ImmutableCard) {
        _addedCards.value.remove(card)
    }

}