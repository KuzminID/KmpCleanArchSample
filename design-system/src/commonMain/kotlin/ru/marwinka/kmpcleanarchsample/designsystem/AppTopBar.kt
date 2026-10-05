package ru.marwinka.kmpcleanarchsample.designsystem

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Верхняя панель экрана. `TopAppBar` в Material 3 экспериментальный, поэтому opt-in живёт
 * только здесь: при изменении API правка нужна в одном месте, а не в каждой фиче.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
    )
}
