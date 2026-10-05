package ru.marwinka.kmpcleanarchsample

import androidx.compose.ui.window.ComposeUIViewController

// Имя вызывается из Swift (MainViewControllerKt.MainViewController) и следует конвенции Compose.
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { App() }
