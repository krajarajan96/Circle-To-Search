# Circle-To-Search

A Compose Multiplatform (Android + iOS) SDK, built on Material 3, for a Google Lens-style "circle
to search" gesture: users draw a lasso over on-screen content, then search either the recognized
text (on-device OCR) or the raw cropped image. Can be dropped onto any screen, with fully
customizable colors/tints, animated chrome, strings, OCR backend, and an overridable bottom bar.

## Usage

```kotlin
SelectableContent(
    onResult = { result ->
        when (result) {
            is CircleSearchResult.TextQuery -> /* result.text, result.image */
            is CircleSearchResult.ImageQuery -> /* result.image */
        }
    },
) {
    // Your screen's content
}
```

### Customization

Pass a `CircleSearchConfig` to override colors, strings, the minimum selection size, the OCR
backend, or the bottom bar shown while selecting:

```kotlin
SelectableContent(
    onResult = { ... },
    config = CircleSearchConfig(
        ocrProvider = MyCustomOcrProvider(), // or OcrProviders.disabled() to hide text search
        colors = CircleSearchColors.defaults().copy(
            searchBarColor = Color.Black,
            contentColor = Color.White,          // text/icon tint across the chrome
            bottomGlowBrush = myBrandGlowBrush,   // accent glow anchored at the bottom edge
        ),
        strings = CircleSearchStrings.defaults().copy(searchTitle = "Find"),
        bottomBar = { MyCustomBottomBar() },      // fully replace the default search bar
    ),
) { /* ... */ }
```

`CircleSearchColors` exposes every color and brush used by the gesture overlay — the scrim, the
top/bottom bar gradients, the bottom glow, the selection stroke, and the dimming shown while
actively drawing — so a host app can reskin the whole experience without forking the library. The
top bar, bottom bar, and result panel all animate in/out (slide + fade), and the scrim/dimming
layers crossfade with them.

By default, `OcrProviders.default()` uses ML Kit on Android and the Vision framework on iOS.

## Modules

- `circletosearch` — the library (publish as `com.circletosearch:circletosearch`).
- `sample` — a minimal Android + iOS app demonstrating both the text-query and image-query flows.

## Building

```
./gradlew :circletosearch:build
./gradlew :sample:assembleDebug
```

The iOS sample app lives in `iosApp/` (open `iosApp/iosApp.xcodeproj` in Xcode); it embeds the
`sample` module's Kotlin/Native framework via the existing Gradle build.

## License

MIT — see [LICENSE](LICENSE).
