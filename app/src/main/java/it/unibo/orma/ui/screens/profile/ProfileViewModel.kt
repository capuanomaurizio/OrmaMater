package it.unibo.orma.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.repositories.HikesRepository
import it.unibo.orma.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(
    val username: String? = null,
    val avatarUri: String? = null,
    val hikeCount: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalElevationMeters: Double = 0.0,
    val longestHikeMeters: Double = 0.0
)

class ProfileViewModel(
    private val settings: SettingsRepository,
    private val hikes: HikesRepository
) : ViewModel() {

    val state = combine(
        settings.username,
        settings.avatarUri,
        hikes.hikes
    ) { username, avatarUri, list ->
        ProfileState(
            username = username,
            avatarUri = avatarUri,
            hikeCount = list.size,
            totalDistanceMeters = list.sumOf { it.distanceMeters },
            totalElevationMeters = list.sumOf { it.elevationGainMeters },
            longestHikeMeters = list.maxOfOrNull { it.distanceMeters } ?: 0.0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileState()
    )

    fun setUsername(name: String) = viewModelScope.launch { settings.setUsername(name) }
    fun setAvatarUri(uri: String) = viewModelScope.launch { settings.setAvatarUri(uri) }
}
