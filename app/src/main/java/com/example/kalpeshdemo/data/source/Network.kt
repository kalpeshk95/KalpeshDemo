package com.example.kalpeshdemo.data.source

import com.example.kalpeshdemo.data.model.PortfolioResponse
import retrofit2.http.GET

interface Network {

    @GET("/")
    suspend fun fetchHoldingData(): PortfolioResponse
}