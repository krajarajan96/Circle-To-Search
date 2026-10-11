package com.circletosearch

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.circletosearch.ocr.OcrProvider
import com.circletosearch.ocr.OcrProviders

// Google-ish "Circle to Search" palette: a cool blue up top, a warm yellow glow down low.
private val DefaultLensBlue = Color(0xFF4285F4)
private val DefaultLensYellow = Color(0xFFFBBC04)
private val DefaultSearchBarBlack = Color(0xFF202124)
private val DefaultSelectionStrokeColor = Color(0xFFFAFAFA)

/** Colors used by [SelectableContent]'s gesture, scrim, and chrome. All fully customizable. */
data class CircleSearchColors(
    val scrimBrush: Brush,
    val topBarBrush: Brush,
    val bottomGlowBrush: Brush,
    val searchBarColor: Color,
    val contentColor: Color,
    val topBarCloseBackgroundColor: Color,
    val resultPanelColor: Color,
    val selectionStrokeColor: Color,
    val selectingDarkenBrush: Brush,
) {
    companion object {
        fun defaults(): CircleSearchColors = CircleSearchColors(
            // A light, uniform 10% tint across the center (not a harsh grey wash) so circled
            // content stays legible, with a bit more color concentrated near the top/bottom chrome.
            scrimBrush = Brush.verticalGradient(
                0f to DefaultLensBlue.copy(alpha = 0.28f),
                0.22f to Color.Black.copy(alpha = 0.10f),
                0.78f to Color.Black.copy(alpha = 0.10f),
                1f to DefaultLensYellow.copy(alpha = 0.08f),
            ),
            topBarBrush = Brush.verticalGradient(listOf(DefaultLensBlue.copy(alpha = 0.85f), Color.Transparent)),
            bottomGlowBrush = Brush.verticalGradient(
                listOf(Color.Transparent, DefaultLensYellow.copy(alpha = 0.16f)),
            ),
            searchBarColor = DefaultSearchBarBlack,
            contentColor = Color.White,
            topBarCloseBackgroundColor = Color.Black.copy(alpha = 0.25f),
            resultPanelColor = Color.Unspecified,
            selectionStrokeColor = DefaultSelectionStrokeColor,
            // A touch darker than the base scrim while actively drawing, so the stroke stands out.
            selectingDarkenBrush = Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.32f),
                0.22f to Color.Black.copy(alpha = 0.10f),
                0.78f to Color.Black.copy(alpha = 0.10f),
                1f to Color.Black.copy(alpha = 0.32f),
            ),
        )
    }
}

/** User-facing copy shown by [SelectableContent]. */
data class CircleSearchStrings(
    val searchTitle: String = "Search",
    val bottomBarHint: String = "Search text or image",
    val searchTextButton: String = "Search text",
    val searchImageButton: String = "Search image",
    val recognizingLabel: String = "Reading text…",
) {
    companion object {
        fun defaults(): CircleSearchStrings = CircleSearchStrings()
    }
}

/**
 * Everything a host screen can customize about [SelectableContent]: the OCR backend, the visual
 * theme, the minimum selection size that counts as a deliberate circle rather than a stray tap,
 * and the bottom chrome shown while the user is drawing a selection.
 *
 * Set [bottomBar] to replace the default "Search text or image" bar with your own composable
 * (e.g. to add suggestion chips, a different hint, or app-specific actions). It's still hosted
 * inside the same animated slide-in/fade-in container as the default bar.
 */
data class CircleSearchConfig(
    val ocrProvider: OcrProvider = OcrProviders.default(),
    val colors: CircleSearchColors = CircleSearchColors.defaults(),
    val strings: CircleSearchStrings = CircleSearchStrings.defaults(),
    val minSelectionSizePx: Int = 20,
    val bottomBar: (@Composable () -> Unit)? = null,
)
