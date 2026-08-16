package it.unibo.orma.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.repositories.SettingsRepository
import it.unibo.orma.data.repositories.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val username: String? = null,
    val avatarUri: String? = null
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val state = combine(
        repository.themeMode,
        repository.username,
        repository.avatarUri
    ) { theme, name, avatar -> SettingsState(theme, name, avatar) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState()
        )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setUsername(name: String) = viewModelScope.launch { repository.setUsername(name) }
    fun setAvatarUri(uri: String) = viewModelScope.launch { repository.setAvatarUri(uri) }
}
