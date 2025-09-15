package com.example.kalpeshdemo.app

import android.app.Application
import com.example.kalpeshdemo.BuildConfig
import com.example.kalpeshdemo.di.appModule
import com.example.kalpeshdemo.di.databaseModule
import com.example.kalpeshdemo.di.repoModule
import com.example.kalpeshdemo.di.retrofitModule
import com.example.kalpeshdemo.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupDI()
    }

    private fun setupDI() {
        startKoin {

            if (BuildConfig.DEBUG) {
                androidLogger(Level.ERROR)
            }

            androidContext(this@MyApplication)

            modules(
                listOf(
                    appModule,
                    repoModule,
                    databaseModule,
                    retrofitModule,
                    viewModelModule
                )
            )
        }
    }
}