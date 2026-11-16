package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Berth
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.MooringStatus
import com.example.myapplication.data.model.ShipSchedule
import com.example.myapplication.data.repository.LineupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil

class LineupViewModel(
    private val repository: LineupRepository = LineupRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineupUiState())
    val uiState: StateFlow<LineupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.berthsFlow.collect { berths ->
                updateBerthsAndPagination(allBerths = berths)
            }
        }
    }

    private fun updateBerthsAndPagination(
        allBerths: List<Berth> = _uiState.value.allBerths,
        query: String = _uiState.value.searchQuery,
        category: CargoCategory? = _uiState.value.selectedCategory,
        status: MooringStatus? = _uiState.value.selectedStatus,
        sector: String? = _uiState.value.selectedSector,
        targetPage: Int = _uiState.value.currentPage
    ) {
        // Aplica filtros de pesquisa e categoria/status/setor
        val filtered = allBerths.filter { berth ->
            val matchesQuery = query.isBlank() ||
                    berth.name.contains(query, ignoreCase = true) ||
                    berth.sectorName.contains(query, ignoreCase = true) ||
                    berth.schedules.any { it.shipName.contains(query, ignoreCase = true) || it.cargoName.contains(query, ignoreCase = true) }

            val matchesCategory = category == null || berth.category == category
            val matchesSector = sector == null || berth.sectorName == sector
            val matchesStatus = status == null || berth.schedules.any { it.status == status }

            matchesQuery && matchesCategory && matchesSector && matchesStatus
        }

        val itemsPerPage = _uiState.value.itemsPerPage
        val calculatedTotalPages = if (filtered.isEmpty()) 1 else ceil(filtered.size.toDouble() / itemsPerPage).toInt()
        val validPage = targetPage.coerceIn(1, calculatedTotalPages)

        val startIndex = (validPage - 1) * itemsPerPage
        val endIndex = (startIndex + itemsPerPage).coerceAtMost(filtered.size)
        val paginated = if (startIndex < filtered.size) filtered.subList(startIndex, endIndex) else emptyList()

        _uiState.update { currentState ->
            currentState.copy(
                allBerths = allBerths,
                filteredBerths = filtered,
                paginatedBerths = paginated,
                currentPage = validPage,
                totalPages = calculatedTotalPages,
                searchQuery = query,
                selectedCategory = category,
                selectedStatus = status,
                selectedSector = sector
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        updateBerthsAndPagination(query = newQuery, targetPage = 1)
    }

    fun onCategorySelected(category: CargoCategory?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        updateBerthsAndPagination(category = newCategory, targetPage = 1)
    }

    fun onStatusSelected(status: MooringStatus?) {
        val newStatus = if (_uiState.value.selectedStatus == status) null else status
        updateBerthsAndPagination(status = newStatus, targetPage = 1)
    }

    fun onSectorSelected(sector: String?) {
        val newSector = if (_uiState.value.selectedSector == sector) null else sector
        updateBerthsAndPagination(sector = newSector, targetPage = 1)
    }

    fun clearFilters() {
        updateBerthsAndPagination(query = "", category = null, status = null, sector = null, targetPage = 1)
    }

    fun goToPage(page: Int) {
        updateBerthsAndPagination(targetPage = page)
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages) {
            goToPage(_uiState.value.currentPage + 1)
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 1) {
            goToPage(_uiState.value.currentPage - 1)
        }
    }

    fun openAddShipDialog(berth: Berth) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                selectedBerthForAdd = berth,
                errorMessage = null
            )
        }
    }

    fun dismissAddShipDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                selectedBerthForAdd = null,
                errorMessage = null
            )
        }
    }

    fun addShipSchedule(
        berthId: Long,
        shipName: String,
        cargoName: String,
        tonnageStr: String,
        startDayStr: String,
        endDayStr: String,
        draftStr: String,
        status: MooringStatus
    ) {
        // Validação dos campos de entrada
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

        val schedule = ShipSchedule(
            id = repository.getNextScheduleId(),
            shipName = shipName.trim(),
            cargoName = cargoName.trim(),
            tonnage = tonnage,
            startDay = startDay,
            endDay = endDay,
            draft = draft,
            status = status
        )

        val result = repository.addSchedule(berthId, schedule)
        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        showAddDialog = false,
                        selectedBerthForAdd = null,
                        errorMessage = null,
                        successMessage = "Navio '${shipName}' agendado com sucesso!"
                    )
                }
            },
            onFailure = { exception ->
                _uiState.update {
                    it.copy(errorMessage = exception.message ?: "Erro ao agendar navio.")
                }
            }
        )
    }

    fun removeSchedule(berthId: Long, scheduleId: Long) {
        val removed = repository.removeSchedule(berthId, scheduleId)
        if (removed) {
            _uiState.update {
                it.copy(successMessage = "Agendamento removido com sucesso.")
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }
}
