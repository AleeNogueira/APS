package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.Berth
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.MooringStatus

data class LineupUiState(
    val allBerths: List<Berth> = emptyList(),
    val filteredBerths: List<Berth> = emptyList(),
    val paginatedBerths: List<Berth> = emptyList(),
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5,
    val totalPages: Int = 12,
    val searchQuery: String = "",
    val selectedCategory: CargoCategory? = null,
    val selectedStatus: MooringStatus? = null,
    val selectedSector: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val selectedBerthForAdd: Berth? = null
) {
    val totalBerthsCount: Int get() = allBerths.size
    val activeShipsCount: Int get() = allBerths.sumOf { it.schedules.size }
    val totalTonnageSum: Double get() = allBerths.sumOf { berth -> berth.schedules.sumOf { it.tonnage } }
    val operatingShipsCount: Int get() = allBerths.sumOf { berth -> berth.schedules.count { it.status == MooringStatus.OPERATING } }
}
