package com.circletosearch.ocr

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Pluggable text-recognition backend for [com.circletosearch.SelectableContent].
 *
 * Implement this to swap the OCR vendor, run recognition server-side, or disable text search
 * (via [OcrProviders.disabled]) while still allowing image search.
 */
interface OcrProvider {
    suspend fun recognizeText(image: ImageBitmap): Result<String>
}

/** Built-in [OcrProvider] implementations. */
object OcrProviders {
    /** The platform's on-device recognizer: ML Kit on Android, Vision on iOS. */
    fun default(): OcrProvider = platformDefaultOcrProvider()

    /** Always fails, so [com.circletosearch.SelectableContent] can hide its "Search text" affordance. */
    fun disabled(): OcrProvider = object : OcrProvider {
        override suspend fun recognizeText(image: ImageBitmap): Result<String> =
            Result.failure(UnsupportedOperationException("Text recognition is disabled"))
    }
}

internal expect fun platformDefaultOcrProvider(): OcrProvider
