package com.example.kalpeshdemo.di

import com.example.kalpeshdemo.ui.portfolio.PortfolioVm
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        PortfolioVm(
            networkRepository = get(),
            networkStatusHelper = get(),
            dispatcher = get()
        )
    }
}
