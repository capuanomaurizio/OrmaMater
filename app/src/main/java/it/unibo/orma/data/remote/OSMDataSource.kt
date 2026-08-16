package it.unibo.orma.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSMReverseResult(
    @SerialName("display_name") val displayName: String? = null
)

class OSMDataSource(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://nominatim.openstreetmap.org"
    }

    /** Risolve delle coordinate nel nome leggibile del luogo. */
    suspend fun reverse(latitude: Double, longitude: Double): String? {
        val url = "$BASE_URL/reverse?lat=$latitude&lon=$longitude&format=json"
        return httpClient.get(url).body<OSMReverseResult>().displayName
    }
}
