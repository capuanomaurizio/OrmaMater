package it.unibo.orma.ui.screens.hikes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.database.Hike
import it.unibo.orma.data.repositories.HikesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HikesState(
    val hikes: List<Hike> = emptyList(),
    val query: String = "",
    val onlyFavorites: Boolean = false
)

data class HikesActions(
    val setQuery: (String) -> Unit,
    val toggleOnlyFavorites: () -> Unit,
    val toggleFavorite: (Hike) -> Unit,
    val delete: (Hike) -> Unit
)

class HikesViewModel(private val repository: HikesRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val onlyFavorites = MutableStateFlow(false)

    val state = combine(
        repository.hikes,
        query,
        onlyFavorites
    ) { hikes, query, onlyFavorites ->
        val visible = hikes.filter { hike ->
            val matchesQuery = query.isBlank() ||
                hike.title.contains(query, ignoreCase = true) ||
                hike.placeName.orEmpty().contains(query, ignoreCase = true)
            val matchesFavorite = !onlyFavorites || hike.isFavorite
            matchesQuery && matchesFavorite
        }
        HikesState(visible, query, onlyFavorites)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HikesState()
    )

    val actions = HikesActions(
        setQuery = { query.value = it },
        toggleOnlyFavorites = { onlyFavorites.value = !onlyFavorites.value },
        toggleFavorite = { hike -> viewModelScope.launch { repository.toggleFavorite(hike) } },
        delete = { hike -> viewModelScope.launch { repository.delete(hike) } }
    )
}
