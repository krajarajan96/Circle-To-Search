package com.circletosearch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Wraps [content] with a Google-Lens-style "circle to search" gesture: a FAB toggles a
 * full-screen drag-to-circle overlay with a dimming scrim and animated top/bottom search chrome,
 * and on release the user is offered a text (OCR) or image search over whatever they circled.
 * Drop this around any screen's content; customize visuals and the OCR backend via [config].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectableContent(
    onResult: (CircleSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    config: CircleSearchConfig = CircleSearchConfig(),
    controller: SelectionController = rememberSelectionController(),
    content: @Composable () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()
    val mode = controller.mode

    // Retained across the exit animation so the result panel has data to render while sliding
    // away, even after `controller.reset()` has already flipped `mode` back to Idle.
    var lastResult by remember { mutableStateOf<SelectionMode?>(null) }
    if (mode is SelectionMode.Selected || mode is SelectionMode.RecognizingText) {
        lastResult = mode
    }

    val inSearchMode = mode !is SelectionMode.Idle
    val showBottomSearchBar = mode is SelectionMode.Selecting
    val showResultPanel = mode is SelectionMode.Selected || mode is SelectionMode.RecognizingText

    BackHandler(enabled = inSearchMode) { controller.reset() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it }
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                },
        ) {
            content()
        }

        if (inSearchMode) {
            Box(modifier = Modifier.fillMaxSize().background(config.colors.scrimBrush))
        }

        if (mode is SelectionMode.Selecting) {
            // Extra darkening while actively drawing so the selection stroke stands out.
            Box(modifier = Modifier.fillMaxSize().background(config.colors.selectingDarkenColor))
        }

        if (mode is SelectionMode.Selecting) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(controller) {
                        detectDragGestures(
                            onDrag = { change, _ -> controller.appendPoint(change.position) },
                            onDragEnd = {
                                val points = (controller.mode as? SelectionMode.Selecting)?.points.orEmpty()
                                val bounds = boundingRect(points)
                                    ?.clampTo(containerSize)
                                    ?.toIntRectOrNull(config.minSelectionSizePx)
                                if (bounds == null) {
                                    controller.reset()
                                } else {
                                    scope.launch {
                                        val snapshot = graphicsLayer.toImageBitmap().toSoftwareImageBitmap()
                                        val preview = cropImageBitmap(snapshot, bounds)
                                        controller.finishSelecting(bounds, preview)
                                    }
                                }
                            },
                        )
                    },
            ) {
                drawSelectionPath(mode.points, config.colors.selectionStrokeColor)
            }
        }

        if (mode is SelectionMode.Idle) {
            FloatingActionButton(
                onClick = { controller.startSelecting() },
                modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(24.dp),
            ) {
                Text("⌕")
            }
        }

        AnimatedVisibility(
            visible = showBottomSearchBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            SearchModeBottomBar(config)
        }

        AnimatedVisibility(
            visible = showResultPanel,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            when (val result = lastResult) {
                is SelectionMode.Selected -> SelectionResultPanel(
                    preview = result.preview,
                    isRecognizing = false,
                    config = config,
                    onSearchText = { controller.beginRecognizingText() },
                    onSearchImage = { onResult(CircleSearchResult.ImageQuery(result.preview)) },
                    onClose = { controller.reset() },
                )

                is SelectionMode.RecognizingText -> {
                    SelectionResultPanel(
                        preview = result.preview,
                        isRecognizing = true,
                        config = config,
                        onSearchText = {},
                        onSearchImage = {},
                        onClose = { controller.reset() },
                    )
                    LaunchedEffect(result.bounds) {
                        val text = config.ocrProvider.recognizeText(result.preview).getOrDefault("")
                        onResult(CircleSearchResult.TextQuery(text, result.preview))
                    }
                }

                else -> Unit
            }
        }

        // Composed last so it stacks above the full-screen drag-capture Canvas and always
        // receives taps on the close button.
        AnimatedVisibility(
            visible = inSearchMode,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        ) {
            SearchModeTopBar(config = config, onClose = { controller.reset() })
        }
    }
}

/** Thick, glowing stroke to mirror Google Lens's circle-to-search drawing style. */
private fun DrawScope.drawSelectionPath(points: List<Offset>, strokeColor: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    // Soft outer glow, then a crisp inner line, both rounded like Lens's drawn circle.
    drawPath(
        path,
        color = strokeColor.copy(alpha = 0.35f),
        style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    drawPath(
        path,
        color = strokeColor,
        style = Stroke(
            width = 7f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(16f),
        ),
    )
}

@Composable
private fun SearchModeTopBar(config: CircleSearchConfig, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(config.colors.topBarBrush)
            .safeDrawingPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp)
                .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            CloseIcon(tint = Color.White, modifier = Modifier.size(8.dp))
        }
        Text(
            config.strings.searchTitle,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun SearchModeBottomBar(config: CircleSearchConfig, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        shape = CircleShape,
        color = config.colors.searchBarColor,
        elevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                config.strings.bottomBarHint,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            MicIcon(tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

/** Drawn rather than an emoji, so it renders identically (and legibly) on both platforms. */
@Composable
private fun CloseIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.14f
        drawLine(
            color = tint,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

/** Drawn rather than an emoji, so it renders identically (and legibly) on both platforms. */
@Composable
private fun MicIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val capsuleWidth = size.width * 0.34f
        val capsuleHeight = size.height * 0.55f
        val capsuleLeft = (size.width - capsuleWidth) / 2f
        drawRoundRect(
            color = tint,
            topLeft = Offset(capsuleLeft, 0f),
            size = androidx.compose.ui.geometry.Size(capsuleWidth, capsuleHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(capsuleWidth / 2f, capsuleWidth / 2f),
        )
        val standTop = capsuleHeight * 0.75f
        val standWidth = size.width * 0.68f
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset((size.width - standWidth) / 2f, standTop - standWidth / 2f),
            size = androidx.compose.ui.geometry.Size(standWidth, standWidth),
            style = Stroke(width = size.width * 0.09f, cap = StrokeCap.Round),
        )
        val stemBottom = standTop + standWidth / 2f
        drawLine(
            color = tint,
            start = Offset(size.width / 2f, stemBottom),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = size.width * 0.09f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.25f, size.height),
            end = Offset(size.width * 0.75f, size.height),
            strokeWidth = size.width * 0.09f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SelectionResultPanel(
    preview: ImageBitmap,
    isRecognizing: Boolean,
    config: CircleSearchConfig,
    onSearchText: () -> Unit,
    onSearchImage: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        elevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(config.colors.searchBarColor, CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    CloseIcon(tint = Color.White, modifier = Modifier.size(7.dp))
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    bitmap = preview,
                    contentDescription = "Selected content",
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                )
                if (isRecognizing) {
                    Spacer(modifier = Modifier.size(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(config.strings.recognizingLabel)
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onSearchText,
                    enabled = !isRecognizing,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(config.strings.searchTextButton, maxLines = 1)
                }
                Button(
                    onClick = onSearchImage,
                    enabled = !isRecognizing,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(config.strings.searchImageButton, maxLines = 1)
                }
            }
        }
    }
}
