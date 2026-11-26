package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.RegisteredVessel
import com.example.myapplication.data.repository.PortVesselsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortVesselsViewModel(
    private val repository: PortVesselsRepository = PortVesselsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortVesselsUiState())
    val uiState: StateFlow<PortVesselsUiState> = _uiState.asStateFlow()

    init {
        loadVessels()
    }

    fun loadVessels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getRegisteredVessels()
            result.fold(
                onSuccess = { list ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            vesselsList = list,
                            isLoading = false
                        )
                    }
                    applyFilters()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao carregar frota do servidor."
                        )
                    }
                }
            )
        }
    }

    private fun applyFilters() {
        val all = _uiState.value.vesselsList
        val query = _uiState.value.searchQuery
        val selectedClass = _uiState.value.selectedVesselClass

        val filtered = all.filter { vessel ->
            val matchesQuery = query.isBlank() ||
                    vessel.imoCode.contains(query, ignoreCase = true) ||
                    vessel.shipName.contains(query, ignoreCase = true) ||
                    vessel.vesselClass.contains(query, ignoreCase = true) ||
                    vessel.typicalImportCargo.contains(query, ignoreCase = true) ||
                    vessel.typicalExportCargo.contains(query, ignoreCase = true)

            val matchesClass = selectedClass == null || vessel.vesselClass.equals(selectedClass, ignoreCase = true)

            matchesQuery && matchesClass
        }

        _uiState.update { it.copy(filteredVessels = filtered) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        applyFilters()
    }

    fun onClassFilterSelected(vesselClass: String?) {
        val newClass = if (_uiState.value.selectedVesselClass == vesselClass) null else vesselClass
        _uiState.update { it.copy(selectedVesselClass = newClass) }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedVesselClass = null) }
        applyFilters()
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = true,
                vesselToEdit = null,
                errorMessage = null
            )
        }
    }

    fun openEditDialog(vessel: RegisteredVessel) {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = true,
                vesselToEdit = vessel,
                errorMessage = null
            )
        }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = false,
                vesselToEdit = null,
                errorMessage = null
            )
        }
    }

    fun saveVessel(
        imoInput: String,
        shipName: String,
        vesselClass: String,
        draftStr: String,
        tonnageStr: String,
        importCargo: String,
        exportCargo: String
    ) {
        val rawImo = imoInput.trim().uppercase()
        val formattedImo = if (rawImo.startsWith("IMO-")) rawImo else "IMO-$rawImo"

        val draft = draftStr.toDoubleOrNull()
        val tonnage = tonnageStr.toDoubleOrNull()

        if (!formattedImo.matches(Regex("IMO-\\d{7}"))) {
            _uiState.update { it.copy(errorMessage = "RN007: Código IMO inválido! Deve seguir o formato IMO-xxxxxxx (7 dígitos).") }
            return
        }

        if (shipName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe o nome do navio.") }
            return
        }

        if (draft == null || draft <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe um calado máximo válido (> 0).") }
            return
        }

        if (tonnage == null || tonnage <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe uma tonelagem padrão válida (> 0).") }
            return
        }

        val vesselObj = RegisteredVessel(
            imoCode = formattedImo,
            shipName = shipName.trim(),
            vesselClass = vesselClass.trim(),
            maxDraft = draft,
            standardTonnage = tonnage,
            typicalImportCargo = importCargo.trim(),
            typicalExportCargo = exportCargo.trim()
        )

        val isEditMode = _uiState.value.vesselToEdit != null

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (isEditMode) {
                repository.updateRegisteredVessel(vesselObj)
            } else {
                repository.addRegisteredVessel(vesselObj)
            }

            result.fold(
                onSuccess = {
                    val msg = if (isEditMode) {
                        "Navio $formattedImo atualizado com sucesso via REST PUT!"
                    } else {
                        "Navio $formattedImo cadastrado com sucesso via REST POST!"
                    }
                    _uiState.update {
                        it.copy(
                            showAddOrEditDialog = false,
                            vesselToEdit = null,
                            isLoading = false,
                            successMessage = msg
                        )
                    }
                    loadVessels()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao salvar navio no servidor."
                        )
                    }
                }
            )
        }
    }

    fun deleteVessel(imoCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.deleteRegisteredVessel(imoCode)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Navio IMO $imoCode removido do servidor via REST DELETE!"
                        )
                    }
                    loadVessels()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao remover navio do servidor."
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
