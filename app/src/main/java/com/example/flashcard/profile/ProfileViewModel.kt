package com.example.flashcard.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableDayModel
import com.example.flashcard.backend.Model.ImmutableWeeklyReviewModel
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileViewModel(private val repository: FlashCardRepository): ViewModel() {

    private var fetchJob: Job? = null
    private var _weeklyReview = MutableStateFlow<UiState<ImmutableWeeklyReviewModel>>(UiState.Loading)
    val weeklyReview: StateFlow<UiState<ImmutableWeeklyReviewModel>> = _weeklyReview.asStateFlow()

    private val thisWeek = ImmutableWeeklyReviewModel(
        sunday = ImmutableDayModel("Sunday", "28-09-23", 0),
        monday = ImmutableDayModel("monday", "28-09-23", 30),
        tuesday = ImmutableDayModel("Tuesday", "28-09-23", 3),
        wednesday = ImmutableDayModel("Wednesday", "28-09-23", 4),
        thursday = ImmutableDayModel("Thursday", "28-09-23", 10),
        friday = ImmutableDayModel("Friday", "28-09-23", 6),
        saturday = ImmutableDayModel("saturday", "28-09-23", 7)
    )

    fun getWeeklyReview() {
        fetchJob?.cancel()
        _weeklyReview.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                _weeklyReview.value = UiState.Success(addColorGrade(thisWeek))
            } catch (e: IOException) {
                _weeklyReview.value = UiState.Error(e.toString())
            }
        }
    }

    private fun putGradeColor(thisWeek: ImmutableWeeklyReviewModel): List<ImmutableDayModel> {
        val days = listOf(
            thisWeek.sunday, thisWeek.monday, thisWeek.tuesday, thisWeek.wednesday,
            thisWeek.thursday, thisWeek.friday, thisWeek.saturday
        )
        val sortedList: List<ImmutableDayModel?> = days.sortedBy { it?.revisedCardSum }
        val max = 700
        val midlMax = 200
        val midl = 100
        val midlMin = 25
        val min = 15

        val result = mutableListOf<ImmutableDayModel>()
        for (index in sortedList.indices) {
            if (sortedList[index]?.revisedCardSum == 0) {
                sortedList[index]?.colorGrade = min
                result.add(sortedList[index]!!)
            } else if (index == 0) {
                sortedList[index]?.colorGrade = max
                result.add(sortedList[index]!!)
            } else if (sortedList[index]?.revisedCardSum == sortedList[index-1]?.revisedCardSum) {
                sortedList[index]?.colorGrade = max
                result.add(sortedList[index]!!)
            } else if (sortedList[index]?.revisedCardSum!! > sortedList[index-1]?.revisedCardSum!!) {
                for (grade in result.indices) {
                    if (result[grade].colorGrade == max) {
                        result[grade].colorGrade = midlMax
                    } else if (result[grade].colorGrade == midlMax) {
                        result[grade].colorGrade = midl
                    } else if (result[grade].colorGrade == midl) {
                        result[grade].colorGrade = midlMin
                    } else if (result[grade].colorGrade == min) {
                        result[grade].colorGrade = min
                    }
                }
                sortedList[index]?.colorGrade = max
                result.add(sortedList[index]!!)
            }
        }
        return result
    }

    fun addColorGrade(weekReview: ImmutableWeeklyReviewModel): ImmutableWeeklyReviewModel {
        val daysWithColorGrade = putGradeColor(weekReview)
        val result = ImmutableWeeklyReviewModel()
        daysWithColorGrade.forEach { day ->
            when (day.dayName) {
                "Sunday" -> {result.sunday = day}
                "monday" -> {result.monday = day}
                "Tuesday" -> {result.tuesday = day}
                "Wednesday" -> {result.wednesday = day}
                "Thursday" -> {result.thursday = day}
                "Friday" -> {result.friday = day}
                "saturday" -> {result.saturday = day}
            }
        }
        return result
    }
}

class ProfileViewModelFactory(private val repository: FlashCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}