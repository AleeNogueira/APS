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
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.ui.components.AddPortCargoDialog
import com.example.myapplication.ui.components.PortCargoCard
import com.example.myapplication.ui.viewmodel.PortCargosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PortCargosScreen(
    viewModel: PortCargosViewModel = viewModel()
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
                Text("+ Cargo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho do Catálogo de Cargas do Porto de Santos (RF006)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "🚢 Perfis de Carga Portuária (RF006)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gêmeo Digital do Porto de Santos • Endpoints REST /api/port/cargos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cartões de Métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedTotalTpd = try {
                        NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(uiState.totalCapacityTpd)
                    } catch (_: Exception) {
                        "%.0f".format(uiState.totalCapacityTpd)
                    }

                    MetricCard(
                        title = "Produtos CADASTRADOS",
                        value = "${uiState.totalCargosCount}",
                        subtitle = "Catálogo Santos",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Prancha Total",
                        value = "$formattedTotalTpd t/d",
                        subtitle = "Capacidade Operacional",
                        modifier = Modifier.weight(1.3f)
                    )

                    MetricCard(
                        title = "Categorias",
                        value = "${uiState.categoriesCount}",
                        subtitle = "Segmentos de Carga",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Pesquisa
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Buscar por código (CRG-xx), produto ou categoria...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtros Horizontais (Categoria e Operação)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.selectedCategory != null || uiState.selectedOperation != null || uiState.searchQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Limpar Filtros", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Operações
                    listOf("Carga", "Descarga", "Descarga & Carga").forEach { op ->
                        val isSelected = uiState.selectedOperation == op
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onOperationSelected(op) },
                            label = { Text(op, fontSize = 11.sp) }
                        )
                    }

                    // Categorias
                    CargoCategory.entries.filterNot { it.name.startsWith("VEGETABLE") || it.name.startsWith("PULP") || it.name.startsWith("CONTAINERS") || it.name.startsWith("LIQUID_BULK") || it.name.startsWith("GENERAL_CARGO") || it.name.startsWith("MINERAL_BULK") }.forEach { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelected(cat) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cat.composeColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(cat.label, fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.composeColor.copy(alpha = 0.2f)
                            )
                        )
                    }
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

            // Lista de Perfis de Carga
            if (uiState.filteredCargos.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum perfil de carga encontrado.",
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
                        items = uiState.filteredCargos,
                        key = { item -> item.productCode }
                    ) { item ->
                        PortCargoCard(
                            item = item,
                            onDeleteClick = { code -> viewModel.deletePortCargo(code) }
                        )
                    }
                }
            }
        }

        // Diálogo de Adição de Perfil de Carga (POST /api/port/cargos)
        if (uiState.showAddDialog) {
            val nextSuggestedNum = uiState.cargosList.size + 1
            val nextCode = "CRG-%02d".format(nextSuggestedNum)

            AddPortCargoDialog(
                nextCodeSuggestion = nextCode,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.dismissAddDialog() },
                onConfirm = { code, name, operation, rate, category ->
                    viewModel.addPortCargo(
                        productCodeInput = code,
                        productName = name,
                        operationType = operation,
                        pranchaRateStr = rate,
                        category = category
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
                fontSize = 15.sp,
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
