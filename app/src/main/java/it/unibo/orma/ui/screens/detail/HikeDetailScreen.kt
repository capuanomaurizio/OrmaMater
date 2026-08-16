package it.unibo.orma.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.net.Uri
import it.unibo.orma.R
import it.unibo.orma.data.database.Hike
import it.unibo.orma.ui.composables.AppBar
import it.unibo.orma.ui.composables.ElevationChart
import it.unibo.orma.ui.composables.HikeImage
import it.unibo.orma.ui.composables.TrackMap
import it.unibo.orma.utils.openInMaps
import it.unibo.orma.utils.rememberCameraLauncher
import it.unibo.orma.utils.saveImageToGallery
import it.unibo.orma.utils.shareHike
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HikeDetailScreen(
    navController: NavHostController,
    state: HikeDetailState,
    viewModel: HikeDetailViewModel
) {
    val hike = state.hike
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val (_, takePicture) = rememberCameraLauncher { uri ->
        viewModel.setCoverImage(uri.toString())
    }

    // rememberSaveable e non remember: ruotando il telefono col dialog aperto, questo resta aperto
    var editing by rememberSaveable { mutableStateOf(false) }

    if (editing && hike != null) {
        EditHikeDialog(
            hike = hike,
            onDismiss = { editing = false },
            onConfirm = { title, description ->
                viewModel.rename(title, description)
                editing = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppBar(stringResource(R.string.route_detail), navController) {
                if (hike != null) {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (hike.isFavorite) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            stringResource(R.string.favorite)
                        )
                    }
                    IconButton(onClick = { editing = true }) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.edit))
                    }
                    IconButton(onClick = { shareHike(ctx, hike) }) {
                        Icon(Icons.Outlined.Share, stringResource(R.string.share))
                    }
                    IconButton(onClick = { viewModel.delete { navController.navigateUp() } }) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                    }
                }
            }
        }
    ) { innerPadding ->
        if (hike == null) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), Alignment.Center) {
                Text(stringResource(R.string.detail_not_found))
            }
            return@Scaffold
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(hike.title, style = MaterialTheme.typography.headlineSmall)

            HikeImage(
                uri = hike.coverImageUri,
                placeholderIconSize = 48.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = takePicture, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(
                            if (hike.coverImageUri == null) R.string.photo_take
                            else R.string.photo_retake
                        )
                    )
                }
                if (hike.coverImageUri != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val saved = saveImageToGallery(ctx, Uri.parse(hike.coverImageUri))
                                snackbarHostState.showSnackbar(
                                    ctx.getString(
                                        if (saved) R.string.photo_saved
                                        else R.string.photo_save_failed
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Download, null)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.photo_save_gallery))
                    }
                }
            }

            if (state.path.isNotEmpty()) {
                TrackMap(
                    path = state.path,
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                )
                OutlinedButton(
                    onClick = {
                        val start = state.path.first()
                        val opened = openInMaps(ctx, start.latitude, start.longitude, hike.title)
                        if (!opened) {
                            scope.launch {
                                snackbarHostState.showSnackbar(ctx.getString(R.string.no_maps_app))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Map, null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.open_in_maps))
                }
            } else {
                Text(
                    stringResource(R.string.detail_no_track),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Card(Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Metric(
                        stringResource(R.string.metric_distance),
                        "%.2f km".format(hike.distanceMeters / 1000)
                    )
                    Metric(
                        stringResource(R.string.metric_duration),
                        formatDuration(hike.durationSeconds)
                    )
                    Metric(
                        stringResource(R.string.metric_elevation),
                        "%.0f m".format(hike.elevationGainMeters)
                    )
                }
            }

            if (state.path.size >= 2) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.stats_elevation_profile),
                            style = MaterialTheme.typography.titleSmall
                        )
                        ElevationChart(
                            altitudes = state.path.map { it.altitude },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }

            Text(
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    .format(Date(hike.startedAt)),
                style = MaterialTheme.typography.bodyMedium
            )

            hike.placeName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            if (hike.description.isNotBlank()) {
                Text(hike.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EditHikeDialog(
    hike: Hike,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(hike.title) }
    var description by rememberSaveable { mutableStateOf(hike.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_hike)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.record_title_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.record_description_label)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), description.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.record_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
