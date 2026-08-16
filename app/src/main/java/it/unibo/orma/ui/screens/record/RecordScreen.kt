package it.unibo.orma.ui.screens.record

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.unibo.orma.R
import it.unibo.orma.ui.composables.AppBar
import it.unibo.orma.ui.composables.HikeImage
import it.unibo.orma.ui.composables.TrackMap
import it.unibo.orma.utils.PermissionStatus
import it.unibo.orma.utils.rememberCameraLauncher
import it.unibo.orma.utils.rememberMultiplePermissions
import java.util.Locale

@Composable
fun RecordScreen(
    navController: NavHostController,
    state: RecordState,
    viewModel: RecordViewModel
) {
    val ctx = LocalContext.current
    var showPermissionDeniedAlert by rememberSaveable { mutableStateOf(false) }

    // passiamo tramite intent quindi non serve permesso
    val (_, takePicture) = rememberCameraLauncher { uri ->
        viewModel.setPhoto(uri.toString())
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.navigateUp()
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { statuses ->
        // basta  uno dei due permessi per poter usare il GPS.
        if (statuses.any { it.value == PermissionStatus.Granted }) viewModel.start()
        else showPermissionDeniedAlert = true
    }

    fun startOrRequestPermission() {
        if (locationPermissions.statuses.any { it.value.isGranted }) viewModel.start()
        else locationPermissions.launchPermissionRequest()
    }

    Scaffold(
        topBar = { AppBar(stringResource(R.string.route_record), navController) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            state.error?.let { error ->
                Text(
                    stringResource(
                        when (error) {
                            RecordError.LocationDisabled -> R.string.error_location_disabled
                            RecordError.PermissionDenied -> R.string.error_permission_denied
                        }
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Metrics(state)

            if (state.path.isNotEmpty()) {
                TrackMap(
                    path = state.path.map { it.coordinates },
                    followLastPoint = state.status != RecordStatus.Finished,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }

            if (state.status == RecordStatus.Recording && state.path.isEmpty()) {
                Text(
                    stringResource(R.string.record_waiting),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            state.placeName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.size(8.dp))

            when (state.status) {
                RecordStatus.Idle -> Button(
                    onClick = ::startOrRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.record_start)) }

                RecordStatus.Recording -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = viewModel::pause, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_pause))
                    }
                    Button(onClick = viewModel::finish, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_finish))
                    }
                }

                RecordStatus.Paused -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = viewModel::resume, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_resume))
                    }
                    OutlinedButton(onClick = viewModel::finish, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_finish))
                    }
                }

                RecordStatus.Finished -> SaveForm(
                    photoUri = state.photoUri,
                    onTakePhoto = takePicture,
                    fallbackTitle = stringResource(R.string.record_untitled),
                    onSave = viewModel::save,
                    onDiscard = {
                        viewModel.discard()
                        navController.navigateUp()
                    }
                )
            }
        }
    }

    if (showPermissionDeniedAlert) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedAlert = false },
            title = { Text(stringResource(R.string.permission_denied_title)) },
            text = { Text(stringResource(R.string.permission_denied_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedAlert = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", ctx.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(ctx.packageManager) != null) {
                        ctx.startActivity(intent)
                    }
                }) { Text(stringResource(R.string.permission_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedAlert = false }) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }
}

@Composable
private fun Metrics(state: RecordState) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Metric(
                stringResource(R.string.metric_distance),
                "%.2f km".format(state.distanceMeters / 1000)
            )
            Metric(
                stringResource(R.string.metric_duration),
                formatDuration(state.elapsedSeconds)
            )
            Metric(
                stringResource(R.string.metric_elevation),
                "%.0f m".format(state.elevationGainMeters)
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SaveForm(
    photoUri: String?,
    onTakePhoto: () -> Unit,
    fallbackTitle: String,
    onSave: (String, String) -> Unit,
    onDiscard: () -> Unit
) {
    // rememberSaveable: ruotando il telefono mentre si scrive il nome dell'escursione, con remember quello che si è digitato andrebbe perso
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HikeImage(
            uri = photoUri,
            placeholderIconSize = 48.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        OutlinedButton(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.PhotoCamera, null)
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(
                    if (photoUri == null) R.string.photo_take else R.string.photo_retake
                )
            )
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.record_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.record_description_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.record_discard))
            }
            Button(
                onClick = { onSave(title.ifBlank { fallbackTitle }, description) },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.record_save)) }
        }
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
