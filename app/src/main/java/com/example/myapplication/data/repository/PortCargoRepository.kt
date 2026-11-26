package com.example.myapplication.data.repository

import com.example.myapplication.data.model.PortCargoCatalogItem
import com.example.myapplication.data.remote.BackendApiClient

class PortCargoRepository(
    private val apiClient: BackendApiClient = BackendApiClient
) {
    suspend fun getPortCargos(): Result<List<PortCargoCatalogItem>> {
        return try {
            val list = apiClient.fetchPortCargos()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPortCargo(item: PortCargoCatalogItem): Result<Unit> {
        return try {
            apiClient.addPortCargo(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePortCargo(productCode: String): Result<Unit> {
        return try {
            apiClient.deletePortCargo(productCode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
