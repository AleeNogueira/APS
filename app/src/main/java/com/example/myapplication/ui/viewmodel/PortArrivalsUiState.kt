package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.Berth
import com.example.myapplication.data.model.BerthArrivalItem
import com.example.myapplication.data.model.MooringStatus

data class PortArrivalsUiState(
    val arrivalsList: List<BerthArrivalItem> = emptyList(),
    val filteredArrivals: List<BerthArrivalItem> = emptyList(),
    val allBerths: List<Berth> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: MooringStatus? = null,
    val selectedSector: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddOrEditDialog: Boolean = false,
    val itemToEdit: BerthArrivalItem? = null
) {
    val totalArrivalsCount: Int get() = arrivalsList.size
    val operatingCount: Int get() = arrivalsList.count { it.schedule.status == MooringStatus.OPERATING }
    val scheduledCount: Int get() = arrivalsList.count { it.schedule.status == MooringStatus.SCHEDULED }
    val confirmedCount: Int get() = arrivalsList.count { it.schedule.status == MooringStatus.CONFIRMED }
    val totalTonnageSum: Double get() = arrivalsList.sumOf { it.schedule.tonnage }
}
