package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.RegisteredVessel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditVesselDialog(
    vesselToEdit: RegisteredVessel?,
    nextImoSuggestion: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        imoCode: String,
        shipName: String,
        vesselClass: String,
        draftStr: String,
        tonnageStr: String,
        importCargo: String,
        exportCargo: String
    ) -> Unit
) {
    val isEditMode = vesselToEdit != null

    var imoCode by remember { mutableStateOf(vesselToEdit?.imoCode ?: nextImoSuggestion) }
    var shipName by remember { mutableStateOf(vesselToEdit?.shipName ?: "") }
    var vesselClass by remember { mutableStateOf(vesselToEdit?.vesselClass ?: "Panamax Graneleiro") }
    var draft by remember { mutableStateOf(vesselToEdit?.maxDraft?.toString() ?: "14.2") }
    var tonnage by remember { mutableStateOf(vesselToEdit?.standardTonnage?.toString() ?: "68500") }
    var importCargo by remember { mutableStateOf(vesselToEdit?.typicalImportCargo ?: "Trigo Importação") }
    var exportCargo by remember { mutableStateOf(vesselToEdit?.typicalExportCargo ?: "Soja em Grãos") }

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
    var classDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (vesselToEdit != null) "Editar Navio (${vesselToEdit.imoCode})" else "Cadastrar Navio Mercante (RF007)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Endpoint REST /api/port/vessels",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = imoCode,
                        onValueChange = { imoCode = it },
                        readOnly = isEditMode, // IMO é chave primária
                        label = { Text("Código IMO (RN007)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        label = { Text("Calado (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shipName,
                    onValueChange = { shipName = it },
                    label = { Text("Nome do Navio (ex: MV Atlantic Star)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector da Classe da Embarcação
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = vesselClass,
                        onValueChange = { vesselClass = it },
                        label = { Text("Classe da Embarcação") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        classOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    vesselClass = option
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tonnage,
                    onValueChange = { tonnage = it },
                    label = { Text("Tonelagem Padrão (t)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = importCargo,
                        onValueChange = { importCargo = it },
                        label = { Text("Carga Descarga (↓)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = exportCargo,
                        onValueChange = { exportCargo = it },
                        label = { Text("Carga Carregamento (↑)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(imoCode, shipName, vesselClass, draft, tonnage, importCargo, exportCargo)
                }
            ) {
                Text(if (isEditMode) "Atualizar (REST PUT)" else "Cadastrar (REST POST)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
