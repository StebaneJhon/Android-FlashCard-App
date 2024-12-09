package com.ssoaharison.recall.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.Model.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsFragmentViewModel(private val repository: FlashCardRepository): ViewModel() {

    private var boxFetchJob: Job? = null
    private val _boxLevels = MutableStateFlow<UiState<List<ImmutableSpaceRepetitionBox>>>(UiState.Loading)
    val boxLevels: StateFlow<UiState<List<ImmutableSpaceRepetitionBox>>> = _boxLevels.asStateFlow()

    fun getBox() {
        boxFetchJob?.cancel()
        _boxLevels.value = UiState.Loading
        boxFetchJob = viewModelScope.launch {
            try {
                repository.getBox().collect {
                    if (it.isEmpty()) {
                        SpaceRepetitionAlgorithmHelper()
                    } else {
                        _boxLevels.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _boxLevels.value = UiState.Error(e.toString())
            }
        }
    }

    fun updateBoxLevel(boxLevel: SpaceRepetitionBox) = viewModelScope.launch { repository.updateBoxLevel(boxLevel) }

    private var _themSelectionList = MutableStateFlow<ArrayList<ThemeModel>>(arrayListOf())
    val themSelectionList: StateFlow<ArrayList<ThemeModel>> = _themSelectionList.asStateFlow()

    fun initThemeSelection(themes: Map<String, Int>, actualTheme: String) {
        if (_themSelectionList.value.isNotEmpty()) {
            return
        }
        themes.forEach { (themeId, theme) ->
            if (themeId == actualTheme) {
                _themSelectionList.value.add(
                    ThemeModel(
                        themeId,
                        theme,
                        true
                    )
                )
            } else {
                _themSelectionList.value.add(
                    ThemeModel(
                        themeId,
                        theme,
                        false
                    )
                )
            }
        }
    }

    fun selectTheme(themeId: String) {
        _themSelectionList.value.forEachIndexed { index, themeModel ->
            _themSelectionList.value[index].isSelected = themeModel.themeId == themeId
        }
    }

    private var deckCountJob: Job? = null
    suspend fun getDeckCount(action: (Int) -> Unit) {
        deckCountJob?.cancel()
        deckCountJob = viewModelScope.launch {
            action(repository.getDeckCount())
        }
    }

    private var cardCountJob: Job? = null
    suspend fun getCardCount( action: (Int) -> Unit ) {
        cardCountJob?.cancel()
        cardCountJob = viewModelScope.launch {
            action(repository.getCardCount())
        }
    }

    private var knownCardCountJob: Job? = null
    suspend fun getKnownCardCount( action: (Int) -> Unit ) {
        knownCardCountJob?.cancel()
        knownCardCountJob = viewModelScope.launch {
            action(repository.getKnownCardCount())
        }
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