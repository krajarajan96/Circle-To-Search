package com.circletosearch

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Returns a copy of this bitmap guaranteed to be software-backed.
 *
 * [androidx.compose.ui.graphics.layer.GraphicsLayer.toImageBitmap] can return a
 * hardware-backed bitmap on Android, which `Canvas.drawImageRect` cannot draw into another
 * software canvas (`Software rendering doesn't support hardware bitmaps`). Cropping requires
 * a software bitmap, so callers must convert before calling [cropImageBitmap].
 */
expect fun ImageBitmap.toSoftwareImageBitmap(): ImageBitmap
