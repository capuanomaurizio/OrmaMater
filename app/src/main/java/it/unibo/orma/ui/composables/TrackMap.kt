package it.unibo.orma.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.unibo.orma.R
import it.unibo.orma.data.location.Coordinates
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Mappa OpenStreetMap con il tracciato dell'escursione.
 *
 * @param followLastPoint se true la mappa insegue l'ultimo punto (registrazione in corso); se false inquadra l'intero tracciato (escursione conclusa).
 */
@Composable
fun TrackMap(
    path: List<Coordinates>,
    modifier: Modifier = Modifier,
    followLastPoint: Boolean = false
) {
    val ctx = LocalContext.current
    val lineColor = MaterialTheme.colorScheme.primary.toArgb()
    val startLabel = stringResource(R.string.map_start)
    val endLabel = stringResource(R.string.map_end)

    var framed by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        factory = { mapView },
        update = { map ->
            map.overlays.clear()

            if (path.isNotEmpty()) {
                val points = path.map { GeoPoint(it.latitude, it.longitude) }

                map.overlays.add(
                    Polyline(map).apply {
                        setPoints(points)
                        outlinePaint.color = lineColor
                        outlinePaint.strokeWidth = 12f
                    }
                )
                map.overlays.add(marker(map, points.first(), startLabel))
                if (points.size > 1) {
                    map.overlays.add(marker(map, points.last(), endLabel))
                }

                if (followLastPoint) {
                    map.controller.setCenter(points.last())
                } else if (!framed) {
                    val box = BoundingBox.fromGeoPoints(points)
                    // Un tracciato quasi rettilineo produce un riquadro di larghezza (o altezza) zero, che zoomToBoundingBox non sa inquadrare: allarghiamo di 100 m
                    val padded = BoundingBox(
                        box.latNorth + 0.001,
                        box.lonEast + 0.001,
                        box.latSouth - 0.001,
                        box.lonWest - 0.001
                    )
                    map.post { map.zoomToBoundingBox(padded, false) }
                    framed = true
                }
            }

            map.invalidate()
        }
    )
}

private fun marker(map: MapView, point: GeoPoint, label: String) =
    Marker(map).apply {
        position = point
        title = label
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
