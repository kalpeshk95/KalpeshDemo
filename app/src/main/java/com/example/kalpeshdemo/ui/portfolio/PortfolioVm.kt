package com.example.kalpeshdemo.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kalpeshdemo.data.model.HoldingData
import com.example.kalpeshdemo.data.model.UserHoldingItem
import com.example.kalpeshdemo.data.model.toHoldingData
import com.example.kalpeshdemo.data.repository.NetworkRepository
import com.example.kalpeshdemo.data.source.Resource
import com.example.kalpeshdemo.utils.NetworkStatusHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class PortfolioVm(
    private val networkRepository: NetworkRepository,
    private val networkStatusHelper: NetworkStatusHelper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _portfolioState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
    val portfolioState: StateFlow<PortfolioUiState> = _portfolioState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData(forceRefresh = true)
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (forceRefresh && !networkStatusHelper.isNetworkAvailable.value) {
                    _portfolioState.value = PortfolioUiState.Error(
                        isOffline = true,
                        message = "No internet connection"
                    )
                    return@launch
                }

                networkRepository.fetchHoldingData(forceRefresh)
                    .flowOn(dispatcher)
                    .catch { handleError() }
                    .collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                _portfolioState.value = PortfolioUiState.Loading
                            }

                            is Resource.Success -> {
                                resource.data?.let { updateUiState(it) }
                                    ?: handleError("No data available")
                            }

                            is Resource.Error -> handleError()
                        }
                    }
            } catch (_: Exception) {
                handleError()
            }
        }
    }

    private fun handleError(error: String = "Failed to load data. Please try again.") {
        val currentState = _portfolioState.value
        _portfolioState.value = when (currentState) {
            is PortfolioUiState.Success -> currentState.copy(
                isOffline = !networkStatusHelper.isNetworkAvailable.value
            )

            else -> PortfolioUiState.Error(
                message = error,
                isOffline = !networkStatusHelper.isNetworkAvailable.value
            )
        }
    }

    private fun updateUiState(holdings: List<UserHoldingItem>) {
        val holdingData = holdings.toHoldingData()
        val currentVal = holdingData.sumOf { it.ltp * it.quantity }
        val totalInv = holdingData.sumOf { it.avgPrice * it.quantity }
        val todayPnL = holdingData.sumOf { it.pnl }
        val profitLoss = currentVal - totalInv
        val profitLossPercent = if (totalInv > 0) {
            (profitLoss / totalInv * 100).absoluteValue
        } else 0.0

        _portfolioState.value = PortfolioUiState.Success(
            holdingList = holdingData,
            currentVal = currentVal,
            totalInv = totalInv,
            todayPnL = todayPnL,
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            isOffline = !networkStatusHelper.isNetworkAvailable.value
        )
    }

    public override fun onCleared() {
        super.onCleared()
        networkStatusHelper.unregister()
    }
}

sealed interface PortfolioUiState {
    object Loading : PortfolioUiState
    data class Success(
        val holdingList: List<HoldingData>,
        val currentVal: Double,
        val totalInv: Double,
        val todayPnL: Double,
        val profitLoss: Double,
        val profitLossPercent: Double,
        val isOffline: Boolean = false
    ) : PortfolioUiState

    data class Error(
        val message: String,
        val isOffline: Boolean = false
    ) : PortfolioUiState
}