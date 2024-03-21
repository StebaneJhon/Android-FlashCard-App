package com.example.flashcard.settings

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableUser
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsFragmentViewModel(private val repository: FlashCardRepository): ViewModel() {

    private var userFetchJob: Job? = null
    private var _user = MutableStateFlow<UiState<List<ImmutableUser>>>(UiState.Loading)
    val user: StateFlow<UiState<List<ImmutableUser>>> = _user.asStateFlow()

    fun getUserDetails() {
        userFetchJob?.cancel()
        _user.value = UiState.Loading
        userFetchJob = viewModelScope.launch {
            try {
                repository.getUser().collect() {
                    if (it.isEmpty()) {
                        _user.value = UiState.Success(
                            listOf(ImmutableUser(
                                userId = null,
                                name = "Anonymous",
                                initial = "A",
                                status = "User",
                                creation = null
                            ))
                        )
                    } else {
                        _user.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _user.value = UiState.Error(e.toString())
            }
        }
    }

    fun insertBoxLevel(boxLevel: SpaceRepetitionBox) = viewModelScope.launch {
        repository.insertBoxLevel(boxLevel)
    }

}

class SettingsFragmentViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsFragmentViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}