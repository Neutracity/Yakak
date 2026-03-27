package com.kayak.yakak.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.Location
import com.kayak.yakak.Task
import com.kayak.yakak.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class TaskEvent {
    data class Delete(val task: Task) : TaskEvent()
    data class EditState(val task: Task,val newState: Boolean) : TaskEvent()
    data class EditTitle(val task: Task,val newTitle: String) : TaskEvent()
    data class EditDescription(val task: Task,val newDescription: String) : TaskEvent()
    data class EditDate(val task: Task,val newDate: LocalDate) : TaskEvent()
    data class EditLocation(val task: Task, val newLocation: Location) : TaskEvent()
    data class NewTask(val task: Task) : TaskEvent()

}

data class TaskListUiState(
    val pendingTasks: List<Task> = emptyList(),
    val finishedTasks: List<Task> = emptyList()
)

class TaskListVM : ViewModel(){
    private val _uiState = MutableStateFlow(TaskListUiState())
    var uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TaskRepository.tasks.collect { allTasks ->
                _uiState.update { currentState ->
                    currentState.copy(
                        pendingTasks = allTasks.filter { !it.isCompleted },
                        finishedTasks = allTasks.filter { it.isCompleted }
                            .sortedWith(
                                compareByDescending<Task> { it.finishedDate }
                                    .thenByDescending { it.id }
                            )
                    )
                }
            }
        }

    }

    fun onEvent(event: TaskEvent){

        when (event){
            is TaskEvent.Delete ->{
                TaskRepository.deleteTask(event.task)
            }
            is TaskEvent.EditState ->{
                if (event.newState) {
                    TaskRepository.updateTask(event.task.copy(isCompleted = true, finishedDate = LocalDate.now()))
                }else{
                    TaskRepository.updateTask(event.task.copy(isCompleted = false, finishedDate = null))
                }
            }
            is TaskEvent.EditTitle ->{
                TaskRepository.updateTask(event.task.copy(name = event.newTitle))
            }
            is TaskEvent.EditDescription ->{
                TaskRepository.updateTask(event.task.copy(description = event.newDescription))
            }
            is TaskEvent.NewTask ->{
                TaskRepository.addTask(event.task)
            }
            is TaskEvent.EditDate -> {
                TaskRepository.updateTask(event.task.copy(expirationDate = event.newDate))
            }
            is TaskEvent.EditLocation -> {
                TaskRepository.updateTask(event.task.copy(location = event.newLocation))
            }
        }
    }


}