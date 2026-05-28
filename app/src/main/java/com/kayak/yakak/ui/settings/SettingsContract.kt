package com.kayak.yakak.ui.settings

data class SettingsState(
    // Apparence
    val isDynamicColorEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val fontScale: Float = 1.0f,
    
    // Notifications & Sons
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationFrequency: NotificationFrequency = NotificationFrequency.NORMAL,
    
    // Données & Stockage
    val autoBackupEnabled: Boolean = false,
    val cacheSize: String = "24 MB",
    
    // Compte
    val appLanguage: AppLanguage = AppLanguage.French,
    val userName: String = "Utilisateur",
    val userEmail: String = "user@example.com",
    val appVersion: String = "1.2.0-expressive",
    
    // Permissions status (State for UI)
    val hasNotificationPermission: Boolean = false,
    val hasLocationPermission: Boolean = false
)

enum class ThemeMode { System, Light, Dark }
enum class AppLanguage(val code: String) { English("en"), French("fr") }
enum class NotificationFrequency { MINIMAL, NORMAL, ALL }

sealed class SettingsEvent {
    // Apparence
    data class ToggleDynamicColor(val enabled: Boolean) : SettingsEvent()
    data class SetThemeMode(val mode: ThemeMode) : SettingsEvent()
    data class SetFontScale(val scale: Float) : SettingsEvent()
    data class SetLanguage(val language: AppLanguage) : SettingsEvent()
    
    // Préférences
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent()
    data class ToggleSoundEffects(val enabled: Boolean) : SettingsEvent()
    data class ToggleHapticFeedback(val enabled: Boolean) : SettingsEvent()
    data class SetNotificationFrequency(val frequency: NotificationFrequency) : SettingsEvent()
    
    // Données
    data class ToggleAutoBackup(val enabled: Boolean) : SettingsEvent()
    object ClearCache : SettingsEvent()
    
    // Permissions
    data class UpdatePermissionStatus(val permission: String, val granted: Boolean) : SettingsEvent()
    
    // Autres
    object SignOut : SettingsEvent()
}
