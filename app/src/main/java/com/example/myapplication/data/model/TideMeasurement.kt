package com.example.myapplication.data.model

/**
 * RF008 - Medição astronômica de maré individual (a cada 30 minutos).
 */
data class TideMeasurement(
    val id: Long,
    val dayNumber: Int,      // Dia do mês (1 a 30)
    val timeSlot: Int,       // Slot de 0 a 47 (a cada 30 min, ex: 0 = 00:00, 1 = 00:30, ..., 47 = 23:30)
    val height: Double       // Altura da maré em metros (ex: 1.49)
)
