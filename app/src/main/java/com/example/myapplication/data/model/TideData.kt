package com.example.myapplication.data.model

import kotlin.math.sin

/**
 * Representa os dados astronômicos de maré para o período de 30 dias do Porto de Santos.
 * Sincronizado reativamente com as Tábuas de Marés (RF008 / RN002).
 */
object TideData {
    private const val TOTAL_DAYS = 30
    private const val MEASUREMENTS_PER_DAY = 48
    const val CRITICAL_LOW_TIDE_THRESHOLD = 1.1 // Metros (Abaixo disso considera-se maré baixa crítica)
    private const val SAFETY_MARGIN = 0.5 // Margem de segurança hidrodinâmica (UKC)

    @Volatile
    private var customActiveMeasurements: Map<Int, List<Double>>? = null

    fun setActiveTable(table: TideTable) {
        val map = mutableMapOf<Int, List<Double>>()
        for (day in 1..TOTAL_DAYS) {
            val dayHeights = table.getMeasurementsForDay(day).map { it.height }
            map[day] = if (dayHeights.size == MEASUREMENTS_PER_DAY) dayHeights else defaultMeasurementsForDay(day)
        }
        customActiveMeasurements = map
    }

    // Mapa de medições astronômicas padrão (dia 1 a 30) -> 48 medições por dia
    val dailyTideMeasurements: Map<Int, List<Double>>
        get() = customActiveMeasurements ?: defaultDailyTideMeasurements

    private val defaultDailyTideMeasurements: Map<Int, List<Double>> by lazy {
        val map = mutableMapOf<Int, List<Double>>()
        for (day in 1..TOTAL_DAYS) {
            map[day] = defaultMeasurementsForDay(day)
        }
        map
    }

    private fun defaultMeasurementsForDay(day: Int): List<Double> {
        val measurements = mutableListOf<Double>()
        for (step in 0 until MEASUREMENTS_PER_DAY) {
            val timeInHours = step * 0.5
            val dayPhase = (day - 1) * (2 * Math.PI / 14.76)
            val tideOscillation = sin(2 * Math.PI * timeInHours / 12.42)
            val amplitude = 1.2 + 0.8 * sin(dayPhase)
            val baseLevel = 2.1
            val tideHeight = baseLevel + amplitude * tideOscillation
            measurements.add((Math.round(tideHeight * 100.0) / 100.0).coerceAtLeast(0.3))
        }
        return measurements
    }

    fun getMinTideForDay(day: Int): Double {
        return dailyTideMeasurements[day]?.minOrNull() ?: 2.0
    }

    fun getMaxTideForDay(day: Int): Double {
        return dailyTideMeasurements[day]?.maxOrNull() ?: 3.5
    }

    fun isCriticalLowTideDay(day: Int): Boolean {
        return getMinTideForDay(day) < CRITICAL_LOW_TIDE_THRESHOLD
    }

    /**
     * RN002: Bloqueio Hidrodinâmico de Maré Baixa.
     */
    fun checkHydrodynamicSafety(
        berthMaxDraft: Double,
        shipDraft: Double,
        startDay: Int,
        endDay: Int
    ): HydrodynamicValidationResult {
        if (shipDraft > berthMaxDraft + 1.0) {
            return HydrodynamicValidationResult.Invalid(
                "Calado do navio (${shipDraft}m) excede o limite físico operacional do berço (${berthMaxDraft}m)."
            )
        }

        for (day in startDay..endDay) {
            val minTide = getMinTideForDay(day)
            val availableDepth = berthMaxDraft + minTide
            val requiredDepth = shipDraft + SAFETY_MARGIN

            if (isCriticalLowTideDay(day) && requiredDepth > availableDepth) {
                return HydrodynamicValidationResult.Invalid(
                    "RN002 - Bloqueio Hidrodinâmico! Alerta de encalhe no Dia $day: Maré baixa crítica (${minTide}m). " +
                            "Profundidade disponível (${availableDepth}m) é insuficiente para calado de ${shipDraft}m + margem de segurança de ${SAFETY_MARGIN}m."
                )
            }
        }

        return HydrodynamicValidationResult.Valid
    }
}

sealed class HydrodynamicValidationResult {
    object Valid : HydrodynamicValidationResult()
    data class Invalid(val reason: String) : HydrodynamicValidationResult()
}
