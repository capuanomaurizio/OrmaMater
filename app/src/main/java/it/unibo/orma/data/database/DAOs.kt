package it.unibo.orma.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HikesDAO {

    @Query("SELECT * FROM hikes ORDER BY startedAt DESC")
    fun getAll(): Flow<List<Hike>>

    @Query("SELECT * FROM hikes WHERE id = :id")
    fun getById(id: Long): Flow<Hike?>

    @Insert
    suspend fun insert(hike: Hike): Long

    @Upsert
    suspend fun upsert(hike: Hike)

    @Delete
    suspend fun delete(hike: Hike)
}

@Dao
interface TrackPointsDAO {

    @Query("SELECT * FROM track_points WHERE hikeId = :hikeId ORDER BY recordedAt ASC")
    fun getByHike(hikeId: Long): Flow<List<TrackPoint>>

    @Insert
    suspend fun insertAll(points: List<TrackPoint>)
}
