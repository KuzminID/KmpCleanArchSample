package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.Res
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.task_details_back
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.task_details_title

@Composable
internal fun TaskDetailsScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.task_details_title)) },
                navigationIcon = {
                    // TextButton, not IconButton: IconButton is a fixed 48dp square sized for an icon
                    // and wraps a word like "Назад" onto two lines.
                    TextButton(onClick = onBack) {
                        Text(stringResource(Res.string.task_details_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(title)
        }
    }
}

@Preview
@Composable
private fun TaskDetailsScreenPreview() {
    TaskDetailsScreen(title = "Write the architecture doc", onBack = {})
}
