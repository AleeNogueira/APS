package com.example.myapplication.data.model

/**
 * RF008 - Tábua de Marés Astronômicas Mensal do Porto de Santos.
 * Contém 1.440 medições (30 dias x 48 medições de 30 em 30 min).
 */
data class TideTable(
    val id: Long,
    val monthKey: String,             // Ex: "Novembro / 2026"
    val isActive: Boolean,            // Indica se é a tábua ativa no servidor REST
    val measurements: List<TideMeasurement>
) {
    /**
     * Obter medições de um dia específico (1 a 30).
     */
    fun getMeasurementsForDay(day: Int): List<TideMeasurement> {
        return measurements.filter { it.dayNumber == day }
    }

    /**
     * Retorna a menor maré do dia.
     */
    fun getMinTideForDay(day: Int): Double {
        return getMeasurementsForDay(day).minOfOrNull { it.height } ?: 1.5
    }

    /**
     * Retorna a maior maré do dia.
     */
    fun getMaxTideForDay(day: Int): Double {
        return getMeasurementsForDay(day).maxOfOrNull { it.height } ?: 3.5
    }

    /**
     * Retorna se o dia possui maré baixa crítica (maré mínima < 1.1m) [RN002].
     */
    fun isCriticalLowTideDay(day: Int): Boolean {
        return getMinTideForDay(day) < TideData.CRITICAL_LOW_TIDE_THRESHOLD
    }
}
