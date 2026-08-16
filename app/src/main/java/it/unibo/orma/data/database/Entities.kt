package it.unibo.orma.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un'escursione registrata dall'utente.
 */
@Entity(tableName = "hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String = "",
    @ColumnInfo val placeName: String? = null,
    @ColumnInfo val startedAt: Long,
    @ColumnInfo val durationSeconds: Long = 0,
    @ColumnInfo val distanceMeters: Double = 0.0,
    @ColumnInfo val elevationGainMeters: Double = 0.0,
    @ColumnInfo val coverImageUri: String? = null,
    @ColumnInfo val isFavorite: Boolean = false
)

/**
 * Un punto del tracciato GPS, collegato alla relativa escursione.
 */
@Entity(
    tableName = "track_points",
    foreignKeys = [
        ForeignKey(
            entity = Hike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hikeId")]
)
data class TrackPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val hikeId: Long,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longitude: Double,
    @ColumnInfo val altitude: Double = 0.0,
    @ColumnInfo val recordedAt: Long
)
