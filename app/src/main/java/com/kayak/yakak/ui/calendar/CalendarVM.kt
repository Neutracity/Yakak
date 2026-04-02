package com.kayak.yakak.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}

@HiltViewModel
class CalendarVM @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDay: StateFlow<LocalDate> = _selectedDay.asStateFlow()

    val selectedTasks: StateFlow<List<Task>> = repository.tasks
        .combine(_selectedDay) { allTasks, day ->
            allTasks.filter { it.expirationDate == day && !it.isCompleted }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val taskCounts: StateFlow<Map<LocalDate, Int>> = repository.tasks
        .combine(MutableStateFlow(Unit)) { allTasks, _ ->
            allTasks
                .filter { !it.isCompleted && it.expirationDate != null }
                .groupingBy { it.expirationDate!! }
                .eachCount()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.SelectDay -> {
                _selectedDay.update { event.date }
                Log.i("CalendarVM", "Changed Date to ${event.date}")
            }
            is CalendarEvent.SelectTask -> {
                event.task.expirationDate?.let { date ->
                    _selectedDay.update { date }
                }
            }
            is CalendarEvent.EditState -> {
                viewModelScope.launch {
                    val updatedTask = if (event.newState) {
                        event.task.copy(isCompleted = true, finishedDate = LocalDateTime.now())
                    } else {
                        event.task.copy(isCompleted = false, finishedDate = null)
                    }
                    repository.updateTask(updatedTask)
                }
            }
        }
    }
}