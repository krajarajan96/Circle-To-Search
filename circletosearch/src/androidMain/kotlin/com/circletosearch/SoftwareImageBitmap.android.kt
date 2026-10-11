package com.circletosearch

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ImageBitmap.toSoftwareImageBitmap(): ImageBitmap {
    val androidBitmap = asAndroidBitmap()
    return if (androidBitmap.config == Bitmap.Config.HARDWARE) {
        androidBitmap.copy(Bitmap.Config.ARGB_8888, false).asImageBitmap()
    } else {
        this
    }
}
