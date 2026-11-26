package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.PortCargoCatalogItem

data class PortCargoUiState(
    val cargosList: List<PortCargoCatalogItem> = emptyList(),
    val filteredCargos: List<PortCargoCatalogItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: CargoCategory? = null,
    val selectedOperation: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false
) {
    val totalCargosCount: Int get() = cargosList.size
    val totalCapacityTpd: Double get() = cargosList.sumOf { it.pranchaRateTpd }
    val categoriesCount: Int get() = cargosList.map { it.category }.distinct().size
}
