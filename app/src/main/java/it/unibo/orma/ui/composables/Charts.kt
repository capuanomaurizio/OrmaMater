package it.unibo.orma.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.unibo.orma.R

/**
 * Istogramma disegnato a mano su Canvas.
 *
 * nessuna libreria di grafici: per due grafici semplici una dipendenza in più sarebbe peso inutile contro le poche righe di Canvas.
 */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = values.maxOrNull() ?: 0f

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            if (values.isEmpty() || maxValue <= 0f) return@Canvas

            val slot = size.width / values.size
            val barWidth = slot * 0.6f
            val gap = (slot - barWidth) / 2

            values.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * size.height
                drawRect(
                    color = barColor,
                    topLeft = Offset(index * slot + gap, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ElevationChart(
    altitudes: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (altitudes.size < 2) return

    val minAltitude = altitudes.min()
    val maxAltitude = altitudes.max()
    val range = (maxAltitude - minAltitude).takeIf { it > 0.5 }

    Column(modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.chart_max_altitude, maxAltitude),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                stringResource(R.string.chart_min_altitude, minAltitude),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val stepX = size.width / (altitudes.size - 1)

            fun yFor(altitude: Double): Float =
                if (range == null) size.height / 2
                else (size.height * (1 - (altitude - minAltitude) / range)).toFloat()

            val line = Path().apply {
                moveTo(0f, yFor(altitudes.first()))
                altitudes.forEachIndexed { index, altitude ->
                    lineTo(index * stepX, yFor(altitude))
                }
            }

            val area = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(area, lineColor.copy(alpha = 0.2f))
            drawPath(line, lineColor, style = Stroke(width = 4f))
        }
    }
}
