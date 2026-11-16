package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AddShipDialog
import com.example.myapplication.ui.components.BerthCard
import com.example.myapplication.ui.components.LineupHeader
import com.example.myapplication.ui.components.PaginationBar
import com.example.myapplication.ui.viewmodel.LineupViewModel

@Composable
fun LineupScreen(
    viewModel: LineupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Mostra Snackbar para mensagens de sucesso ou alerta
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
        }
    }

    // Scroll para o topo ao mudar de página
    LaunchedEffect(uiState.currentPage) {
        listState.animateScrollToItem(0)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            PaginationBar(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                totalItems = uiState.filteredBerths.size,
                itemsPerPage = uiState.itemsPerPage,
                onNextPage = { viewModel.nextPage() },
                onPreviousPage = { viewModel.previousPage() },
                onPageSelected = { page -> viewModel.goToPage(page) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho com Filtros, Busca e Métricas
            LineupHeader(
                uiState = uiState,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onCategorySelected = { viewModel.onCategorySelected(it) },
                onStatusSelected = { viewModel.onStatusSelected(it) },
                onClearFilters = { viewModel.clearFilters() }
            )

            // Lista Paginada de Berços (5 por página para garantir 60 FPS)
            if (uiState.paginatedBerths.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nenhum berço encontrado com os filtros atuais.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.paginatedBerths,
                        key = { berth -> berth.id }
                    ) { berth ->
                        BerthCard(
                            berth = berth,
                            onAddShipClick = { selectedBerth ->
                                viewModel.openAddShipDialog(selectedBerth)
                            },
                            onRemoveScheduleClick = { berthId, scheduleId ->
                                viewModel.removeSchedule(berthId, scheduleId)
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de Agendamento de Navio (com validações RN001 e RN002)
        if (uiState.showAddDialog && uiState.selectedBerthForAdd != null) {
            AddShipDialog(
                berth = uiState.selectedBerthForAdd!!,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.dismissAddShipDialog() },
                onConfirm = { shipName, cargoName, tonnage, startDay, endDay, draft, status ->
                    viewModel.addShipSchedule(
                        berthId = uiState.selectedBerthForAdd!!.id,
                        shipName = shipName,
                        cargoName = cargoName,
                        tonnageStr = tonnage,
                        startDayStr = startDay,
                        endDayStr = endDay,
                        draftStr = draft,
                        status = status
                    )
                }
            )
        }
    }
}
