# Circle-To-Search

A Compose Multiplatform (Android + iOS) SDK for a Google Lens-style "circle to search" gesture:
users draw a lasso over on-screen content, then search either the recognized text (on-device OCR)
or the raw cropped image. Can be dropped onto any screen, with customizable colors, strings, and
OCR backend.

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

Pass a `CircleSearchConfig` to override colors, strings, the minimum selection size, or the OCR
backend:

```kotlin
SelectableContent(
    onResult = { ... },
    config = CircleSearchConfig(
        ocrProvider = MyCustomOcrProvider(), // or OcrProviders.disabled() to hide text search
        colors = CircleSearchColors.defaults().copy(searchBarColor = Color.Black),
        strings = CircleSearchStrings.defaults().copy(searchTitle = "Find"),
    ),
) { /* ... */ }
```

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
