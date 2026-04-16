package com.kayak.yakak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kayak.yakak.ui.AppScreen
import com.kayak.yakak.ui.settings.SettingsViewModel
import com.kayak.yakak.ui.settings.ThemeMode
import com.kayak.yakak.ui.theme.YakakTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()
            
            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> isSystemInDarkTheme()
            }
            
            YakakTheme(
                darkTheme = darkTheme,
                dynamicColor = settingsState.isDynamicColorEnabled,
                fontScale = settingsState.fontScale
            ) {
                AppScreen()
            }
        }
    }
}
