package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.PortCargoCatalogItem
import com.example.myapplication.data.repository.PortCargoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortCargosViewModel(
    private val repository: PortCargoRepository = PortCargoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortCargoUiState())
    val uiState: StateFlow<PortCargoUiState> = _uiState.asStateFlow()

    init {
        loadCargos()
    }

    fun loadCargos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getPortCargos()
            result.fold(
                onSuccess = { list ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            cargosList = list,
                            isLoading = false
                        )
                    }
                    applyFilters()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao carregar catálogo do servidor."
                        )
                    }
                }
            )
        }
    }

    private fun applyFilters() {
        val all = _uiState.value.cargosList
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory
        val operation = _uiState.value.selectedOperation

        val filtered = all.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.productCode.contains(query, ignoreCase = true) ||
                    item.productName.contains(query, ignoreCase = true) ||
                    item.category.label.contains(query, ignoreCase = true)

            val matchesCategory = category == null || item.category == category

            val matchesOperation = operation == null ||
                    item.operationType.contains(operation, ignoreCase = true)

            matchesQuery && matchesCategory && matchesOperation
        }

        _uiState.update { it.copy(filteredCargos = filtered) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        applyFilters()
    }

    fun onCategorySelected(category: CargoCategory?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }
        applyFilters()
    }

    fun onOperationSelected(operation: String?) {
        val newOp = if (_uiState.value.selectedOperation == operation) null else operation
        _uiState.update { it.copy(selectedOperation = newOp) }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategory = null,
                selectedOperation = null
            )
        }
        applyFilters()
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, errorMessage = null) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, errorMessage = null) }
    }

    fun addPortCargo(
        productCodeInput: String,
        productName: String,
        operationType: String,
        pranchaRateStr: String,
        category: CargoCategory
    ) {
        val code = productCodeInput.trim().uppercase()
        val formattedCode = if (code.startsWith("CRG-")) code else "CRG-$code"
        val rate = pranchaRateStr.toDoubleOrNull()

        if (productName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe o nome da mercadoria.") }
            return
        }

        if (rate == null || rate <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe uma taxa de prancha média válida (t/dia).") }
            return
        }

        val newItem = PortCargoCatalogItem(
            productCode = formattedCode,
            productName = productName.trim(),
            operationType = operationType,
            pranchaRateTpd = rate,
            category = category
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.addPortCargo(newItem)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showAddDialog = false,
                            isLoading = false,
                            successMessage = "Perfil $formattedCode adicionado com sucesso no servidor!"
                        )
                    }
                    loadCargos()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao adicionar perfil no servidor."
                        )
                    }
                }
            )
        }
    }

    fun deletePortCargo(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.deletePortCargo(productCode)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Perfil $productCode removido do servidor com sucesso."
                        )
                    }
                    loadCargos()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao remover perfil do servidor."
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
