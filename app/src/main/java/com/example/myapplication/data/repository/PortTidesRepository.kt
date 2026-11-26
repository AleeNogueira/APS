package com.example.myapplication.data.repository

import com.example.myapplication.data.model.TideTable
import com.example.myapplication.data.remote.BackendApiClient

class PortTidesRepository(
    private val apiClient: BackendApiClient = BackendApiClient
) {
    suspend fun getTideTables(): Result<List<TideTable>> {
        return try {
            val list = apiClient.fetchTideTables()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateTideTable(tableId: Long): Result<TideTable> {
        return try {
            val activated = apiClient.activateTideTable(tableId)
            Result.success(activated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
