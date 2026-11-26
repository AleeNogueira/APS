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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AddOrEditVesselDialog
import com.example.myapplication.ui.components.VesselCard
import com.example.myapplication.ui.viewmodel.PortVesselsViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PortVesselsScreen(
    viewModel: PortVesselsViewModel = viewModel()
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
                Text("+ Navio", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho da Frota de Navios Mercantes & IMO Codes (RF007)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "⚓ Frota Mercante & Códigos IMO (RF007)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gêmeo Digital do Porto de Santos • Endpoints REST /api/port/vessels",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cartões de Métricas da Frota
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedTonnage = try {
                        NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(uiState.totalTonnageSum)
                    } catch (_: Exception) {
                        "%.0f".format(uiState.totalTonnageSum)
                    }

                    MetricCard(
                        title = "Frota Cadastrada",
                        value = "${uiState.totalFleetCount} navios",
                        subtitle = "Cadastro Santos",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Calado Médio",
                        value = "%.1fm".format(uiState.averageDraft),
                        subtitle = "Profundidade",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Tonelagem Total",
                        value = "$formattedTonnage t",
                        subtitle = "Capacidade Padrão",
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Pesquisa Reativa (Nome, IMO, Classe ou Carga)
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Buscar por nome, IMO (ex: IMO-9810100) ou classe...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de Filtro por Classe da Embarcação
                val classOptions = listOf(
                    "Panamax Graneleiro",
                    "Kamsarmax Bulk Carrier",
                    "Capesize Bulk Carrier",
                    "Post-Panamax Contêineres",
                    "Aframax Petroleiro",
                    "Suezmax Tanque",
                    "Navio de Cruzeiro",
                    "Apoio Offshore (PSV)"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.selectedVesselClass != null || uiState.searchQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Limpar Filtros", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    classOptions.forEach { option ->
                        val isSelected = uiState.selectedVesselClass.equals(option, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onClassFilterSelected(option) },
                            label = { Text(option, fontSize = 11.sp) }
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

            // Lista da Frota de Navios
            if (uiState.filteredVessels.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum navio encontrado com os filtros atuais.",
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
                        items = uiState.filteredVessels,
                        key = { vessel -> vessel.imoCode }
                    ) { vessel ->
                        VesselCard(
                            vessel = vessel,
                            onEditClick = { target -> viewModel.openEditDialog(target) },
                            onDeleteClick = { imo -> viewModel.deleteVessel(imo) }
                        )
                    }
                }
            }
        }

        // Diálogo de Adição ou Edição de Navio Mercante
        if (uiState.showAddOrEditDialog) {
            val nextSuggestedNum = 9810000 + (uiState.vesselsList.size + 1) * 3
            val nextImoCode = "IMO-%07d".format(nextSuggestedNum)

            AddOrEditVesselDialog(
                vesselToEdit = uiState.vesselToEdit,
                nextImoSuggestion = nextImoCode,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { imo, name, vesselClass, draft, tonnage, importCargo, exportCargo ->
                    viewModel.saveVessel(
                        imoInput = imo,
                        shipName = name,
                        vesselClass = vesselClass,
                        draftStr = draft,
                        tonnageStr = tonnage,
                        importCargo = importCargo,
                        exportCargo = exportCargo
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
