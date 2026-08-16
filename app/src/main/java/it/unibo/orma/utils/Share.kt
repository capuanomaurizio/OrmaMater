package it.unibo.orma.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import it.unibo.orma.R
import it.unibo.orma.data.database.Hike

fun shareHike(ctx: Context, hike: Hike) {
    val text = buildString {
        appendLine(hike.title)
        append("%.2f km".format(hike.distanceMeters / 1000))
        if (hike.elevationGainMeters > 0) {
            append(" · ")
            append(
                ctx.getString(
                    R.string.share_elevation,
                    "%.0f m".format(hike.elevationGainMeters)
                )
            )
        }
        hike.placeName?.let {
            appendLine()
            append(it)
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_SUBJECT, hike.title)
        putExtra(Intent.EXTRA_TEXT, text)
        if (hike.coverImageUri != null) {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(hike.coverImageUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }

    ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.share_chooser)))
}

fun openInMaps(ctx: Context, latitude: Double, longitude: Double, label: String): Boolean {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    return if (intent.resolveActivity(ctx.packageManager) != null) {
        ctx.startActivity(intent)
        true
    } else {
        false
    }
}
