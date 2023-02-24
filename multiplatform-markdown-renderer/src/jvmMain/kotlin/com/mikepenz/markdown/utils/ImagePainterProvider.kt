package com.mikepenz.markdown.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.mikepenz.markdown.compose.LocalDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@Composable
internal actual fun imagePainter(url: String): Painter? {
    val uri = URI(url)
    if (uri.scheme == null) {
        return loadLocalImage(url)?.let { BitmapPainter(it) }
    }
    return fetchImage(url)?.let { BitmapPainter(it) }
}

@Composable
fun loadLocalImage(url: String): ImageBitmap? {
    val file = LocalDir.current.resolve(url)
    if (file.exists()) {
        return Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap()
    }
    return null
}

@Composable
fun fetchImage(url: String): ImageBitmap? {
    var image by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        image = loadPicture(url)
    }
    return image
}

suspend fun loadPicture(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.connect()

        val input: InputStream = connection.inputStream
        Image.makeFromEncoded(input.readBytes()).toComposeImageBitmap()
    }.getOrNull()
}