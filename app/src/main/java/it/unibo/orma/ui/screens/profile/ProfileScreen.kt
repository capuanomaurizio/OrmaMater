package it.unibo.orma.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.unibo.orma.R
import it.unibo.orma.ui.composables.AppBar
import it.unibo.orma.ui.composables.HikeImage
import it.unibo.orma.utils.copyToInternalStorage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    state: ProfileState,
    viewModel: ProfileViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.username) {
        if (state.username != null && name.isBlank()) name = state.username
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                copyToInternalStorage(ctx, uri, "avatar.jpg")?.let {
                    viewModel.setAvatarUri(it.toString())
                }
            }
        }
    }

    Scaffold(
        topBar = { AppBar(stringResource(R.string.route_profile), navController) }
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
            HikeImage(
                uri = state.avatarUri,
                placeholderIconSize = 48.dp,
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )

            OutlinedButton(
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Outlined.PhotoLibrary, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.profile_pick_avatar))
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.setUsername(it)
                },
                label = { Text(stringResource(R.string.profile_username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Card(Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.profile_stats),
                        style = MaterialTheme.typography.titleMedium
                    )
                    StatRow(
                        stringResource(R.string.profile_hike_count),
                        state.hikeCount.toString()
                    )
                    StatRow(
                        stringResource(R.string.profile_total_distance),
                        "%.1f km".format(state.totalDistanceMeters / 1000)
                    )
                    StatRow(
                        stringResource(R.string.profile_total_elevation),
                        "%.0f m".format(state.totalElevationMeters)
                    )
                    StatRow(
                        stringResource(R.string.profile_longest),
                        "%.1f km".format(state.longestHikeMeters / 1000)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
