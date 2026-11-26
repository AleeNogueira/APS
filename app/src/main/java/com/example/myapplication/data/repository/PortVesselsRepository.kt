package com.example.myapplication.data.repository

import com.example.myapplication.data.model.RegisteredVessel
import com.example.myapplication.data.remote.BackendApiClient

class PortVesselsRepository(
    private val apiClient: BackendApiClient = BackendApiClient
) {
    suspend fun getRegisteredVessels(): Result<List<RegisteredVessel>> {
        return try {
            val list = apiClient.fetchRegisteredVessels()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRegisteredVessel(vessel: RegisteredVessel): Result<Unit> {
        return try {
            apiClient.addRegisteredVessel(vessel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRegisteredVessel(vessel: RegisteredVessel): Result<Unit> {
        return try {
            apiClient.updateRegisteredVessel(vessel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRegisteredVessel(imoCode: String): Result<Unit> {
        return try {
            apiClient.deleteRegisteredVessel(imoCode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
