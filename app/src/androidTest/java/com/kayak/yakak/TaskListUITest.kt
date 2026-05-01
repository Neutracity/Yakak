package com.kayak.yakak

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import javax.inject.Inject

@HiltAndroidTest
class TaskListUITest {

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


    @org.junit.Before
    fun init() {
        hiltRule.inject()
    }

    private suspend fun insertTestData() {
        taskDao.insertTask(Task(id = 1, name = "Test Task", expirationDate = LocalDateTime.now().plusDays(1)))
    }

    @Test
    fun test_initial_state_hidden_text() {
        composeRule.onNodeWithTag("TEXT").assertDoesNotExist()
    }


    @Test
    fun test_task_creation_from_fab() {
        composeRule.onNodeWithTag("FAB").performClick()
        composeRule.onNodeWithTag("ADDTASK").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
                 composeRule.onAllNodesWithTag("EDITSHEET", useUnmergedTree = true)
                     .fetchSemanticsNodes()
                     .isNotEmpty()
             }
        composeRule.onNodeWithTag("EDITSHEET", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("EDITSHEET", useUnmergedTree = true).assertIsDisplayed()

    }


    @Test
    fun test_task_open_fab_menu() {
        composeRule.onNodeWithTag("FAB").performClick()
        composeRule.onNodeWithTag("ADDTASK").assertIsDisplayed()
    }

}
