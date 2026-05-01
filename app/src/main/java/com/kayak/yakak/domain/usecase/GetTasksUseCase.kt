package com.kayak.yakak.domain.usecase

import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.tasks
    }
}
