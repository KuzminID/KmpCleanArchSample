package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTaskHistoryRepository : TaskHistoryRepository {
    val history = MutableSharedFlow<List<TaskHistoryEntry>>()

    override fun observeHistory(): Flow<List<TaskHistoryEntry>> = history
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val entry = TaskHistoryEntry(id = "1", title = "S01E01 · Pilot", completedAtEpochMillis = 0)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loading_lasts_until_the_first_emission_from_storage() =
        runTest {
            val repository = FakeTaskHistoryRepository()
            val model = HistoryViewModel(GetTaskHistoryUseCase(repository))
            val states = mutableListOf<HistoryUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect { states += it } }
            runCurrent()

            assertEquals(HistoryUiState(), states.last())

            repository.history.emit(emptyList())
            assertEquals(HistoryUiState(entries = emptyList(), isLoading = false), states.last())

            repository.history.emit(listOf(entry))
            assertEquals(HistoryUiState(entries = listOf(entry), isLoading = false), states.last())
        }
}
