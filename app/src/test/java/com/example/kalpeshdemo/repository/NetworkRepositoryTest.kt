package com.example.kalpeshdemo.repository

import com.example.kalpeshdemo.data.local.LocalDataSource
import com.example.kalpeshdemo.data.model.Data
import com.example.kalpeshdemo.data.model.PortfolioResponse
import com.example.kalpeshdemo.data.model.UserHoldingItem
import com.example.kalpeshdemo.data.repository.NetworkRepository
import com.example.kalpeshdemo.data.source.Network
import com.example.kalpeshdemo.data.source.Resource
import com.example.kalpeshdemo.utils.MainDispatcherRule
import com.example.kalpeshdemo.utils.TestDataFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockNetwork: Network

    @MockK
    private lateinit var mockLocalDataSource: LocalDataSource

    private lateinit var repository: NetworkRepository

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
        repository = NetworkRepository(mockNetwork, mockLocalDataSource)
    }

    @Test
    fun `fetchHoldingData should emit loading first when no cache`() = runTest {
        // Given
        coEvery { mockLocalDataSource.getUserHoldings() } returns flowOf(emptyList())
        coEvery { mockNetwork.fetchHoldingData() } returns TestDataFactory.createPortfolioResponse(
            testHoldings
        )
        coEvery { mockLocalDataSource.saveUserHoldings(any()) } just Runs

        // When
        val results = mutableListOf<Resource<*>>()
        repository.fetchHoldingData().collect { results.add(it) }

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results[0] is Resource.Loading)
    }

    @Test
    fun `fetchHoldingData should return cached data first when available`() = runTest {
        // Given
        val cachedHoldings = testHoldings.map { it.toEntity() }
        coEvery { mockLocalDataSource.getUserHoldings() } returns flowOf(cachedHoldings)
        coEvery { mockNetwork.fetchHoldingData() } returns TestDataFactory.createPortfolioResponse(
            testHoldings
        )
        coEvery { mockLocalDataSource.saveUserHoldings(any()) } just Runs

        // When
        val results = mutableListOf<Resource<*>>()
        val job = launch {
            repository.fetchHoldingData().collect {
                results.add(it)
                // Cancel after we've collected what we expect
                if (results.size >= 2) {
                    cancel()
                }
            }
        }

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        assertTrue("Expected at least 2 emissions (Loading and Success)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is Resource.Loading)
        assertTrue("Second emission should be Success", results[1] is Resource.Success<*>)
        val successResult = results[1] as Resource.Success<*>
        val data = successResult.data as? List<*>
        assertNotNull("Data should not be null", data)
        assertEquals("Data size should match test holdings", testHoldings.size, data?.size ?: 0)

        // Cleanup
        if (job.isActive) job.cancel()
    }

    @Test
    fun `fetchHoldingData should fetch from network and save to local`() = runTest {
        // Given
        coEvery { mockLocalDataSource.getUserHoldings() } returns flowOf(emptyList())
        coEvery { mockNetwork.fetchHoldingData() } returns TestDataFactory.createPortfolioResponse(
            testHoldings
        )
        coEvery { mockLocalDataSource.saveUserHoldings(any()) } just Runs

        // When
        val results = mutableListOf<Resource<*>>()
        val job = launch {
            repository.fetchHoldingData().collect {
                results.add(it)
                // Cancel after we've collected what we expect
                if (results.size >= 2) {
                    cancel()
                }
            }
        }

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        coVerify(exactly = 1) { mockLocalDataSource.saveUserHoldings(any()) }
        assertTrue("Expected at least 2 emissions (Loading and Success)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is Resource.Loading)
        assertTrue("Second emission should be Success", results[1] is Resource.Success<*>)

        val successResult = results[1] as Resource.Success<*>
        val data = successResult.data as? List<*>
        assertNotNull("Data should not be null", data)
        assertEquals("Data size should match test holdings", testHoldings.size, data?.size ?: 0)

        // Cleanup
        if (job.isActive) job.cancel()
    }

    @Test
    fun `fetchHoldingData should return error when network fails and no cache`() = runTest {
        // Given
        val error = IOException("Network error")
        coEvery { mockLocalDataSource.getUserHoldings() } returns flowOf(emptyList())
        coEvery { mockNetwork.fetchHoldingData() } throws error

        // When
        val results = mutableListOf<Resource<*>>()
        val job = launch {
            repository.fetchHoldingData().collect {
                results.add(it)
                // Cancel after we've collected what we expect
                if (results.size >= 2) {
                    cancel()
                }
            }
        }

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        assertTrue("Expected at least 2 emissions (Loading and Error)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is Resource.Loading)
        assertTrue("Second emission should be Error", results[1] is Resource.Error)

        val errorResult = results[1] as Resource.Error
        assertEquals("Exception should match", error, errorResult.exception)

        // Cleanup
        if (job.isActive) job.cancel()
    }

    @Test
    fun `forceRefresh should skip cache and fetch from network`() = runTest {
        // Given
        val responseData = Data(userHolding = testHoldings)
        coEvery { mockNetwork.fetchHoldingData() } returns PortfolioResponse(data = responseData)
        coEvery { mockLocalDataSource.saveUserHoldings(any()) } just Runs

        // When
        val results = mutableListOf<Resource<*>>()
        val job = launch {
            repository.fetchHoldingData(forceRefresh = true).collect {
                results.add(it)
                // Cancel after we've collected what we expect
                if (results.size >= 2) {
                    cancel()
                }
            }
        }

        // Need to wait for the flow to emit values
        while (results.size < 2 && !job.isCompleted) {
            advanceTimeBy(100)
        }

        // Then
        coVerify(exactly = 1) { mockNetwork.fetchHoldingData() }
        coVerify(exactly = 1) { mockLocalDataSource.saveUserHoldings(any()) }

        assertTrue("Expected at least 2 emissions (Loading and Success)", results.size >= 2)
        assertTrue("First emission should be Loading", results[0] is Resource.Loading)
        assertTrue("Second emission should be Success", results[1] is Resource.Success<*>)

        val successResult = results[1] as Resource.Success<*>
        val data = successResult.data as? List<*>
        assertNotNull("Data should not be null", data)
        assertEquals("Data size should match test holdings", testHoldings.size, data?.size ?: 0)

        // Cleanup
        if (job.isActive) job.cancel()
    }

    private fun UserHoldingItem.toEntity() = TestDataFactory.createUserHoldingEntity(
        symbol = this.symbol ?: "",
        quantity = quantity ?: 0,
        avgPrice = avgPrice ?: 0.0,
        ltp = ltp ?: 0.0,
        close = close ?: 0.0
    )
}
