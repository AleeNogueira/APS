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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.TideDayRowCard
import com.example.myapplication.ui.components.TideTableCard
import com.example.myapplication.ui.viewmodel.PortTidesViewModel

@Composable
fun PortTidesScreen(
    viewModel: PortTidesViewModel = viewModel()
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho das Tábuas de Marés Astronômicas (RF008 / RN002 / RN008)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "🌊 Tábuas de Marés Astronômicas (RF008)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gêmeo Digital do Porto de Santos • Endpoints REST /api/port/tides/*",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seleção de Tábuas Mensais com Ação de Ativação no Servidor REST
                Text(
                    text = "Tábuas Mensais Disponíveis (Clique para Visualizar ou Ativar):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.tideTables.forEach { table ->
                        val isSelected = uiState.selectedTable?.id == table.id
                        TideTableCard(
                            table = table,
                            isSelected = isSelected,
                            onSelectClick = { viewModel.selectTideTable(table) },
                            onActivateClick = { viewModel.activateTideTable(table.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cartões de Métricas da Tábua Selecionada
                val currentTable = uiState.currentDisplayTable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Tábua Exibida",
                        value = currentTable?.monthKey ?: "N/A",
                        subtitle = if (currentTable?.isActive == true) "Ativa no Servidor" else "Inativa",
                        modifier = Modifier.weight(1.2f)
                    )

                    MetricCard(
                        title = "Média Geral",
                        value = "%.2fm".format(uiState.averageHeight),
                        subtitle = "Altura Astronômica",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Dias Críticos",
                        value = "${uiState.criticalDaysCount} dias",
                        subtitle = "Bloqueio < 1.1m (RN002)",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Busca por Dia
                OutlinedTextField(
                    value = uiState.searchDayQuery,
                    onValueChange = { viewModel.onSearchDayQueryChanged(it) },
                    placeholder = { Text("Filtrar por dia (ex: Dia 08, Dia 21)...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de Filtro por Status de Navegação
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.filterStatus != null || uiState.searchDayQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Limpar Filtros", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    FilterChip(
                        selected = uiState.filterStatus == "LIBERADO",
                        onClick = { viewModel.onFilterStatusSelected("LIBERADO") },
                        label = { Text("🟢 MARÉ ALTA (LIBERADO)", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = uiState.filterStatus == "BLOQUEADO",
                        onClick = { viewModel.onFilterStatusSelected("BLOQUEADO") },
                        label = { Text("🔴 MARÉ BAIXA (BLOQUEADO)", fontSize = 11.sp) }
                    )
                }
            }

            // Indicador de Carregamento REST
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

            // Lista dos 30 Dias do Mês Selecionado
            val activeTideTable = uiState.currentDisplayTable
            if (activeTideTable == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma tábua de marés selecionada.")
                }
            } else {
                val filteredDays = (1..30).filter { day ->
                    val query = uiState.searchDayQuery.trim()
                    val matchesQuery = query.isBlank() ||
                            "Dia %02d".format(day).contains(query, ignoreCase = true) ||
                            day.toString() == query

                    val isCritical = activeTideTable.isCriticalLowTideDay(day)
                    val matchesStatus = when (uiState.filterStatus) {
                        "LIBERADO" -> !isCritical
                        "BLOQUEADO" -> isCritical
                        else -> true
                    }

                    matchesQuery && matchesStatus
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(
                        items = filteredDays,
                        key = { day -> day }
                    ) { dayNumber ->
                        TideDayRowCard(
                            dayNumber = dayNumber,
                            tideTable = activeTideTable
                        )
                    }
                }
            }
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
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 9.sp,
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
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
