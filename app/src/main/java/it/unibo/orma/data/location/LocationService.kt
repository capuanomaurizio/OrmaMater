package it.unibo.orma.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0
)

class LocationService(private val ctx: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
    private val locationManager =
        ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** Emette la posizione ogni [intervalMillis], finché qualcuno raccoglie il Flow. */
    @SuppressLint("MissingPermission") // il permesso è verificato subito sotto
    fun locationUpdates(intervalMillis: Long = 5_000): Flow<Coordinates> = callbackFlow {
        val granted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) throw SecurityException("Location permission not granted")

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw IllegalStateException("Location is disabled")
        }

        val request = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            // da fermi il GPS oscilla e senza soglia si accumulerebbero punti quasi identici.
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toCoordinates()) }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }
}

private fun Location.toCoordinates() = Coordinates(latitude, longitude, altitude)
