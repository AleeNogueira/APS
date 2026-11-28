package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.MooringStatus
import com.example.myapplication.ui.components.AddOrEditArrivalDialog
import com.example.myapplication.ui.components.ArrivalCard
import com.example.myapplication.ui.viewmodel.PortArrivalsViewModel

@Composable
fun PortArrivalsScreen(
    viewModel: PortArrivalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+ Atracação", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho da Programação Geral de Atracações (RF009)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "🗓️ Programação Geral de Atracações (RF009)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gêmeo Digital do Porto de Santos • Sincronização Reativa (RN008)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cartões de Métricas das Atracações
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricCard(
                        title = "Atracações",
                        value = "${uiState.totalArrivalsCount}",
                        subtitle = "60 Berços",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Operando",
                        value = "${uiState.operatingCount}",
                        subtitle = "Prioridade",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Programados",
                        value = "${uiState.scheduledCount}",
                        subtitle = "Aguardando",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Confirmados",
                        value = "${uiState.confirmedCount}",
                        subtitle = "Liberados",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Pesquisa Reativa (Navio, Carga, Berço, Setor)
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Buscar por navio, carga, berço ou setor...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de Filtro por Status (RN005 Cores Normatizadas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.selectedStatus != null || uiState.selectedSector != null || uiState.searchQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Limpar Filtros", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    MooringStatus.entries.forEach { status ->
                        val isSelected = uiState.selectedStatus == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusSelected(status) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(status.composeColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(status.label, fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = status.composeColor.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            // Indicador de Carregamento
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            // Lista Consolidada de Atracações
            if (uiState.filteredArrivals.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma atracação encontrada com os filtros atuais.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.filteredArrivals,
                        key = { item -> "${item.berthId}_${item.schedule.id}" }
                    ) { item ->
                        ArrivalCard(
                            item = item,
                            onEditClick = { target -> viewModel.openEditDialog(target) },
                            onDeleteClick = { berthId, scheduleId ->
                                viewModel.deleteArrivalSchedule(berthId, scheduleId)
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de Adição ou Edição de Atracação
        if (uiState.showAddOrEditDialog) {
            AddOrEditArrivalDialog(
                itemToEdit = uiState.itemToEdit,
                allBerths = uiState.allBerths,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { berthId, shipName, cargoName, tonnage, startDay, endDay, draft, status ->
                    viewModel.saveArrivalSchedule(
                        targetBerthId = berthId,
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

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
