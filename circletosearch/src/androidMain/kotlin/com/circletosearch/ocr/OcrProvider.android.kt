package com.circletosearch.ocr

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/** ML Kit-backed [OcrProvider] used as the Android default. */
private class MlKitOcrProvider : OcrProvider {
    override suspend fun recognizeText(image: ImageBitmap): Result<String> = runCatching {
        val inputImage = InputImage.fromBitmap(image.asAndroidBitmap(), 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage).await().text
    }
}

internal actual fun platformDefaultOcrProvider(): OcrProvider = MlKitOcrProvider()
