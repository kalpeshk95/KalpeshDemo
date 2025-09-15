package com.example.kalpeshdemo.data.model


import kotlinx.serialization.Serializable

@Serializable
data class PortfolioResponse(
//    @SerialName("data")
    val data: Data? = null
)

@Serializable
data class UserHoldingItem(

//    @SerialName("symbol")
    val symbol: String? = null,

//    @SerialName("quantity")
    val quantity: Int? = null,

//    @SerialName("avgPrice")
    val avgPrice: Double? = null,

//    @SerialName("ltp")
    val ltp: Double? = null,

//    @SerialName("close")
    val close: Double? = null
)

@Serializable
data class Data(

//    @SerialName("userHolding")
    val userHolding: List<UserHoldingItem>? = null
)
