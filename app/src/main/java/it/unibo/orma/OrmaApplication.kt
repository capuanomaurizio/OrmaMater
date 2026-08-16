package it.unibo.orma

import android.app.Application
import it.unibo.orma.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.osmdroid.config.Configuration
import java.io.File

class OrmaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@OrmaApplication)
            modules(appModule)
        }

        // impostato User-Agent prima che venga creata la prima MapView così che la richiesta non venga rifiutata
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }
    }
}
