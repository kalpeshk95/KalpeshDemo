package com.example.kalpeshdemo.data.repository

import com.example.kalpeshdemo.data.local.LocalDataSource
import com.example.kalpeshdemo.data.local.db.UserHoldingEntity
import com.example.kalpeshdemo.data.model.UserHoldingItem
import com.example.kalpeshdemo.data.source.Network
import com.example.kalpeshdemo.data.source.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

interface NetworkManager {
    fun fetchHoldingData(forceRefresh: Boolean = false): Flow<Resource<List<UserHoldingItem>?>>
}

class NetworkRepository(
    private val network: Network,
    private val localDataSource: LocalDataSource
) : NetworkManager {

    override fun fetchHoldingData(forceRefresh: Boolean): Flow<Resource<List<UserHoldingItem>?>> = flow {
        emit(Resource.Loading)

        // Try to load from cache first if not forcing refresh
        if (!forceRefresh) {
            try {
                val cachedHoldings = localDataSource.getUserHoldings().first()
                if (cachedHoldings.isNotEmpty()) {
                    emit(Resource.Success(cachedHoldings.map { it.toUserHoldingItem() }))
                }
            } catch (_: Exception) {
            }
        }

        try {
            // Fetch from network
            val response = network.fetchHoldingData()
            val holdings = response.data?.userHolding

            if (holdings != null) {
                // Save to local database
                localDataSource.saveUserHoldings(holdings.map { it.toEntity() })
                emit(Resource.Success(holdings))
            } else {
                emit(Resource.Error(IOException("No data received")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    fun UserHoldingItem.toEntity(): UserHoldingEntity {
        return UserHoldingEntity(
            symbol = this.symbol ?: "",
            quantity = quantity ?: 0,
            avgPrice = avgPrice ?: 0.0,
            ltp = ltp ?: 0.0,
            close = close ?: 0.0
        )
    }
}
