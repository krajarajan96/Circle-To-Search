package com.circletosearch.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.circletosearch.CircleSearchResult

private sealed interface Screen {
    data object Demo : Screen
    data class Result(val result: CircleSearchResult) : Screen
}

@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<Screen>(Screen.Demo) }

        when (val current = screen) {
            is Screen.Demo -> DemoContentScreen(
                onResult = { result -> screen = Screen.Result(result) },
            )

            is Screen.Result -> ResultScreen(
                result = current.result,
                onBack = { screen = Screen.Demo },
            )
        }
    }
}
