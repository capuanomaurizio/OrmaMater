package it.unibo.orma.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import it.unibo.orma.R
import it.unibo.orma.data.Badge
import it.unibo.orma.ui.composables.AppBar
import it.unibo.orma.ui.composables.BarChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(navController: NavHostController, state: StatsState) {
    Scaffold(
        topBar = { AppBar(stringResource(R.string.route_stats), navController) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.stats_monthly),
                        style = MaterialTheme.typography.titleMedium
                    )
                    BarChart(
                        values = state.monthly.map { it.kilometers },
                        labels = state.monthly.map { it.label },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            }

            Text(
                stringResource(R.string.stats_badges),
                style = MaterialTheme.typography.titleMedium
            )

            state.badges.forEach { (badge, unlockedAt) ->
                BadgeRow(badge, unlockedAt)
            }
        }
    }
}

@Composable
private fun BadgeRow(badge: Badge, unlockedAt: Long?) {
    val unlocked = unlockedAt != null

    Card(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    stringResource(badge.title),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) Color.Unspecified
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (unlockedAt != null) {
                        stringResource(
                            R.string.badge_unlocked_on,
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(unlockedAt))
                        )
                    } else {
                        stringResource(badge.description)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
