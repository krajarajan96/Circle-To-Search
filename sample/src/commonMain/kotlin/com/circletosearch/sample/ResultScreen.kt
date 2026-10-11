package com.circletosearch.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.circletosearch.CircleSearchResult

/** Shows whichever [CircleSearchResult] the user chose, proving both query types work end to end. */
@Composable
fun ResultScreen(result: CircleSearchResult, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Search result", style = MaterialTheme.typography.h4)

            when (result) {
                is CircleSearchResult.TextQuery -> {
                    Text("Recognized text:", style = MaterialTheme.typography.subtitle1)
                    Text(result.text.ifBlank { "(no text recognized)" })
                    Image(bitmap = result.image, contentDescription = null, modifier = Modifier.size(120.dp))
                }

                is CircleSearchResult.ImageQuery -> {
                    Text("Selected image:", style = MaterialTheme.typography.subtitle1)
                    Image(bitmap = result.image, contentDescription = null, modifier = Modifier.size(200.dp))
                }
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
