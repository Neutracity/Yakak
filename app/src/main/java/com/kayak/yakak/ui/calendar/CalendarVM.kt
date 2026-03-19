package com.kayak.yakak.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.Task
import com.kayak.yakak.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class CalendarEvent{
    data class SelectDay(val date: LocalDate) : CalendarEvent()
    data class SelectTask(val task : Task) : CalendarEvent()
    data class EditState(val task: Task,val newState: Boolean) : CalendarEvent()

}


class CalendarVM : ViewModel(){
    private var _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDay : StateFlow<LocalDate> = _selectedDay.asStateFlow()

    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks.asStateFlow()

    private val _taskCounts = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val taskCounts: StateFlow<Map<LocalDate, Int>> = _taskCounts.asStateFlow()

    init {
        viewModelScope.launch {
            TaskRepository.tasks.collect { allTasks ->
                _selectedTasks.update {
                    allTasks.filter { it.expirationDate == _selectedDay.value && !it.isCompleted}
                }
                val countsByDate = allTasks
                    .filter { !it.isCompleted }
                    .groupingBy { it.expirationDate }
                    .eachCount()

                _taskCounts.update { countsByDate }
            }
        }

    }
    fun updateTasks(){
        _selectedTasks.update { TaskRepository.tasks.value.filter {  it.expirationDate == _selectedDay.value && !it.isCompleted} }
    }

    fun onEvent(event : CalendarEvent) {
        when (event){
            is CalendarEvent.SelectDay -> {
                _selectedDay.update { event.date }
                updateTasks()
                Log.println(Log.INFO,null,"Changed Date to ${_selectedDay.value}")
            }
            is CalendarEvent.SelectTask -> {
                val day = TaskRepository.tasks.value.find { it.id == event.task.id }?.expirationDate
                if(day != null){
                    _selectedDay.update { day }
                }
                updateTasks()
            }
            is CalendarEvent.EditState -> {
                TaskRepository.updateTask(event.task.copy(isCompleted = event.newState))
            }
        }
    }



}