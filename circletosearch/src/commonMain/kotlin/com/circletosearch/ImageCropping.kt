package com.circletosearch

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/**
 * Crops [source] to [rect], returning a new, smaller [ImageBitmap].
 *
 * Pure common code: [ImageBitmap], [Canvas], and [Canvas.drawImageRect] are all part of the
 * `compose-ui` common artifact, so no platform-specific bitmap handling is needed here.
 */
fun cropImageBitmap(source: ImageBitmap, rect: IntRect): ImageBitmap {
    val cropped = ImageBitmap(rect.width, rect.height)
    val canvas = Canvas(cropped)
    canvas.drawImageRect(
        image = source,
        srcOffset = IntOffset(rect.left, rect.top),
        srcSize = IntSize(rect.width, rect.height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(rect.width, rect.height),
        paint = Paint(),
    )
    return cropped
}
