package com.kayak.yakak.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.data.RecurrenceFrequency
import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

sealed class CalendarEvent {
    data class SelectDay(val date: LocalDate) : CalendarEvent()
    data class SelectTask(val task: Task) : CalendarEvent()
    data class EditState(val task: Task, val newState: Boolean) : CalendarEvent()
    data class ToggleShowLowFrequency(val show: Boolean) : CalendarEvent()
}

@HiltViewModel
class CalendarVM @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDay: StateFlow<LocalDate> = _selectedDay.asStateFlow()

    private val _showLowFrequency = MutableStateFlow(false)
    val showLowFrequency: StateFlow<Boolean> = _showLowFrequency.asStateFlow()

    val selectedTasks: StateFlow<List<Task>> = repository.tasks
        .combine(_selectedDay) { allTasks, day ->
            allTasks.filter { task ->
                !task.isCompleted && isTaskOnDay(task, day) && !task.isBirthday
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDayBirthdays: StateFlow<List<Task>> = repository.tasks
        .combine(_selectedDay) { allTasks, day ->
            allTasks.filter { it.isBirthday && it.expirationDate.toLocalDate().dayOfMonth == day.dayOfMonth && it.expirationDate.toLocalDate().month == day.month }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskCounts: StateFlow<Map<LocalDate, Int>> = repository.tasks
        .combine(_showLowFrequency) { allTasks, showLow ->
            allTasks.filter { !it.isCompleted && !it.isBirthday && shouldShowInCalendar(it, showLow) }
                .groupingBy { it.expirationDate.toLocalDate() }.eachCount()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val birthdayDays: StateFlow<Set<LocalDate>> = repository.tasks
        .combine(MutableStateFlow(Unit)) { allTasks, _ ->
            allTasks.filter { it.isBirthday }.map { it.expirationDate.toLocalDate() }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private fun shouldShowInCalendar(task: Task, showLow: Boolean): Boolean {
        if (task.recurrence == RecurrenceFrequency.NONE) return true
        if (showLow) return true
        return task.recurrence == RecurrenceFrequency.DAILY || task.recurrence == RecurrenceFrequency.WEEKLY
    }

    private fun isTaskOnDay(task: Task, day: LocalDate): Boolean {
        if (task.recurrence == RecurrenceFrequency.NONE) return task.expirationDate.toLocalDate() == day
        val startDate = task.creationDate.toLocalDate()
        if (day.isBefore(startDate)) return false
        return when (task.recurrence) {
            RecurrenceFrequency.DAILY -> true
            RecurrenceFrequency.WEEKLY -> startDate.dayOfWeek == day.dayOfWeek
            RecurrenceFrequency.MONTHLY -> startDate.dayOfMonth == day.dayOfMonth
            RecurrenceFrequency.YEARLY -> startDate.dayOfMonth == day.dayOfMonth && startDate.month == day.month
            else -> false
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.SelectDay -> _selectedDay.update { event.date }
            is CalendarEvent.SelectTask -> _selectedDay.update { event.task.expirationDate.toLocalDate() }
            is CalendarEvent.EditState -> viewModelScope.launch {
                val updatedTask = if (event.newState) event.task.copy(isCompleted = true, finishedDate = LocalDateTime.now())
                else event.task.copy(isCompleted = false, finishedDate = null)
                repository.updateTask(updatedTask)
            }
            is CalendarEvent.ToggleShowLowFrequency -> _showLowFrequency.update { event.show }
        }
    }
}