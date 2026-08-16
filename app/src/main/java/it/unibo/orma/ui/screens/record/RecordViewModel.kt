package it.unibo.orma.ui.screens.record

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.orma.data.database.Hike
import it.unibo.orma.data.database.TrackPoint
import it.unibo.orma.data.location.Coordinates
import it.unibo.orma.data.location.LocationService
import it.unibo.orma.data.remote.OSMDataSource
import it.unibo.orma.data.repositories.HikesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Un punto del tracciato mentre la registrazione è in corso.
 */
data class RecordedPoint(val coordinates: Coordinates, val recordedAt: Long)

enum class RecordStatus { Idle, Recording, Paused, Finished }

/**
 * Cosa è andato storto durante il tracking.
 */
enum class RecordError { LocationDisabled, PermissionDenied }

data class RecordState(
    val status: RecordStatus = RecordStatus.Idle,
    val path: List<RecordedPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elapsedSeconds: Long = 0,
    val placeName: String? = null,
    val photoUri: String? = null,
    val error: RecordError? = null,
    val isSaved: Boolean = false
)

class RecordViewModel(
    private val locationService: LocationService,
    private val repository: HikesRepository,
    private val osmDataSource: OSMDataSource
) : ViewModel() {

    private companion object {
        /** Sotto questa soglia il movimento è rumore del GPS, non cammino. */
        const val MIN_SEGMENT_METERS = 5.0

        /** L'altitudine GPS ha un errore di circa +/- 10 m: sotto i 3 m non contiamo salita. */
        const val MIN_ELEVATION_DELTA_METERS = 3.0

        /**
         * Oltre questa soglia il dislivello non è credibile
         * Senza questo limite una singola lettura sbagliata falsa l'intera escursione.
         */
        const val MAX_ELEVATION_DELTA_METERS = 50.0
    }

    private val _state = MutableStateFlow(RecordState())
    val state = _state.asStateFlow()

    private var trackingJob: Job? = null
    private var timerJob: Job? = null

    private var startedAtMillis = 0L

    private var elapsedBeforePause = 0L
    private var segmentStartMillis = 0L

    fun start() {
        if (_state.value.status == RecordStatus.Recording) return

        startedAtMillis = System.currentTimeMillis()
        elapsedBeforePause = 0L
        _state.value = RecordState(status = RecordStatus.Recording)
        resumeTracking()
    }

    fun pause() {
        if (_state.value.status != RecordStatus.Recording) return

        stopTracking()
        _state.update { it.copy(status = RecordStatus.Paused) }
    }

    fun resume() {
        if (_state.value.status != RecordStatus.Paused) return

        _state.update { it.copy(status = RecordStatus.Recording, error = null) }
        resumeTracking()
    }

    fun finish() {
        stopTracking()
        _state.update {
            it.copy(
                status = RecordStatus.Finished,
                elapsedSeconds = elapsedBeforePause,
                error = null
            )
        }
    }

    fun discard() {
        stopJobs()
        _state.value = RecordState()
    }

    fun setPhoto(uri: String) {
        _state.update { it.copy(photoUri = uri) }
    }

    fun save(title: String, description: String) {
        viewModelScope.launch {
            val current = _state.value

            val hike = Hike(
                title = title,
                description = description,
                placeName = current.placeName,
                startedAt = startedAtMillis,
                durationSeconds = current.elapsedSeconds,
                distanceMeters = current.distanceMeters,
                elevationGainMeters = current.elevationGainMeters,
                coverImageUri = current.photoUri
            )

            val hikeId = repository.insert(hike)
            repository.saveTrack(
                current.path.map { point ->
                    TrackPoint(
                        hikeId = hikeId,
                        latitude = point.coordinates.latitude,
                        longitude = point.coordinates.longitude,
                        altitude = point.coordinates.altitude,
                        recordedAt = point.recordedAt
                    )
                }
            )

            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun resumeTracking() {
        segmentStartMillis = System.currentTimeMillis()

        trackingJob = viewModelScope.launch {
            try {
                locationService.locationUpdates().collect { addPoint(it) }
            } catch (_: SecurityException) {
                setError(RecordError.PermissionDenied)
            } catch (_: IllegalStateException) {
                setError(RecordError.LocationDisabled)
            }
        }

        timerJob = viewModelScope.launch {
            while (isActive) {
                val runningFor = (System.currentTimeMillis() - segmentStartMillis) / 1000
                _state.update { it.copy(elapsedSeconds = elapsedBeforePause + runningFor) }
                delay(1000)
            }
        }
    }

    private fun stopTracking() {
        if (_state.value.status == RecordStatus.Recording) {
            elapsedBeforePause += (System.currentTimeMillis() - segmentStartMillis) / 1000
        }
        stopJobs()
    }

    private fun stopJobs() {
        trackingJob?.cancel()
        timerJob?.cancel()
        trackingJob = null
        timerJob = null
    }

    private fun addPoint(coordinates: Coordinates) {
        val now = System.currentTimeMillis()
        val point = RecordedPoint(coordinates, now)
        val last = _state.value.path.lastOrNull()

        if (last == null) {
            _state.update { it.copy(path = listOf(point)) }
            resolvePlaceName(coordinates)
            return
        }

        val segment = distanceBetween(last.coordinates, coordinates)
        if (segment < MIN_SEGMENT_METERS) return

        val climb = coordinates.altitude - last.coordinates.altitude
        val gain = if (climb in MIN_ELEVATION_DELTA_METERS..MAX_ELEVATION_DELTA_METERS) climb
        else 0.0

        _state.update {
            it.copy(
                path = it.path + point,
                distanceMeters = it.distanceMeters + segment,
                elevationGainMeters = it.elevationGainMeters + gain
            )
        }
    }

    private fun resolvePlaceName(coordinates: Coordinates) {
        viewModelScope.launch {
            val name = try {
                osmDataSource.reverse(coordinates.latitude, coordinates.longitude)
            } catch (_: Exception) { null }
            _state.update { it.copy(placeName = name) }
        }
    }

    private fun setError(error: RecordError) {
        stopTracking()
        _state.update {
            val status =
                if (it.path.isEmpty()) RecordStatus.Idle else RecordStatus.Paused
            it.copy(status = status, error = error)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopJobs()
    }
}

private fun distanceBetween(a: Coordinates, b: Coordinates): Double {
    val results = FloatArray(1)
    Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
    return results[0].toDouble()
}
