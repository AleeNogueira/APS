package com.example.myapplication.data.model

data class Berth(
    val id: Long,
    val name: String,
    val sectorName: String,
    val maxDraft: Double, // Calado máximo operacional do berço em metros
    val category: CargoCategory,
    val schedules: MutableList<ShipSchedule> = mutableListOf()
) {
    /**
     * RN001: Validação Anti-Sobreposição
     * Retorna true se houver conflito de agendamento no mesmo intervalo de dias.
     */
    fun hasOverlap(startDay: Int, endDay: Int, excludeScheduleId: Long? = null): Boolean {
        return schedules.any { schedule ->
            if (excludeScheduleId != null && schedule.id == excludeScheduleId) {
                false
            } else {
                startDay <= schedule.endDay && endDay >= schedule.startDay
            }
        }
    }
}
