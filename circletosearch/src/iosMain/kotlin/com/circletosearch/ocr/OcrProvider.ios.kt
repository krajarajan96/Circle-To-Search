package com.circletosearch.ocr

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

/** Vision-framework-backed [OcrProvider] used as the iOS default. */
private class VisionOcrProvider : OcrProvider {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun recognizeText(image: ImageBitmap): Result<String> = runCatching {
        val pngData = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode selection as PNG")
        val uiImage = UIImage(data = pngData.bytes.toNSData())
        val cgImage = uiImage.CGImage ?: error("No CGImage available for selection")

        suspendCancellableCoroutine { continuation ->
            val request = VNRecognizeTextRequest { request, error ->
                if (error != null) {
                    continuation.resumeWithException(IllegalStateException(error.localizedDescription))
                    return@VNRecognizeTextRequest
                }
                @Suppress("UNCHECKED_CAST")
                val observations = request?.results as? List<VNRecognizedTextObservation> ?: emptyList()
                val text = observations
                    .mapNotNull { (it.topCandidates(maxCandidateCount = 1u).firstOrNull() as? VNRecognizedText)?.string }
                    .joinToString("\n")
                continuation.resume(text)
            }
            request.recognitionLevel = VNRequestTextRecognitionLevelAccurate

            val handler = VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any?>())
            handler.performRequests(listOf(request), error = null)
        }
    }
}

internal actual fun platformDefaultOcrProvider(): OcrProvider = VisionOcrProvider()
