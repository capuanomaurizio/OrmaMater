package it.unibo.orma.data

import androidx.annotation.StringRes
import it.unibo.orma.R
import it.unibo.orma.data.database.Hike

enum class Badge(
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    FirstHike(R.string.badge_first_hike, R.string.badge_first_hike_desc),
    FiveHikes(R.string.badge_five_hikes, R.string.badge_five_hikes_desc),
    TenHikes(R.string.badge_ten_hikes, R.string.badge_ten_hikes_desc),
    TenKm(R.string.badge_ten_km, R.string.badge_ten_km_desc),
    FiftyKm(R.string.badge_fifty_km, R.string.badge_fifty_km_desc),
    ThousandMeters(R.string.badge_thousand_meters, R.string.badge_thousand_meters_desc)
}

/**
 * Calcola quali traguardi sono stati raggiunti e quando.
 *
 * I badge non hanno una tabella dedicata: sono **derivati** dalle escursioni salvate.
 * Ripercorrendo la cronologia in ordine si ottiene anche la data di sblocco, senza
 * dover mantenere uno stato duplicato che potrebbe andare fuori sincrono con i dati.
 *
 * @return per ogni badge, l'istante in cui è stato sbloccato, oppure null.
 */
fun unlockedBadges(hikes: List<Hike>): Map<Badge, Long?> {
    val unlocked = Badge.entries.associateWith { null as Long? }.toMutableMap()

    var count = 0
    var totalDistance = 0.0
    var totalElevation = 0.0

    hikes.sortedBy { it.startedAt }.forEach { hike ->
        count++
        totalDistance += hike.distanceMeters
        totalElevation += hike.elevationGainMeters

        fun unlock(badge: Badge) {
            if (unlocked[badge] == null) unlocked[badge] = hike.startedAt
        }

        if (count >= 1) unlock(Badge.FirstHike)
        if (count >= 5) unlock(Badge.FiveHikes)
        if (count >= 10) unlock(Badge.TenHikes)
        if (totalDistance >= 10_000) unlock(Badge.TenKm)
        if (totalDistance >= 50_000) unlock(Badge.FiftyKm)
        if (totalElevation >= 1_000) unlock(Badge.ThousandMeters)
    }

    return unlocked
}
