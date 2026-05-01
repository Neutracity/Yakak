package com.kayak.yakak.domain.usecase

import com.kayak.yakak.data.FakeTaskDao
import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddTaskUseCaseTest {

    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var repository: TaskRepository
    private lateinit var fakeDao: FakeTaskDao

    @Before
    fun setUp() {
        fakeDao = FakeTaskDao()
        repository = TaskRepository(fakeDao)
        addTaskUseCase = AddTaskUseCase(repository)
    }

    @Test
    fun `task should be added if fields are valid`() = runBlocking {
        // Arrange
        val task = Task(name = "Test Task", description = "Test Description")

        // Act
        addTaskUseCase.invoke(task)

        // Assert
        val tasks = fakeDao.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].name)
    }
}
