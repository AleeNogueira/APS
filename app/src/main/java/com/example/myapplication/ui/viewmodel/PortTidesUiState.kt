package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.TideTable

data class PortTidesUiState(
    val tideTables: List<TideTable> = emptyList(),
    val activeTable: TideTable? = null,
    val selectedTable: TideTable? = null,
    val isLoading: Boolean = false,
    val searchDayQuery: String = "",
    val filterStatus: String? = null, // "LIBERADO" ou "BLOQUEADO"
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val currentDisplayTable: TideTable? get() = selectedTable ?: activeTable
    val totalMeasurementsCount: Int get() = currentDisplayTable?.measurements?.size ?: 0

    val criticalDaysCount: Int
        get() {
            val table = currentDisplayTable ?: return 0
            return (1..30).count { day -> table.isCriticalLowTideDay(day) }
        }

    val averageHeight: Double
        get() {
            val measurements = currentDisplayTable?.measurements ?: return 0.0
            return if (measurements.isEmpty()) 0.0 else measurements.map { it.height }.average()
        }
}
