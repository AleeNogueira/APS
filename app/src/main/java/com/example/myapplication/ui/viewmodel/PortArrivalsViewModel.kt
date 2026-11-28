package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.BerthArrivalItem
import com.example.myapplication.data.model.MooringStatus
import com.example.myapplication.data.model.ShipSchedule
import com.example.myapplication.data.repository.LineupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortArrivalsViewModel(
    private val repository: LineupRepository = LineupRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortArrivalsUiState())
    val uiState: StateFlow<PortArrivalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Sincronização Reativa Bidirecional (RN008): Coleta berthsFlow em tempo real
            repository.berthsFlow.collect { berths ->
                val flatArrivals = berths.flatMap { berth ->
                    berth.schedules.map { schedule ->
                        BerthArrivalItem(
                            berthId = berth.id,
                            berthName = berth.name,
                            sectorName = berth.sectorName,
                            maxDraft = berth.maxDraft,
                            schedule = schedule
                        )
                    }
                }.sortedBy { it.schedule.startDay }

                _uiState.update { currentState ->
                    currentState.copy(
                        arrivalsList = flatArrivals,
                        allBerths = berths
                    )
                }
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val all = _uiState.value.arrivalsList
        val query = _uiState.value.searchQuery
        val status = _uiState.value.selectedStatus
        val sector = _uiState.value.selectedSector

        val filtered = all.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.schedule.shipName.contains(query, ignoreCase = true) ||
                    item.schedule.cargoName.contains(query, ignoreCase = true) ||
                    item.berthName.contains(query, ignoreCase = true) ||
                    item.sectorName.contains(query, ignoreCase = true)

            val matchesStatus = status == null || item.schedule.status == status
            val matchesSector = sector == null || item.sectorName.equals(sector, ignoreCase = true)

            matchesQuery && matchesStatus && matchesSector
        }

        _uiState.update { it.copy(filteredArrivals = filtered) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        applyFilters()
    }

    fun onStatusSelected(status: MooringStatus?) {
        val newStatus = if (_uiState.value.selectedStatus == status) null else status
        _uiState.update { it.copy(selectedStatus = newStatus) }
        applyFilters()
    }

    fun onSectorSelected(sector: String?) {
        val newSector = if (_uiState.value.selectedSector == sector) null else sector
        _uiState.update { it.copy(selectedSector = newSector) }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedStatus = null, selectedSector = null) }
        applyFilters()
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = true,
                itemToEdit = null,
                errorMessage = null
            )
        }
    }

    fun openEditDialog(item: BerthArrivalItem) {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = true,
                itemToEdit = item,
                errorMessage = null
            )
        }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(
                showAddOrEditDialog = false,
                itemToEdit = null,
                errorMessage = null
            )
        }
    }

    fun saveArrivalSchedule(
        targetBerthId: Long,
        shipName: String,
        cargoName: String,
        tonnageStr: String,
        startDayStr: String,
        endDayStr: String,
        draftStr: String,
        status: MooringStatus
    ) {
        val tonnage = tonnageStr.toDoubleOrNull()
        val startDay = startDayStr.toIntOrNull()
        val endDay = endDayStr.toIntOrNull()
        val draft = draftStr.toDoubleOrNull()

        if (shipName.isBlank() || cargoName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos obrigatórios (Nome do Navio e Carga).") }
            return
        }

        if (tonnage == null || tonnage <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe uma tonelagem válida maior que zero.") }
            return
        }

        if (startDay == null || endDay == null || startDay !in 1..30 || endDay !in 1..30) {
            _uiState.update { it.copy(errorMessage = "Os dias de início e fim devem estar entre 1 e 30.") }
            return
        }

        if (startDay > endDay) {
            _uiState.update { it.copy(errorMessage = "O dia inicial não pode ser maior que o dia final.") }
            return
        }

        if (draft == null || draft <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe um calado válido maior que zero.") }
            return
        }

        val isEditMode = _uiState.value.itemToEdit != null
        val scheduleId = _uiState.value.itemToEdit?.schedule?.id ?: repository.getNextScheduleId()

        val schedule = ShipSchedule(
            id = scheduleId,
            shipName = shipName.trim(),
            cargoName = cargoName.trim(),
            tonnage = tonnage,
            startDay = startDay,
            endDay = endDay,
            draft = draft,
            status = status
        )

        val result = if (isEditMode) {
            repository.updateSchedule(targetBerthId, schedule)
        } else {
            repository.addSchedule(targetBerthId, schedule)
        }

        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        showAddOrEditDialog = false,
                        itemToEdit = null,
                        errorMessage = null,
                        successMessage = if (isEditMode) "Atracação atualizada com sucesso!" else "Atracação agendada com sucesso!"
                    )
                }
            },
            onFailure = { exception ->
                _uiState.update {
                    it.copy(errorMessage = exception.message ?: "Erro ao salvar atracação.")
                }
            }
        )
    }

    fun deleteArrivalSchedule(berthId: Long, scheduleId: Long) {
        val removed = repository.removeSchedule(berthId, scheduleId)
        if (removed) {
            _uiState.update {
                it.copy(successMessage = "Atracação removida com sucesso.")
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
