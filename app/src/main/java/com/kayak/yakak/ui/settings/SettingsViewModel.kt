package com.kayak.yakak.ui.settings

import android.Manifest
import android.app.LocaleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.LocaleList
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    val uiState: StateFlow<SettingsState> = repository.settings

    init {
        refreshPermissionStatus()
        updateCacheSize()
    }

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.ToggleDynamicColor -> repository.updateSettings { it.copy(isDynamicColorEnabled = event.enabled) }
                is SettingsEvent.SetThemeMode -> repository.updateSettings { it.copy(themeMode = event.mode) }
                is SettingsEvent.SetFontScale -> repository.updateSettings { it.copy(fontScale = event.scale) }
                is SettingsEvent.SetLanguage -> {
                    repository.updateSettings { it.copy(appLanguage = event.language) }
                    changeLocale(event.language.code)
                }
                is SettingsEvent.ToggleNotifications -> repository.updateSettings { it.copy(notificationsEnabled = event.enabled) }
                is SettingsEvent.ToggleSoundEffects -> repository.updateSettings { it.copy(soundEffectsEnabled = event.enabled) }
                is SettingsEvent.ToggleHapticFeedback -> repository.updateSettings { it.copy(hapticFeedbackEnabled = event.enabled) }
                is SettingsEvent.SetNotificationFrequency -> repository.updateSettings { it.copy(notificationFrequency = event.frequency) }
                is SettingsEvent.ToggleAutoBackup -> repository.updateSettings { it.copy(autoBackupEnabled = event.enabled) }
                
                SettingsEvent.ClearCache -> {
                    clearApplicationCache()
                    updateCacheSize()
                }
                
                is SettingsEvent.UpdatePermissionStatus -> {
                    refreshPermissionStatus()
                }
                
                SettingsEvent.SignOut -> { /* Logic for logout */ }
            }
        }
    }

    fun refreshPermissionStatus() {
        val hasNotifications = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        repository.updateSettings { 
            it.copy(
                hasNotificationPermission = hasNotifications,
                hasLocationPermission = hasLocation
            )
        }
    }

    fun updateCacheSize() {
        viewModelScope.launch {
            val size = getCacheSize(context.cacheDir) + (context.externalCacheDir?.let { getCacheSize(it) } ?: 0L)
            val sizeStr = when {
                size > 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", size / (1024f * 1024f))
                size > 1024 -> "${size / 1024} KB"
                else -> "$size B"
            }
            repository.updateSettings { it.copy(cacheSize = sizeStr) }
        }
    }

    private fun getCacheSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0
        var size: Long = 0
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getCacheSize(file) else file.length()
        }
        return size
    }

    private fun clearApplicationCache() {
        context.cacheDir.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    }

    private fun changeLocale(languageCode: String) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
    }
}
