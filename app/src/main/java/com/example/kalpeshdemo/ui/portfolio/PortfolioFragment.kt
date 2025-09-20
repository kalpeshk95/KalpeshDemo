package com.example.kalpeshdemo.ui.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalpeshdemo.R
import com.example.kalpeshdemo.databinding.FragmentPortfolioBinding
import com.example.kalpeshdemo.utils.formatAsCurrency
import com.example.kalpeshdemo.utils.gone
import com.example.kalpeshdemo.utils.setTextColorRes
import com.example.kalpeshdemo.utils.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PortfolioFragment : Fragment(R.layout.fragment_portfolio) {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PortfolioVm by viewModel()
    private val holdingsAdapter by lazy {
        HoldingAdapter().apply {
            setHasStableIds(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClick()
        observer()
    }

    private fun initView() {
        with(binding) {
            // Setup RecyclerView
            rvHoldings.layoutManager = LinearLayoutManager(context)
            rvHoldings.adapter = holdingsAdapter

            // Setup SwipeRefreshLayout
            swipeRefreshLayout.setColorSchemeResources(
                R.color.purple_500,
                R.color.teal_700,
                R.color.green
            )
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.portfolioState.collectLatest { state ->
                        when (state) {
                            is PortfolioUiState.Loading -> showLoading()
                            is PortfolioUiState.Success -> showSuccess(state)
                            is PortfolioUiState.Error -> handleError(state)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        with(binding) {
            // Only show progress in the SwipeRefreshLayout if it's not already refreshing
            if (!swipeRefreshLayout.isRefreshing) {
                progressBar.visible()
                errorView.root.gone()
                rvHoldings.gone()
                viewGroup.gone()
            }
        }
    }

    private fun showSuccess(state: PortfolioUiState.Success) {
        try {
            holdingsAdapter.submitList(state.holdingList)

            val todayPnLColor = if (state.todayPnL >= 0) R.color.green else R.color.red
            val profitLossColor = if (state.profitLoss >= 0) R.color.green else R.color.red

            with(binding) {
                progressBar.gone()
                swipeRefreshLayout.isRefreshing = false
                errorView.root.gone()
                viewGroup.visible()
                rvHoldings.visible()
                // Update offline indicator
                if (state.isOffline) showOfflineIndicator() else hideOfflineIndicator()
                // Update bottom view
                with(bottomView) {
                    lblCurrentVal.text = requireContext().formatAsCurrency(state.currentVal)
                    lblTotalInv.text = requireContext().formatAsCurrency(state.totalInv)
                    lblTodayPnL.apply {
                        text = requireContext().formatAsCurrency(state.todayPnL)
                        setTextColorRes(todayPnLColor)
                    }
                    lblProfitLoss.apply {
                        text = requireContext().formatAsCurrency(state.profitLoss)
                        setTextColorRes(profitLossColor)
                    }
                    lblProfitLossPer.apply {
                        text = getString(R.string.lbl_percentage, state.profitLossPercent)
                        setTextColorRes(profitLossColor)
                    }
                }
            }
        } catch (_: Exception) {
            binding.progressBar.gone()
            showError("Failed to display data")
        }
    }

    private fun handleError(state: PortfolioUiState.Error) {
        with(binding) {
            progressBar.gone()
            swipeRefreshLayout.isRefreshing = false

            if (holdingsAdapter.itemCount == 0) {
                viewGroup.gone()
                rvHoldings.gone()
                errorView.root.visible()
                errorView.tvError.text = state.message
            }
        }
    }

    private fun showError(message: String) {
        with(binding.errorView) {
            root.visible()
            tvError.text = message
        }
    }

    private fun showOfflineIndicator() {
        binding.offlineIndicator.visible()
    }

    private fun hideOfflineIndicator() {
        binding.offlineIndicator.gone()
    }

    private fun initClick() {
        with(binding.bottomView) {
            txtProfitLoss.setOnClickListener {
                if (infoGroup.isGone) {
                    infoGroup.visible()
                    txtProfitLoss.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_down,
                        0
                    )
                } else {
                    infoGroup.gone()
                    txtProfitLoss.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_up,
                        0
                    )
                }
            }
        }

    }

    override fun onDestroy() {
        binding.rvHoldings.adapter = null
        _binding = null
        super.onDestroyView()
    }
}