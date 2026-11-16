package com.example.myapplication.data.model

import kotlin.math.sin

/**
 * Representa os dados astronômicos de maré para o período de 30 dias.
 * Cada dia contém 48 medições (a cada 30 minutos).
 */
object TideData {
    private const val TOTAL_DAYS = 30
    private const val MEASUREMENTS_PER_DAY = 48
    const val CRITICAL_LOW_TIDE_THRESHOLD = 1.1 // Metros (Abaixo disso considera-se maré baixa crítica)
    private const val SAFETY_MARGIN = 0.5 // Margem de segurança hidrodinâmica (UKC - Under Keel Clearance)

    // Mapa de medições por dia (dia 1 a 30) -> lista com 48 alturas de maré em metros (0.4m a 3.8m)
    val dailyTideMeasurements: Map<Int, List<Double>> by lazy {
        val map = mutableMapOf<Int, List<Double>>()
        for (day in 1..TOTAL_DAYS) {
            val measurements = mutableListOf<Double>()
            for (step in 0 until MEASUREMENTS_PER_DAY) {
                // Simula variação astronômica senoidal com ciclos de maré viva (sizígia) e maré morta (quadratura)
                val timeInHours = step * 0.5
                val dayPhase = (day - 1) * (2 * Math.PI / 14.76) // Ciclo lunar ~14.76 dias
                val tideOscillation = sin(2 * Math.PI * timeInHours / 12.42) // Período semidiurno ~12.42h
                
                // Amplitude varia com o ciclo astronômico
                val amplitude = 1.2 + 0.8 * sin(dayPhase)
                val baseLevel = 2.1
                
                val tideHeight = baseLevel + amplitude * tideOscillation
                // Arredonda para 2 casas decimais
                measurements.add((Math.round(tideHeight * 100.0) / 100.0).coerceAtLeast(0.3))
            }
            map[day] = measurements
        }
        map
    }

    /**
     * Retorna a menor maré (baixa-mar mínima) registrada para um determinado dia.
     */
    fun getMinTideForDay(day: Int): Double {
        return dailyTideMeasurements[day]?.minOrNull() ?: 2.0
    }

    /**
     * Retorna a maior maré (preamar máxima) registrada para um determinado dia.
     */
    fun getMaxTideForDay(day: Int): Double {
        return dailyTideMeasurements[day]?.maxOrNull() ?: 3.5
    }

    /**
     * Identifica se um dia possui maré baixa crítica (risco hidrodinâmico de encalhe).
     */
    fun isCriticalLowTideDay(day: Int): Boolean {
        return getMinTideForDay(day) < CRITICAL_LOW_TIDE_THRESHOLD
    }

    /**
     * RN002: Bloqueio Hidrodinâmico de Maré Baixa.
     * Verifica se um navio com determinado calado (draft) pode operar com segurança no berço
     * durante o período especificado.
     */
    fun checkHydrodynamicSafety(
        berthMaxDraft: Double,
        shipDraft: Double,
        startDay: Int,
        endDay: Int
    ): HydrodynamicValidationResult {
        // Regra 1: Calado do navio maior que o calado máximo absoluto do berço
        if (shipDraft > berthMaxDraft + 1.0) { // Tolera até 1.0m de maré alta se for maré normal, mas calado base não pode exceder berço
            return HydrodynamicValidationResult.Invalid(
                "Calado do navio (${shipDraft}m) excede o limite físico operacional do berço (${berthMaxDraft}m)."
            )
        }

        // Regra 2: Durante dias de maré baixa crítica, verifica a folga sob a quilha (UKC)
        for (day in startDay..endDay) {
            val minTide = getMinTideForDay(day)
            // Profundidade disponível no momento mais crítico = calado do berço + altura da maré baixa
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
