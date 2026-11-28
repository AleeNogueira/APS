package com.example.myapplication.data.model

/**
 * RF009 - Objeto consolidado para exibição das atracacões de navios na Programação Geral.
 */
data class BerthArrivalItem(
    val berthId: Long,
    val berthName: String,
    val sectorName: String,
    val maxDraft: Double,
    val schedule: ShipSchedule
)
