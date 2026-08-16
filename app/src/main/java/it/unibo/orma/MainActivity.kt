package it.unibo.orma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import it.unibo.orma.ui.OrmaNavGraph
import it.unibo.orma.ui.screens.settings.SettingsViewModel
import it.unibo.orma.ui.theme.OrmaTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm = koinViewModel<SettingsViewModel>()
            val settingsState by settingsVm.state.collectAsStateWithLifecycle()

            OrmaTheme(themeMode = settingsState.themeMode) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    OrmaNavGraph(navController, settingsVm)
                }
            }
        }
    }
}
