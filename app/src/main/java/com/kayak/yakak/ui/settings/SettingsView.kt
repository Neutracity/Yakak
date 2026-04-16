package com.kayak.yakak.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kayak.yakak.ui.theme.YKShapeDefaults.cardShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Refresh cache size when entering screen
    LaunchedEffect(Unit) {
        viewModel.updateCacheSize()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Paramètres", 
                        style = MaterialTheme.typography.displayMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION PERSONNALISATION ---
            item { SettingHeader("Apparence") }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        title = "Couleurs dynamiques",
                        subtitle = "Adapte l'app à ton fond d'écran",
                        icon = Icons.Default.Palette,
                        checked = state.isDynamicColorEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleDynamicColor(it)) }
                    )
                    
                    // SegmentedButton for Theme
                    Column(Modifier.padding(16.dp)) {
                        Text("Thème de l'application", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = state.themeMode == mode,
                                    onClick = { viewModel.onEvent(SettingsEvent.SetThemeMode(mode)) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size)
                                ) {
                                    Text(when(mode) {
                                        ThemeMode.System -> "Système"
                                        ThemeMode.Light -> "Clair"
                                        ThemeMode.Dark -> "Sombre"
                                    })
                                }
                            }
                        }
                    }

                    // Font Scale Slider
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Taille de la police", style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = state.fontScale,
                            onValueChange = { viewModel.onEvent(SettingsEvent.SetFontScale(it)) },
                            valueRange = 0.8f..1.4f,
                            steps = 3
                        )
                    }
                }
            }

            // --- SECTION PERMISSIONS ---
            item { SettingHeader("Sécurité & Accès") }
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = "Permissions",
                        subtitle = "Gérer les accès Notifications et GPS",
                        icon = Icons.Default.Security,
                        onClick = { navController.navigate("settings/permissions") }
                    )
                }
            }

            // --- SECTION PRÉFÉRENCES ---
            item { SettingHeader("Préférences") }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        title = "Notifications",
                        subtitle = "Rappels pour tes tâches",
                        icon = Icons.Default.Notifications,
                        checked = state.notificationsEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleNotifications(it)) }
                    )
                    SettingsSwitchItem(
                        title = "Sons",
                        subtitle = "Effets sonores de complétion",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = state.soundEffectsEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleSoundEffects(it)) }
                    )
                }
            }

            // --- SECTION DONNÉES ---
            item { SettingHeader("Données") }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        title = "Sauvegarde automatique",
                        subtitle = "Synchronisation Cloud",
                        icon = Icons.Default.Backup,
                        checked = state.autoBackupEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleAutoBackup(it)) }
                    )
                    SettingsClickableItem(
                        title = "Vider le cache",
                        subtitle = "Actuel: ${state.cacheSize}",
                        icon = Icons.Default.DeleteSweep,
                        onClick = { viewModel.onEvent(SettingsEvent.ClearCache) }
                    )
                }
            }

            // --- INFO APP ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Yakak",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Version ${state.appVersion}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp, top = 16.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
