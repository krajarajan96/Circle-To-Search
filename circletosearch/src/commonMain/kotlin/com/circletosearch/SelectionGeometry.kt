package com.circletosearch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

/**
 * Computes the axis-aligned bounding box of a drawn selection path.
 * Returns null when there are fewer than two points, since no meaningful area is enclosed.
 */
fun boundingRect(points: List<Offset>): Rect? {
    if (points.size < 2) return null
    var minX = points[0].x
    var minY = points[0].y
    var maxX = points[0].x
    var maxY = points[0].y
    for (point in points) {
        minX = min(minX, point.x)
        minY = min(minY, point.y)
        maxX = max(maxX, point.x)
        maxY = max(maxY, point.y)
    }
    return Rect(minX, minY, maxX, maxY)
}

/** Clamps this rect to fit within the given container bounds, preserving as much area as possible. */
fun Rect.clampTo(containerSize: IntSize): Rect {
    val maxWidth = containerSize.width.toFloat()
    val maxHeight = containerSize.height.toFloat()
    val left = left.coerceIn(0f, maxWidth)
    val top = top.coerceIn(0f, maxHeight)
    val right = right.coerceIn(left, maxWidth)
    val bottom = bottom.coerceIn(top, maxHeight)
    return Rect(left, top, right, bottom)
}

/**
 * Converts this rect to a pixel-aligned [IntRect], or null if it's too small to be a meaningful
 * selection (e.g. an accidental tap rather than a drawn circle).
 */
fun Rect.toIntRectOrNull(minSizePx: Int = 20): IntRect? {
    val intRect = IntRect(
        left = left.toInt(),
        top = top.toInt(),
        right = right.toInt(),
        bottom = bottom.toInt(),
    )
    if (intRect.width < minSizePx || intRect.height < minSizePx) return null
    return intRect
}
