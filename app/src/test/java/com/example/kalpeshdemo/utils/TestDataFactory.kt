package com.example.kalpeshdemo.utils

import com.example.kalpeshdemo.data.local.db.UserHoldingEntity
import com.example.kalpeshdemo.data.model.Data
import com.example.kalpeshdemo.data.model.HoldingData
import com.example.kalpeshdemo.data.model.PortfolioResponse
import com.example.kalpeshdemo.data.model.UserHoldingItem
import com.example.kalpeshdemo.data.source.Resource
import kotlin.random.Random

object TestDataFactory {

    // Generate a random string of given length
    private fun randomString(length: Int = 5): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // Generate a random double between min and max
    private fun randomDouble(min: Double = 0.0, max: Double = 10000.0): Double {
        return min + (max - min) * Random.nextDouble()
    }

    // Generate a random integer between min and max
    private fun randomInt(min: Int = 1, max: Int = 100): Int {
        return (min..max).random()
    }

    // Create a test UserHoldingItem
    fun createUserHoldingItem(
        symbol: String = "SYM${randomString(3)}",
        quantity: Int = randomInt(1, 1000),
        avgPrice: Double = randomDouble(10.0, 2000.0),
        ltp: Double = randomDouble(10.0, 2000.0),
        close: Double = randomDouble(10.0, 2000.0)
    ): UserHoldingItem {
        return UserHoldingItem(
            symbol = symbol,
            quantity = quantity,
            avgPrice = avgPrice,
            ltp = ltp,
            close = close
        )
    }

    // Create a list of test UserHoldingItem
    fun createUserHoldingItems(count: Int = 5): List<UserHoldingItem> {
        return List(count) { createUserHoldingItem() }
    }

    // Create a test UserHoldingEntity
    fun createUserHoldingEntity(
        symbol: String = "SYM${randomString(3)}",
        quantity: Int = randomInt(1, 1000),
        avgPrice: Double = randomDouble(10.0, 2000.0),
        ltp: Double = randomDouble(10.0, 2000.0),
        close: Double = randomDouble(10.0, 2000.0)
    ): UserHoldingEntity {
        return UserHoldingEntity(
            symbol = symbol,
            quantity = quantity,
            avgPrice = avgPrice,
            ltp = ltp,
            close = close
        )
    }

    // Create a test HoldingData
    fun createHoldingData(
        symbol: String = "SYM${randomString(3)}",
        quantity: Int = randomInt(1, 1000),
        avgPrice: Double = randomDouble(10.0, 2000.0),
        ltp: Double = randomDouble(10.0, 2000.0)
    ): HoldingData {
        return HoldingData(
            symbol = symbol,
            quantity = quantity,
            avgPrice = avgPrice,
            ltp = ltp,
            pnl = (ltp - (avgPrice * quantity)) * quantity
        )
    }

    // Create a test PortfolioResponse
    fun createPortfolioResponse(
        holdings: List<UserHoldingItem> = createUserHoldingItems(3)
    ): PortfolioResponse {
        val data = Data(userHolding = holdings)
        return PortfolioResponse(data = data)
    }

    // Create a test Resource.Success with UserHoldingItems
    fun createSuccessResource(
        data: List<UserHoldingItem> = createUserHoldingItems(3)
    ): Resource<List<UserHoldingItem>> {
        return Resource.Success(data)
    }

    // Create a test Resource.Error
    fun createErrorResource(
        error: Throwable = Exception("Test error")
    ): Resource<Nothing> {
        return Resource.Error(error)
    }

    // Create a test Resource.Loading
    fun <T> createLoadingResource(): Resource<T> {
        return Resource.Loading
    }
}
