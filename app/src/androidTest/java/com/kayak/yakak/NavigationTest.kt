package com.kayak.yakak

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.rule.GrantPermissionRule
import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import javax.inject.Inject

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var taskDao: TaskDao

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun init() {
        hiltRule.inject()
    }

    private suspend fun insertTestData() {
        taskDao.insertTask(Task(id = 1, name = "Test Task", expirationDate = LocalDateTime.now().plusDays(1)))
    }

    @Test
    fun test_navigation_to_maps() {
        runBlocking { insertTestData() }
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()
        composeRule.onRoot().printToLog("DEBUG_MAPS")
        composeRule.waitUntil(timeoutMillis = 5000) { composeRule.onAllNodesWithContentDescription("Maps").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithContentDescription("Maps").performClick()
        composeRule.onNodeWithContentDescription("Maps").assertIsDisplayed()
    }

    @Test
    fun test_navigation_to_agenda() {
        runBlocking { insertTestData() }
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()
        composeRule.onRoot().printToLog("DEBUG_AGENDA")
        composeRule.waitUntil(timeoutMillis = 5000) { composeRule.onAllNodesWithContentDescription("Agenda").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithContentDescription("Agenda").performClick()
        composeRule.onNodeWithContentDescription("Agenda").assertIsDisplayed()
    }
}
