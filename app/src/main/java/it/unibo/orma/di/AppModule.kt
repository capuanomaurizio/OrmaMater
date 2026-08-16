package it.unibo.orma.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import it.unibo.orma.data.database.OrmaDatabase
import it.unibo.orma.data.location.LocationService
import it.unibo.orma.data.remote.OSMDataSource
import it.unibo.orma.data.repositories.HikesRepository
import it.unibo.orma.data.repositories.SettingsRepository
import it.unibo.orma.ui.screens.detail.HikeDetailViewModel
import it.unibo.orma.ui.screens.hikes.HikesViewModel
import it.unibo.orma.ui.screens.profile.ProfileViewModel
import it.unibo.orma.ui.screens.stats.StatsViewModel
import it.unibo.orma.ui.screens.record.RecordViewModel
import it.unibo.orma.ui.screens.settings.SettingsViewModel
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// una sola istanza di DataStore (che agisce come estensione di Contecxt) per tutta l'app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

val appModule = module {

    single { get<Context>().dataStore }

    single {
        Room.databaseBuilder(
            androidContext(),
            OrmaDatabase::class.java,
            "orma-db"
        ).build()
    }

    single {
        HttpClient {
            defaultRequest {
                headers.append(HttpHeaders.UserAgent, "OrmaMater/1.0 (it.unibo.orma)")
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single { OSMDataSource(get()) }
    single { LocationService(androidContext()) }
    single { SettingsRepository(get()) }
    single {
        HikesRepository(
            get<OrmaDatabase>().hikesDAO(),
            get<OrmaDatabase>().trackPointsDAO()
        )
    }

    viewModel { HikesViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { RecordViewModel(get(), get(), get()) }
    viewModel { (hikeId: Long) -> HikeDetailViewModel(hikeId, get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { StatsViewModel(get()) }
}
