package it.unibo.orma.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.database.Hike
import it.unibo.orma.data.location.Coordinates
import it.unibo.orma.data.repositories.HikesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HikeDetailState(
    val hike: Hike? = null,
    val path: List<Coordinates> = emptyList()
)

class HikeDetailViewModel(
    private val hikeId: Long,
    private val repository: HikesRepository
) : ViewModel() {

    val state = combine(
        repository.hikeById(hikeId),
        repository.trackPoints(hikeId)
    ) { hike, points ->
        HikeDetailState(
            hike = hike,
            path = points.map { Coordinates(it.latitude, it.longitude, it.altitude) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HikeDetailState()
    )

    fun setCoverImage(uri: String) {
        val hike = state.value.hike ?: return
        viewModelScope.launch { repository.upsert(hike.copy(coverImageUri = uri)) }
    }

    fun rename(title: String, description: String) {
        val hike = state.value.hike ?: return
        viewModelScope.launch {
            repository.upsert(hike.copy(title = title, description = description))
        }
    }

    fun toggleFavorite() {
        val hike = state.value.hike ?: return
        viewModelScope.launch { repository.toggleFavorite(hike) }
    }

    fun delete(onDeleted: () -> Unit) {
        val hike = state.value.hike ?: return
        viewModelScope.launch {
            repository.delete(hike)
            onDeleted()
        }
    }
}
