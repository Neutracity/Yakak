package com.kayak.yakak.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeTaskDao : TaskDao {
    val tasks = mutableListOf<Task>()

    override fun getAllTasks(): Flow<List<Task>> = flow {
        emit(tasks)
    }

    override suspend fun insertTask(task: Task) {
        tasks.find { it.id == task.id }?.let { tasks.remove(it) }
        tasks.add(task)
    }

    override suspend fun updateTask(task: Task) {
        insertTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        tasks.removeIf { it.id == task.id }
    }
}
