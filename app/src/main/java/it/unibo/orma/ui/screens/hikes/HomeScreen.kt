package it.unibo.orma.ui.screens.hikes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.unibo.orma.R
import it.unibo.orma.data.database.Hike
import it.unibo.orma.ui.OrmaRoute
import it.unibo.orma.ui.composables.AppBar
import it.unibo.orma.ui.composables.HikeImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    state: HikesState,
    actions: HikesActions
) {
    Scaffold(
        topBar = {
            AppBar(stringResource(R.string.route_home), navController) {
                IconButton(onClick = actions.toggleOnlyFavorites) {
                    Icon(
                        if (state.onlyFavorites) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        stringResource(R.string.filter_favorites)
                    )
                }
                IconButton(onClick = { navController.navigate(OrmaRoute.Stats) }) {
                    Icon(Icons.Outlined.BarChart, stringResource(R.string.route_stats))
                }
                IconButton(onClick = { navController.navigate(OrmaRoute.Profile) }) {
                    Icon(Icons.Outlined.Person, stringResource(R.string.route_profile))
                }
                IconButton(onClick = { navController.navigate(OrmaRoute.Settings) }) {
                    Icon(Icons.Outlined.Settings, stringResource(R.string.route_settings))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(OrmaRoute.Record) }) {
                Icon(Icons.Outlined.Add, stringResource(R.string.route_record))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            OutlinedTextField(
                value = state.query,
                onValueChange = actions.setQuery,
                label = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, stringResource(R.string.search))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.hikes.isEmpty()) {
                val messaggio = when {
                    state.query.isNotBlank() ->
                        stringResource(R.string.home_no_results, state.query)
                    state.onlyFavorites -> stringResource(R.string.home_no_favorites)
                    else -> stringResource(R.string.home_empty)
                }
                Box(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        messaggio,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.hikes, key = { it.id }) { hike ->
                        HikeCard(
                            hike = hike,
                            onClick = {
                                navController.navigate(OrmaRoute.HikeDetail(hike.id))
                            },
                            onToggleFavorite = { actions.toggleFavorite(hike) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HikeCard(hike: Hike, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            HikeImage(
                uri = hike.coverImageUri,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(hike.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "%.1f km · %s".format(
                        hike.distanceMeters / 1000,
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(hike.startedAt))
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (hike.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    stringResource(R.string.favorite)
                )
            }
        }
    }
}
