package com.example.kalpeshdemo.portfolio

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kalpeshdemo.data.repository.NetworkRepository
import com.example.kalpeshdemo.data.source.Resource
import com.example.kalpeshdemo.ui.portfolio.PortfolioUiState
import com.example.kalpeshdemo.ui.portfolio.PortfolioVm
import com.example.kalpeshdemo.utils.MainDispatcherRule
import com.example.kalpeshdemo.utils.NetworkStatusHelper
import com.example.kalpeshdemo.utils.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioVmTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockRepository: NetworkRepository

    @MockK
    private lateinit var mockNetworkStatusHelper: NetworkStatusHelper

    private lateinit var viewModel: PortfolioVm

    private val testHoldings = TestDataFactory.createUserHoldingItems(2).mapIndexed { index, item ->
        when (index) {
            0 -> item.copy(
                symbol = "TCS",
                quantity = 10,
                avgPrice = 2000.0,
                ltp = 2200.0,
                close = 2100.0
            )

            else -> item.copy(
                symbol = "INFY",
                quantity = 5,
                avgPrice = 1500.0,
                ltp = 1600.0,
                close = 1550.0
            )
        }
    }

    @Before
    fun setup() {
        every { mockNetworkStatusHelper.isNetworkAvailable } returns MutableStateFlow(true)
        viewModel = PortfolioVm(mockRepository, mockNetworkStatusHelper)
    }

    @Test
    fun `loadData should update state to Success when data is fetched successfully`() = runTest {
        // Given
        coEvery { mockRepository.fetchHoldingData(any()) } returns flow {
            emit(Resource.Loading)
            emit(Resource.Success(testHoldings))
        }

        // When
        val results = mutableListOf<PortfolioUiState>()
        val job = launch {
            viewModel.portfolioState.collect { results.add(it) }
        }

        // Trigger refresh
        viewModel.refresh()

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        assertTrue("Expected at least 2 emissions (Loading and Success)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is PortfolioUiState.Loading)

        val state = results[1] as? PortfolioUiState.Success
        assertNotNull("State should be Success", state)

        state?.let { s ->
            assertEquals("Should have 2 holdings", 2, s.holdingList.size)
            // Current value: (2200 * 10) + (1600 * 5) = 22000 + 8000 = 30000
            assertEquals(30000.0, s.currentVal, 0.01)
            // Total investment: (2000 * 10) + (1500 * 5) = 20000 + 7500 = 27500
            assertEquals(27500.0, s.totalInv, 0.01)
        }

        // Cleanup
        job.cancel()
    }

    @Test
    fun `loadData should update state to Error when network fails`() = runTest {
        // Given
        val errorMessage = "Failed to load data. Please try again."
        coEvery { mockRepository.fetchHoldingData(any()) } returns flow {
            emit(Resource.Loading)
            emit(TestDataFactory.createErrorResource(Exception(errorMessage)))
        }

        // When
        val results = mutableListOf<PortfolioUiState>()
        val job = launch {
            viewModel.portfolioState.collect { results.add(it) }
        }

        // Trigger refresh
        viewModel.refresh()

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        assertTrue("Expected at least 2 emissions (Loading and Error)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is PortfolioUiState.Loading)

        val state = results[1] as? PortfolioUiState.Error
        assertNotNull("State should be Error", state)
        assertEquals("Error message should match", errorMessage, state?.message)

        // Cleanup
        job.cancel()
    }

    @Test
    fun `should handle empty holdings list`() = runTest {
        // Given
        coEvery { mockRepository.fetchHoldingData(any()) } returns flow {
            emit(Resource.Loading)
            emit(TestDataFactory.createSuccessResource(emptyList()))
        }

        // When
        val results = mutableListOf<PortfolioUiState>()
        val job = launch {
            viewModel.portfolioState.collect { results.add(it) }
        }

        // Trigger refresh
        viewModel.refresh()

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        assertTrue("Expected at least 2 emissions (Loading and Success)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is PortfolioUiState.Loading)

        val state = results[1] as? PortfolioUiState.Success
        assertNotNull("State should be Success", state)

        state?.let { s ->
            assertTrue("Holding list should be empty", s.holdingList.isEmpty())
            assertEquals(0.0, s.currentVal, 0.01)
            assertEquals(0.0, s.totalInv, 0.01)
            assertEquals(0.0, s.todayPnL, 0.01)
            assertEquals(0.0, s.profitLoss, 0.01)
            assertEquals(0.0, s.profitLossPercent, 0.01)
        }

        // Cleanup
        job.cancel()
    }
}
