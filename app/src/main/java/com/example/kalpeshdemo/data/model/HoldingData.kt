package com.example.kalpeshdemo.data.model

data class HoldingData(
    val symbol: String,
    val quantity: Int,
    val avgPrice: Double,
    val ltp: Double,
    val pnl: Double
)

fun List<UserHoldingItem>.toHoldingData(): List<HoldingData> {
    return this.map {
        HoldingData(
            symbol = it.symbol ?: "",
            quantity = it.quantity ?: 0,
            avgPrice = it.avgPrice ?: 0.0,
            ltp = it.ltp ?: 0.0,
            pnl = getPnL(it)
        )
    }
}

fun getPnL(item: UserHoldingItem): Double {
    val closePrice = item.close ?: 0.0
    val ltp = item.ltp ?: 0.0
    val quantity = item.quantity ?: 0
    return (closePrice.minus(ltp)) * quantity
}
