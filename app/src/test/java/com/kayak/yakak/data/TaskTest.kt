package com.kayak.yakak.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class TaskTest {

    @Test
    fun `test default task values`() {
        val task = Task(name = "Test Task")
        
        assertEquals("Test Task", task.name)
        assertEquals("", task.description)
        assertFalse(task.isCompleted)
        assertNull(task.finishedDate)
        assertEquals(RecurrenceFrequency.NONE, task.recurrence)
        assertEquals(0, task.streakCount)
        assertEquals(0.0, task.location.latitude, 0.0)
        assertEquals(0.0, task.location.longitude, 0.0)
    }

    @Test
    fun `test task completion`() {
        val now = LocalDateTime.now()
        val task = Task(
            name = "Completed Task",
            isCompleted = true,
            finishedDate = now
        )
        
        assertEquals(true, task.isCompleted)
        assertEquals(now, task.finishedDate)
    }

    @Test
    fun `test task with location`() {
        val location = Location(48.8583, 2.2945)
        val task = Task(name = "Eiffel Tower", location = location)
        
        assertEquals(48.8583, task.location.latitude, 0.0001)
        assertEquals(2.2945, task.location.longitude, 0.0001)
    }
}
