package com.circletosearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect

/** Outcome handed back to whichever screen hosts [SelectableContent]. */
sealed interface CircleSearchResult {
    data class TextQuery(val text: String, val image: ImageBitmap) : CircleSearchResult
    data class ImageQuery(val image: ImageBitmap) : CircleSearchResult
}

/** State machine driving the circle-to-search gesture overlay. */
sealed interface SelectionMode {
    data object Idle : SelectionMode
    data class Selecting(val points: List<Offset>) : SelectionMode
    data class Selected(val bounds: IntRect, val preview: ImageBitmap) : SelectionMode
    data class RecognizingText(val bounds: IntRect, val preview: ImageBitmap) : SelectionMode
}

/** Plain `mutableStateOf`-backed controller for [SelectableContent] (no ViewModel/DI required). */
class SelectionController {
    var mode: SelectionMode by mutableStateOf(SelectionMode.Idle)
        private set

    fun startSelecting() {
        mode = SelectionMode.Selecting(emptyList())
    }

    fun appendPoint(point: Offset) {
        val current = mode
        if (current is SelectionMode.Selecting) {
            mode = current.copy(points = current.points + point)
        }
    }

    fun finishSelecting(bounds: IntRect, preview: ImageBitmap) {
        mode = SelectionMode.Selected(bounds, preview)
    }

    fun beginRecognizingText() {
        val current = mode
        if (current is SelectionMode.Selected) {
            mode = SelectionMode.RecognizingText(current.bounds, current.preview)
        }
    }

    fun reset() {
        mode = SelectionMode.Idle
    }
}

@Composable
fun rememberSelectionController(): SelectionController = remember { SelectionController() }
