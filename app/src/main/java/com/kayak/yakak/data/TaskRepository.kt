package com.kayak.yakak.data

import com.kayak.yakak.Task
import com.kayak.yakak.utils.getTaskList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    var tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        _tasks.value =  getTaskList()
    }

    // CRUD : Create Read Update Delete

    fun addTask(task : Task){
        _tasks.update { currentList ->
            currentList + task
        }
    }

    fun updateTask(updatedTask : Task){
        _tasks.update { currentList ->
            currentList.map{ task ->
                if (task.id == updatedTask.id) updatedTask else task
            }
        }
    }

    fun deleteTask(task : Task){
        _tasks.update { currentList ->
            currentList.filter { it.id != task.id }
        }
    }

}