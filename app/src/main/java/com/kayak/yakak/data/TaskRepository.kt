package com.kayak.yakak.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()


    // CRUD : Create Read Update Delete

    suspend fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}