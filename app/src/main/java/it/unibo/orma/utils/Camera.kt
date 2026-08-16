package it.unibo.orma.utils

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberCameraLauncher(
    onPictureTaken: (Uri) -> Unit = {}
): Pair<Uri?, () -> Unit> {
    var launcherUri by remember { mutableStateOf<Uri?>(null) }
    var pictureUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { pictureTaken ->
        if (pictureTaken) launcherUri?.let {
            pictureUri = it
            onPictureTaken(it)
        }
    }

    val ctx = LocalContext.current
    val takePicture = {
        val dir = File(ctx.filesDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("orma_", ".jpg", dir)
        launcherUri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
        launcher.launch(launcherUri!!)
    }

    return pictureUri to takePicture
}
