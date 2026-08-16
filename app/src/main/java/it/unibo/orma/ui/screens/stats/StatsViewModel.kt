package it.unibo.orma.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.Badge
import it.unibo.orma.data.repositories.HikesRepository
import it.unibo.orma.data.unlockedBadges
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class MonthlyDistance(val label: String, val kilometers: Float)

data class StatsState(
    val monthly: List<MonthlyDistance> = emptyList(),
    val badges: Map<Badge, Long?> = emptyMap()
)

class StatsViewModel(repository: HikesRepository) : ViewModel() {

    val state = repository.hikes
        .map { hikes ->
            StatsState(
                monthly = monthlyDistances(hikes.map { it.startedAt to it.distanceMeters }),
                badges = unlockedBadges(hikes)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsState()
        )
}

private const val MONTHS_SHOWN = 6

private val MONTH_LABELS = listOf(
    "Gen", "Feb", "Mar", "Apr", "Mag", "Giu",
    "Lug", "Ago", "Set", "Ott", "Nov", "Dic"
)

/**
 * mesi vuoti inclusi: un grafico con i buchi si legge meglio di uno che invece salta quelli senza escursioni
 */
private fun monthlyDistances(hikes: List<Pair<Long, Double>>): List<MonthlyDistance> {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    val totals = hikes.groupBy { (startedAt, _) ->
        calendar.timeInMillis = startedAt
        calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
    }.mapValues { (_, entries) -> entries.sumOf { it.second } }

    val lastKey = currentYear * 12 + currentMonth
    return ((lastKey - MONTHS_SHOWN + 1)..lastKey).map { key ->
        MonthlyDistance(
            label = MONTH_LABELS[key % 12],
            kilometers = ((totals[key] ?: 0.0) / 1000).toFloat()
        )
    }
}
