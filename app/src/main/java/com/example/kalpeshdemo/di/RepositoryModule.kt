package com.example.kalpeshdemo.di

import com.example.kalpeshdemo.data.repository.NetworkRepository
import org.koin.dsl.module

val repoModule = module {
    single { NetworkRepository(get(), get()) }
}