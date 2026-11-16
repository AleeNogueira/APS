package com.example.myapplication.data.model

import androidx.compose.ui.graphics.Color

/**
 * RN005: Cores Normatizadas de Status
 * - Vermelho (#e74c3c): Operando / Prioridade
 * - Laranja (#e67e22): Programado
 * - Verde (#2ecc71): Confirmado
 */
enum class MooringStatus(
    val label: String,
    val colorHex: String,
    val composeColor: Color
) {
    OPERATING("Operando / Prioridade", "#e74c3c", Color(0xFFE74C3C)),
    SCHEDULED("Programado", "#e67e22", Color(0xFFE67E22)),
    CONFIRMED("Confirmado", "#2ecc71", Color(0xFF2ECC71))
}
