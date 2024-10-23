package com.ssoaharison.recall.card

import androidx.lifecycle.ViewModel
import com.ssoaharison.recall.backend.Model.ImmutableCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewCardDialogViewModel() : ViewModel() {

    private var _addedCards =
        MutableStateFlow<ArrayList<ImmutableCard>>(arrayListOf<ImmutableCard>())
    val addedCards: StateFlow<ArrayList<ImmutableCard>> = _addedCards.asStateFlow()
    private var fetchJob: Job? = null

    fun areThereUnSavedAddedCards() = _addedCards.value.size != 0

    fun clearAddedCards() {
        _addedCards.value.clear()
    }

    fun addCard(card: ImmutableCard) {
        _addedCards.value.add(0, card)
    }

    fun removeCard(cardToBeRemoved: ImmutableCard?) {
        _addedCards.value.remove(cardToBeRemoved)
    }

    fun updateCard(updatedCard: ImmutableCard, indexCardToUpdate: Int) {
        _addedCards.value[indexCardToUpdate] = updatedCard
    }

    fun removeAllCards() {
        _addedCards.value.clear()
    }

}