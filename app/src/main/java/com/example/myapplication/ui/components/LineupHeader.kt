package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.MooringStatus
import com.example.myapplication.ui.viewmodel.LineupUiState

@Composable
fun LineupHeader(
    uiState: LineupUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (CargoCategory?) -> Unit,
    onStatusSelected: (MooringStatus?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(14.dp)
    ) {
        // Título Principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚓ Lineup Marítimo de Navios",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Complexo Portuário • 60 Berços Mapeados • Visualização 30 Dias",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Métricas e Indicadores do Porto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Berços",
                value = "${uiState.totalBerthsCount}",
                subtitle = "60 no porto",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Navios Ativos",
                value = "${uiState.activeShipsCount}",
                subtitle = "${uiState.operatingShipsCount} operando",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Carga Total",
                value = "%.1fk t".format(uiState.totalTonnageSum / 1000.0),
                subtitle = "Programado",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Alerta Visual de Maré Baixa Crítica (RN002)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFECEC),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌊 RN002 - Alerta Hidrodinâmico:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFC0392B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dias de maré baixa crítica (sombreamento vermelho no Canvas). Bloqueio automático de calado ativo.",
                    fontSize = 11.sp,
                    color = Color(0xFF7F8C8D)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de Pesquisa
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Buscar por berço, navio, carga ou setor...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chips de Filtro por Categoria de Carga & Status (RN005)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.selectedCategory != null || uiState.selectedStatus != null || uiState.searchQuery.isNotBlank()) {
                TextButton(onClick = onClearFilters) {
                    Text("Limpar Filtros", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            // Status (RN005 Cores Normatizadas)
            MooringStatus.entries.forEach { status ->
                val isSelected = uiState.selectedStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusSelected(status) },
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

            // Categorias de Carga
            CargoCategory.entries.forEach { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.composeColor.copy(alpha = 0.2f)
                    )
                )
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
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
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
