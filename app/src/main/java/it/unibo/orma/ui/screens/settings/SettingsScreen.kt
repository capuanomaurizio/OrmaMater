package it.unibo.orma.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.unibo.orma.R
import it.unibo.orma.data.repositories.ThemeMode
import it.unibo.orma.ui.composables.AppBar

@Composable
fun SettingsScreen(
    navController: NavHostController,
    state: SettingsState,
    onThemeChange: (ThemeMode) -> Unit
) {
    Scaffold(
        topBar = { AppBar(stringResource(R.string.route_settings), navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.size(8.dp))

            ThemeMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.themeMode == mode,
                            onClick = { onThemeChange(mode) }
                        )
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = state.themeMode == mode,
                        onClick = { onThemeChange(mode) }
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        when (mode) {
                            ThemeMode.Light -> stringResource(R.string.theme_light)
                            ThemeMode.Dark -> stringResource(R.string.theme_dark)
                            ThemeMode.System -> stringResource(R.string.theme_system)
                        }
                    )
                }
            }
        }
    }
}
