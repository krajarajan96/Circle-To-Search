package com.circletosearch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectionGeometryTest {

    @Test
    fun boundingRect_returnsNull_forEmptyList() {
        assertNull(boundingRect(emptyList()))
    }

    @Test
    fun boundingRect_returnsNull_forSinglePoint() {
        assertNull(boundingRect(listOf(Offset(10f, 10f))))
    }

    @Test
    fun boundingRect_computesMinMax_forMultiplePoints() {
        val points = listOf(
            Offset(10f, 50f),
            Offset(40f, 10f),
            Offset(5f, 30f),
            Offset(60f, 70f),
        )
        assertEquals(Rect(5f, 10f, 60f, 70f), boundingRect(points))
    }

    @Test
    fun clampTo_shrinksRectToContainerBounds() {
        val rect = Rect(-10f, -10f, 150f, 150f)
        val clamped = rect.clampTo(IntSize(100, 100))
        assertEquals(Rect(0f, 0f, 100f, 100f), clamped)
    }

    @Test
    fun clampTo_leavesRectInsideBoundsUnchanged() {
        val rect = Rect(10f, 10f, 50f, 50f)
        val clamped = rect.clampTo(IntSize(100, 100))
        assertEquals(rect, clamped)
    }

    @Test
    fun toIntRectOrNull_rejectsDegenerateSelection() {
        val tiny = Rect(0f, 0f, 5f, 5f)
        assertNull(tiny.toIntRectOrNull(minSizePx = 20))
    }

    @Test
    fun toIntRectOrNull_acceptsSelectionAtOrAboveMinSize() {
        val rect = Rect(0f, 0f, 20f, 20f)
        val intRect = rect.toIntRectOrNull(minSizePx = 20)
        assertEquals(20, intRect?.width)
        assertEquals(20, intRect?.height)
    }
}
