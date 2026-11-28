package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Berth
import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.HydrodynamicValidationResult
import com.example.myapplication.data.model.MooringStatus
import com.example.myapplication.data.model.ShipSchedule
import com.example.myapplication.data.model.TideData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class LineupRepository {

    private val _berthsFlow = MutableStateFlow<List<Berth>>(emptyList())
    val berthsFlow: StateFlow<List<Berth>> = _berthsFlow.asStateFlow()

    init {
        generateMockBerths()
    }

    private fun generateMockBerths() {
        val sectors = listOf(
            "Terminal de Graneis Vegetais",
            "Terminal de Contêineres (TECON)",
            "Pier de Celulose & Papel",
            "Terminal de Liquidos Quimicos",
            "Cais de Carga Geral & Projetos",
            "Terminal Mineral Norte"
        )

        val shipPrefixes = listOf("MV", "MS", "MT", "CMA CGM", "Maersk", "MSC", "Hapag", "ONE", "COSCO", "Evergreen")
        val shipNames = listOf(
            "Santos Express", "Bahia Star", "Paranaguá Glory", "Atlantic Vanguard", "Pacifica Titan",
            "Rio Horizon", "Amazon Conqueror", "Cap San Lorenzo", "Meridian Pioneer", "Polaris Leader",
            "Navagante I", "Vitoria Pride", "Itajaí Breeze", "Suape Fortune", "Itaqui Monarch"
        )

        val cargoTypesMap = mapOf(
            CargoCategory.VEGETABLE_BULK to listOf("Soja em Grão", "Milho", "Farelo de Soja", "Açúcar VHP"),
            CargoCategory.CONTAINERS to listOf("Contêineres Reefer", "Contêineres Dry", "Autopeças & Carga Geral"),
            CargoCategory.PULP to listOf("Fardos de Celulose", "Papel KRAFT", "Bobinas de Papel"),
            CargoCategory.LIQUID_BULK to listOf("Óleo Vegetal", "Combustível Marítimo", "Etanol Anidro", "Produtos Químicos"),
            CargoCategory.GENERAL_CARGO to listOf("Trilhos de Aço", "Equipamentos Eólicos", "Transformadores", "Carga Projeto"),
            CargoCategory.MINERAL_BULK to listOf("Minério de Ferro", "Bauxita", "Fertilizantes NPK", "Cloreto de Potássio")
        )

        val berthsList = mutableListOf<Berth>()
        var scheduleIdCounter = 1L

        // Gera 60 berços exatos conforme o Requisito de Domínio 1
        for (i in 1..60) {
            val sectorIndex = (i - 1) / 10
            val sector = sectors[sectorIndex.coerceIn(0, sectors.size - 1)]
            val category = CargoCategory.entries[(i - 1) % CargoCategory.entries.size]
            val maxDraft = 11.5 + ((i * 3) % 45) * 0.1 // Calados variando de 11.5m a 15.9m

            val berth = Berth(
                id = i.toLong(),
                name = "Berço %03d".format(100 + i),
                sectorName = sector,
                maxDraft = Math.round(maxDraft * 10.0) / 10.0,
                category = category,
                schedules = mutableListOf()
            )

            // Adiciona de 1 a 2 agendamentos por berço sem sobreposição
            val numShips = if (i % 3 == 0) 2 else 1
            var currentDay = 2 + (i % 3)

            for (s in 0 until numShips) {
                val duration = 3 + (i + s) % 4 // Duração de 3 a 6 dias
                val startDay = currentDay
                val endDay = (startDay + duration - 1).coerceAtMost(28)

                if (startDay <= endDay && !berth.hasOverlap(startDay, endDay)) {
                    val status = when ((i + s) % 3) {
                        0 -> MooringStatus.OPERATING
                        1 -> MooringStatus.SCHEDULED
                        else -> MooringStatus.CONFIRMED
                    }
                    val cargoList = cargoTypesMap[category] ?: listOf("Carga Diversa")
                    val cargoName = cargoList[(i + s) % cargoList.size]
                    val shipFullName = "${shipPrefixes[(i + s) % shipPrefixes.size]} ${shipNames[(i + s) % shipNames.size]}"
                    val tonnage = 25000.0 + ((i * 17 + s * 1000) % 45000)
                    // Garantir calado compatível com maré inicial
                    val draft = (berth.maxDraft - 1.2 + (s * 0.3)).coerceAtLeast(9.5)

                    val shipSchedule = ShipSchedule(
                        id = scheduleIdCounter++,
                        shipName = shipFullName,
                        cargoName = cargoName,
                        tonnage = Math.round(tonnage * 10.0) / 10.0,
                        startDay = startDay,
                        endDay = endDay,
                        draft = Math.round(draft * 10.0) / 10.0,
                        status = status
                    )
                    berth.schedules.add(shipSchedule)
                }
                currentDay = endDay + 2
            }

            berthsList.add(berth)
        }

        _berthsFlow.value = berthsList
    }

    /**
     * Adiciona um novo agendamento de navio respeitando as Regras de Negócio:
     * - RN001: Anti-Sobreposição
     * - RN002: Bloqueio Hidrodinâmico de Maré Baixa
     */
    fun addSchedule(berthId: Long, schedule: ShipSchedule): Result<Unit> {
        val currentBerths = _berthsFlow.value.toMutableList()
        val index = currentBerths.indexOfFirst { it.id == berthId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Berço não encontrado com ID: $berthId"))
        }

        val berth = currentBerths[index]

        // RN001: Validação Anti-Sobreposição
        if (berth.hasOverlap(schedule.startDay, schedule.endDay)) {
            return Result.failure(
                IllegalStateException("RN001 - Conflito de Agenda! O ${berth.name} já possui outro navio agendado no período de dia ${schedule.startDay} a ${schedule.endDay}.")
            )
        }

        // RN002: Bloqueio Hidrodinâmico de Maré Baixa
        val hydrodynamicCheck = TideData.checkHydrodynamicSafety(
            berthMaxDraft = berth.maxDraft,
            shipDraft = schedule.draft,
            startDay = schedule.startDay,
            endDay = schedule.endDay
        )

        if (hydrodynamicCheck is HydrodynamicValidationResult.Invalid) {
            return Result.failure(IllegalStateException(hydrodynamicCheck.reason))
        }

        // Se passou em todas as regras, adiciona o navio
        val updatedSchedules = berth.schedules.toMutableList().apply { add(schedule) }
        val updatedBerth = berth.copy(schedules = updatedSchedules)
        currentBerths[index] = updatedBerth
        _berthsFlow.value = currentBerths

        return Result.success(Unit)
    }

    /**
     * Atualiza um agendamento existente de navio no berço.
     */
    fun updateSchedule(berthId: Long, schedule: ShipSchedule): Result<Unit> {
        val currentBerths = _berthsFlow.value.toMutableList()
        val index = currentBerths.indexOfFirst { it.id == berthId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Berço não encontrado com ID: $berthId"))
        }

        val berth = currentBerths[index]

        // RN001: Validação Anti-Sobreposição excluindo o próprio agendamento
        if (berth.hasOverlap(schedule.startDay, schedule.endDay, excludeScheduleId = schedule.id)) {
            return Result.failure(
                IllegalStateException("RN001 - Conflito de Agenda! O ${berth.name} já possui outro navio agendado no período de dia ${schedule.startDay} a ${schedule.endDay}.")
            )
        }

        // RN002: Bloqueio Hidrodinâmico de Maré Baixa
        val hydrodynamicCheck = TideData.checkHydrodynamicSafety(
            berthMaxDraft = berth.maxDraft,
            shipDraft = schedule.draft,
            startDay = schedule.startDay,
            endDay = schedule.endDay
        )

        if (hydrodynamicCheck is HydrodynamicValidationResult.Invalid) {
            return Result.failure(IllegalStateException(hydrodynamicCheck.reason))
        }

        val updatedSchedules = berth.schedules.map { if (it.id == schedule.id) schedule else it }.toMutableList()
        currentBerths[index] = berth.copy(schedules = updatedSchedules)
        _berthsFlow.value = currentBerths

        return Result.success(Unit)
    }

    /**
     * Remove um agendamento de navio do berço.
     */
    fun removeSchedule(berthId: Long, scheduleId: Long): Boolean {
        val currentBerths = _berthsFlow.value.toMutableList()
        val index = currentBerths.indexOfFirst { it.id == berthId }
        if (index == -1) return false

        val berth = currentBerths[index]
        val updatedSchedules = berth.schedules.filterNot { it.id == scheduleId }.toMutableList()
        currentBerths[index] = berth.copy(schedules = updatedSchedules)
        _berthsFlow.value = currentBerths
        return true
    }

    fun getNextScheduleId(): Long {
        val allSchedules = _berthsFlow.value.flatMap { it.schedules }
        return (allSchedules.maxOfOrNull { it.id } ?: 0L) + 1L
    }
}
