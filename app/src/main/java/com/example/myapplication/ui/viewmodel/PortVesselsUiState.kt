package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.RegisteredVessel

data class PortVesselsUiState(
    val vesselsList: List<RegisteredVessel> = emptyList(),
    val filteredVessels: List<RegisteredVessel> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedVesselClass: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddOrEditDialog: Boolean = false,
    val vesselToEdit: RegisteredVessel? = null
) {
    val totalFleetCount: Int get() = vesselsList.size
    val averageDraft: Double get() = if (vesselsList.isEmpty()) 0.0 else vesselsList.map { it.maxDraft }.average()
    val totalTonnageSum: Double get() = vesselsList.sumOf { it.standardTonnage }
    val distinctClassesCount: Int get() = vesselsList.map { it.vesselClass }.distinct().size
}
