package com.circletosearch.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.circletosearch.CircleSearchResult
import com.circletosearch.SelectableContent

/**
 * Static content proving [SelectableContent] is droppable onto a screen without any
 * coupling to that screen's own logic.
 */
@Composable
fun DemoContentScreen(onResult: (CircleSearchResult) -> Unit) {
    SelectableContent(onResult = onResult) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().safeContentPadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Circle To Search", style = MaterialTheme.typography.headlineMedium)
                Text("Circle any part of this screen, then search its text or image.")

                Card(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Open hours: Mon-Fri 9am-6pm, Sat 10am-4pm, closed Sundays.",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Card(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Return policy: items may be returned within 30 days with a receipt.",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Card(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Contact: support@example.com or (555) 010-1234.",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
