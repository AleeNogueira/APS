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
import com.example.myapplication.data.model.CargoCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPortCargoDialog(
    nextCodeSuggestion: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        productCode: String,
        productName: String,
        operationType: String,
        pranchaRateStr: String,
        category: CargoCategory
    ) -> Unit
) {
    var productCode by remember { mutableStateOf(nextCodeSuggestion) }
    var productName by remember { mutableStateOf("") }
    var pranchaRate by remember { mutableStateOf("15000") }

    val operationOptions = listOf("↑ Carga", "↓ Descarga", "↓↑ Descarga & Carga")
    var selectedOperation by remember { mutableStateOf(operationOptions[0]) }
    var operationDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(CargoCategory.SOLIDOS_VEGETAIS) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Cadastrar Perfil de Carga (RF006)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Endpoint REST /api/port/cargos",
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
                        value = productCode,
                        onValueChange = { productCode = it },
                        label = { Text("Código (ex: CRG-13)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = pranchaRate,
                        onValueChange = { pranchaRate = it },
                        label = { Text("Prancha (t/dia)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nome da Mercadoria (ex: Açúcar Ensacado)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector do Tipo de Operação
                ExposedDropdownMenuBox(
                    expanded = operationDropdownExpanded,
                    onExpandedChange = { operationDropdownExpanded = !operationDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOperation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Operação") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operationDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = operationDropdownExpanded,
                        onDismissRequest = { operationDropdownExpanded = false }
                    ) {
                        operationOptions.forEach { op ->
                            DropdownMenuItem(
                                text = { Text(op, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedOperation = op
                                    operationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Categoria de Carga
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria da Carga") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        CargoCategory.entries.filterNot { it.name.startsWith("VEGETABLE") || it.name.startsWith("PULP") || it.name.startsWith("CONTAINERS") || it.name.startsWith("LIQUID_BULK") || it.name.startsWith("GENERAL_CARGO") || it.name.startsWith("MINERAL_BULK") }.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat.label,
                                        color = cat.composeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(productCode, productName, selectedOperation, pranchaRate, selectedCategory)
                }
            ) {
                Text("Enviar via REST (POST)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
