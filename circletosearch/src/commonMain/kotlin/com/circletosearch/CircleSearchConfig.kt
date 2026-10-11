package com.circletosearch

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.circletosearch.ocr.OcrProvider
import com.circletosearch.ocr.OcrProviders

// Google-ish "Circle to Search" palette: a cool blue up top, a barely-there warm cream down low.
private val DefaultLensBlue = Color(0xFF4285F4)
private val DefaultLensPaleCream = Color(0xFFFFF8E1)
private val DefaultSearchBarBlack = Color(0xFF202124)
private val DefaultSelectionStrokeColor = Color(0xFFFAFAFA)

/** Colors used by [SelectableContent]'s gesture, scrim, and chrome. */
data class CircleSearchColors(
    val scrimBrush: Brush,
    val topBarBrush: Brush,
    val searchBarColor: Color,
    val selectionStrokeColor: Color,
    val selectingDarkenColor: Color,
) {
    companion object {
        fun defaults(): CircleSearchColors = CircleSearchColors(
            scrimBrush = Brush.linearGradient(
                0f to DefaultLensBlue.copy(alpha = 0.22f),
                1f to DefaultLensPaleCream.copy(alpha = 0.16f),
            ),
            topBarBrush = Brush.verticalGradient(listOf(DefaultLensBlue.copy(alpha = 0.85f), Color.Transparent)),
            searchBarColor = DefaultSearchBarBlack,
            selectionStrokeColor = DefaultSelectionStrokeColor,
            selectingDarkenColor = Color.Black.copy(alpha = 0.22f),
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
 * theme, and the minimum selection size that counts as a deliberate circle rather than a stray tap.
 */
data class CircleSearchConfig(
    val ocrProvider: OcrProvider = OcrProviders.default(),
    val colors: CircleSearchColors = CircleSearchColors.defaults(),
    val strings: CircleSearchStrings = CircleSearchStrings.defaults(),
    val minSelectionSizePx: Int = 20,
)
