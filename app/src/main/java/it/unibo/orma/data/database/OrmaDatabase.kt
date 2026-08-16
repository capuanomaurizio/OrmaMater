package it.unibo.orma.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Hike::class, TrackPoint::class],
    version = 1,
    exportSchema = false
)
abstract class OrmaDatabase : RoomDatabase() {
    abstract fun hikesDAO(): HikesDAO
    abstract fun trackPointsDAO(): TrackPointsDAO
}
