package com.soaharisonstebane.mneme.home

import androidx.lifecycle.ViewModel
import com.soaharisonstebane.mneme.util.ColorModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrentDeckDetailsViewModel: ViewModel() {
    private var _colorSelectionList = MutableStateFlow<ArrayList<ColorModel>>(arrayListOf())
    val colorSelectionList: StateFlow<ArrayList<ColorModel>> = _colorSelectionList.asStateFlow()

    fun initColorSelection(colors: Map<String, Int>, actualColorId: String?) {
        colors.forEach { (id, color) ->
            if (actualColorId == id) {
                _colorSelectionList.value.add(
                    ColorModel(color, id, true)
                )
            } else {
                _colorSelectionList.value.add(
                    ColorModel(color, id)
                )
            }

        }
    }

    fun selectColor(id: String) {
        _colorSelectionList.value.forEachIndexed { index, color ->
            if (color.id == id) {
                _colorSelectionList.value[index].isSelected = true
            } else {
                _colorSelectionList.value[index].isSelected = false
            }
        }
    }

    fun getSelectedColor(): ColorModel? {
        _colorSelectionList.value.forEach { color ->
            if (color.isSelected) {
                return color
            }
        }
        return null
    }

}