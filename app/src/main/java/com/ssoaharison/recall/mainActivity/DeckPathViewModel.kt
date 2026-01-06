package com.ssoaharison.recall.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.ssoaharison.recall.util.ThemePicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeckPathViewModel(private val repository: FlashCardRepository): ViewModel() {

    private val themePicker = ThemePicker()
    private var _currentDeck = MutableStateFlow<ExternalDeck?>(null)
    val currentDeck: StateFlow<ExternalDeck?> = _currentDeck.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMainDeck()?.let { mainDeck ->
                _currentDeck.update {
                    mainDeck

                }
            }
        }
    }

    fun setCurrentDeck(deck: ExternalDeck) {
        _currentDeck.update {
            deck
        }
    }


    fun getViewTheme(defaultThemeName: String): Int {
        if (currentDeck.value?.deckColorCode == null) {
            val viewTheme = themePicker.selectTheme(defaultThemeName) ?: themePicker.getDefaultTheme()
            return (viewTheme)
        } else {
            val viewTheme = if (defaultThemeName == DARK_THEME) {
                themePicker.selectDarkThemeByDeckColorCode(currentDeck.value?.deckColorCode!!, themePicker.getDefaultTheme())
            } else {
                themePicker.selectThemeByDeckColorCode(currentDeck.value?.deckColorCode!!, themePicker.getDefaultTheme())
            }
            return viewTheme
        }
    }

}

class DeckPathViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckPathViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckPathViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}
