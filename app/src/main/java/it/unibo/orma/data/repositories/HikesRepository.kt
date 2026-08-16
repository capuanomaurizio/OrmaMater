package it.unibo.orma.data.repositories

import it.unibo.orma.data.database.Hike
import it.unibo.orma.data.database.HikesDAO
import it.unibo.orma.data.database.TrackPoint
import it.unibo.orma.data.database.TrackPointsDAO
import kotlinx.coroutines.flow.Flow

class HikesRepository(
    private val hikesDAO: HikesDAO,
    private val trackPointsDAO: TrackPointsDAO
) {
    val hikes: Flow<List<Hike>> = hikesDAO.getAll()

    fun hikeById(id: Long): Flow<Hike?> = hikesDAO.getById(id)
    fun trackPoints(hikeId: Long): Flow<List<TrackPoint>> = trackPointsDAO.getByHike(hikeId)

    suspend fun insert(hike: Hike): Long = hikesDAO.insert(hike)
    suspend fun upsert(hike: Hike) = hikesDAO.upsert(hike)
    suspend fun delete(hike: Hike) = hikesDAO.delete(hike)

    suspend fun toggleFavorite(hike: Hike) = hikesDAO.upsert(hike.copy(isFavorite = !hike.isFavorite))

    suspend fun saveTrack(points: List<TrackPoint>) = trackPointsDAO.insertAll(points)
}
