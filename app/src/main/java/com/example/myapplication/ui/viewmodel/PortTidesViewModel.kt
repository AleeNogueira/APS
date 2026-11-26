package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.TideTable
import com.example.myapplication.data.repository.PortTidesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortTidesViewModel(
    private val repository: PortTidesRepository = PortTidesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortTidesUiState())
    val uiState: StateFlow<PortTidesUiState> = _uiState.asStateFlow()

    init {
        loadTides()
    }

    fun loadTides() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getTideTables()
            result.fold(
                onSuccess = { list ->
                    val active = list.find { it.isActive } ?: list.firstOrNull()
                    _uiState.update { currentState ->
                        currentState.copy(
                            tideTables = list,
                            activeTable = active,
                            selectedTable = currentState.selectedTable ?: active,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao carregar tábuas de marés."
                        )
                    }
                }
            )
        }
    }

    fun selectTideTable(table: TideTable) {
        _uiState.update { it.copy(selectedTable = table) }
    }

    fun activateTideTable(tableId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.activateTideTable(tableId)
            result.fold(
                onSuccess = { activatedTable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Tábua de Marés '${activatedTable.monthKey}' ativada com sucesso no servidor REST!"
                        )
                    }
                    loadTides()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao ativar tábua de marés no servidor."
                        )
                    }
                }
            )
        }
    }

    fun onSearchDayQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchDayQuery = newQuery) }
    }

    fun onFilterStatusSelected(status: String?) {
        val newStatus = if (_uiState.value.filterStatus == status) null else status
        _uiState.update { it.copy(filterStatus = newStatus) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchDayQuery = "", filterStatus = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
