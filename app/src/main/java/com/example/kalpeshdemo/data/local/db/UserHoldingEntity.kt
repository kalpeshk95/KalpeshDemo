package com.example.kalpeshdemo.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalpeshdemo.data.model.UserHoldingItem

@Entity(tableName = "user_holdings")
data class UserHoldingEntity(
    @PrimaryKey
    val symbol: String,
    val quantity: Int,
    val avgPrice: Double,
    val ltp: Double,
    val close: Double
) {
    fun toUserHoldingItem() = UserHoldingItem(
        symbol = symbol,
        quantity = quantity,
        avgPrice = avgPrice,
        ltp = ltp,
        close = close
    )
}
