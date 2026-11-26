package com.example.myapplication.data.remote

import com.example.myapplication.data.model.CargoCategory
import com.example.myapplication.data.model.PortCargoCatalogItem
import com.example.myapplication.data.model.RegisteredVessel
import com.example.myapplication.data.model.TideData
import com.example.myapplication.data.model.TideMeasurement
import com.example.myapplication.data.model.TideTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.math.sin

/**
 * Cliente API REST do Gêmeo Digital do Porto de Santos.
 */
object BackendApiClient {

    // 1. Perfis de Carga (/api/port/cargos)
    private val mockCargosDatabase: MutableList<PortCargoCatalogItem> = Collections.synchronizedList(
        mutableListOf(
            PortCargoCatalogItem("CRG-01", "Soja em Grãos (Exportação)", "↑ Carga", 25000.0, CargoCategory.SOLIDOS_VEGETAIS),
            PortCargoCatalogItem("CRG-02", "Milho (Exportação)", "↑ Carga", 22000.0, CargoCategory.SOLIDOS_VEGETAIS),
            PortCargoCatalogItem("CRG-03", "Açúcar VHP (Exportação em Granel)", "↑ Carga", 20000.0, CargoCategory.SOLIDOS_VEGETAIS),
            PortCargoCatalogItem("CRG-04", "Trigo (Importação)", "↓ Descarga", 12000.0, CargoCategory.SOLIDOS_VEGETAIS),
            PortCargoCatalogItem("CRG-05", "Fertilizantes NPK (Importação)", "↓ Descarga", 10000.0, CargoCategory.SOLIDOS_MINERAIS),
            PortCargoCatalogItem("CRG-06", "Contêineres Reefer & Dry (TECON)", "↓↑ Descarga & Carga", 18000.0, CargoCategory.CONTEINERES),
            PortCargoCatalogItem("CRG-07", "Celulose em Fardos", "↑ Carga", 15000.0, CargoCategory.CELULOSE),
            PortCargoCatalogItem("CRG-08", "Óleo Vegetal & Etanol (Granel Líquido)", "↓↑ Descarga & Carga", 14000.0, CargoCategory.LIQUIDOS),
            PortCargoCatalogItem("CRG-09", "Suco de Laranja Concentrado (FCOJ)", "↑ Carga", 8000.0, CargoCategory.SUCOS),
            PortCargoCatalogItem("CRG-10", "Carga Geral & Equipamentos Eólicos", "↓↑ Descarga & Carga", 6000.0, CargoCategory.CARGA_GERAL),
            PortCargoCatalogItem("CRG-11", "Navios de Cruzeiro & Passageiros (Concais)", "↓↑ Descarga & Carga", 2500.0, CargoCategory.PASSAGEIROS),
            PortCargoCatalogItem("CRG-12", "Apoio Marítimo & Subsea Offshore", "↓↑ Descarga & Carga", 4000.0, CargoCategory.OFFSHORE)
        )
    )

    // 2. Frota de Navios Mercantes (/api/port/vessels)
    private val mockVesselsDatabase: MutableList<RegisteredVessel> = Collections.synchronizedList(
        generateMockVessels()
    )

    // 3. Tábuas de Marés Astronômicas (/api/port/tides/*)
    private val mockTideTablesDatabase: MutableList<TideTable> = Collections.synchronizedList(
        generateMockTideTables()
    )

    init {
        mockTideTablesDatabase.find { it.isActive }?.let { active ->
            TideData.setActiveTable(active)
        }
    }

    private fun generateMockVessels(): MutableList<RegisteredVessel> {
        val vesselClasses = listOf(
            "Panamax Graneleiro", "Kamsarmax Bulk Carrier", "Capesize Bulk Carrier",
            "Post-Panamax Contêineres", "Aframax Petroleiro", "Suezmax Tanque",
            "Navio de Cruzeiro", "Apoio Offshore (PSV)"
        )

        val prefixes = listOf("MV", "MS", "MT", "CMA CGM", "Maersk", "MSC", "Hapag-Lloyd", "ONE", "COSCO", "Evergreen", "Pacific", "Oceanic")
        val vesselNames = listOf(
            "Santos Express", "Atlantic Star", "Paranaguá Glory", "Vanguard Titan", "Amazon Horizon",
            "Pacifica Leader", "Cap San Lorenzo", "Meridian Pioneer", "Polaris Victory", "Navegante I",
            "Suape Fortune", "Itaqui Monarch", "Rio Breeze", "Guanabara Star", "Brazuca Giant"
        )

        val imports = listOf("Trigo Importação", "Fertilizantes NPK", "Contêineres Dry", "Óleo Cruto", "Bauxita", "Cloreto de Potássio")
        val exports = listOf("Soja em Grãos", "Milho Exportação", "Açúcar VHP", "Fardos de Celulose", "Suco de Laranja (FCOJ)", "Etanol")

        val list = mutableListOf<RegisteredVessel>()

        for (i in 1..153) {
            val imoNum = 9800000 + i * 3 + (i % 7)
            val imoCode = "IMO-%07d".format(imoNum)
            val vesselClass = vesselClasses[(i - 1) % vesselClasses.size]
            val prefix = prefixes[(i - 1) % prefixes.size]
            val name = vesselNames[(i - 1) % vesselNames.size]
            val shipName = "$prefix $name ${if (i > 15) i else ""}".trim()

            val maxDraft = 10.5 + ((i * 3) % 45) * 0.1
            val standardTonnage = 25000.0 + ((i * 1700) % 55000)
            val importCargo = imports[(i - 1) % imports.size]
            val exportCargo = exports[(i - 1) % exports.size]

            list.add(
                RegisteredVessel(
                    imoCode = imoCode,
                    shipName = shipName,
                    vesselClass = vesselClass,
                    maxDraft = Math.round(maxDraft * 10.0) / 10.0,
                    standardTonnage = Math.round(standardTonnage * 10.0) / 10.0,
                    typicalImportCargo = importCargo,
                    typicalExportCargo = exportCargo
                )
            )
        }
        return list
    }

    private fun generateMockTideTables(): MutableList<TideTable> {
        val tables = mutableListOf<TideTable>()

        val months = listOf(
            Pair(1L, "Novembro / 2026"),
            Pair(2L, "Setembro / 2026"),
            Pair(3L, "Junho / 2026")
        )

        var measurementIdCounter = 1L

        months.forEachIndexed { monthIndex, (tableId, monthKey) ->
            val isActive = monthIndex == 0
            val measurementsList = mutableListOf<TideMeasurement>()
            val phaseShift = monthIndex * 2.5

            for (day in 1..30) {
                for (slot in 0 until 48) {
                    val timeInHours = slot * 0.5
                    val dayPhase = (day - 1 + phaseShift) * (2 * Math.PI / 14.76)
                    val tideOscillation = sin(2 * Math.PI * timeInHours / 12.42)
                    val amplitude = 1.25 + 0.75 * sin(dayPhase)
                    val baseLevel = 2.15

                    var height = baseLevel + amplitude * tideOscillation
                    if (monthIndex == 0 && (day in 7..9 || day in 21..23) && slot in 18..28) {
                        height -= 1.4
                    }

                    height = (Math.round(height * 100.0) / 100.0).coerceAtLeast(0.3)

                    measurementsList.add(
                        TideMeasurement(
                            id = measurementIdCounter++,
                            dayNumber = day,
                            timeSlot = slot,
                            height = height
                        )
                    )
                }
            }

            tables.add(
                TideTable(
                    id = tableId,
                    monthKey = monthKey,
                    isActive = isActive,
                    measurements = measurementsList
                )
            )
        }

        return tables
    }

    // --- ENDPOINTS /api/port/cargos ---

    suspend fun fetchPortCargos(): List<PortCargoCatalogItem> = withContext(Dispatchers.IO) {
        delay(150)
        synchronized(mockCargosDatabase) { mockCargosDatabase.toList() }
    }

    suspend fun addPortCargo(item: PortCargoCatalogItem): Boolean = withContext(Dispatchers.IO) {
        delay(200)
        synchronized(mockCargosDatabase) {
            if (mockCargosDatabase.any { it.productCode.equals(item.productCode, ignoreCase = true) }) {
                throw IllegalArgumentException("Já existe um produto cadastrado com o código ${item.productCode}.")
            }
            mockCargosDatabase.add(item)
            true
        }
    }

    suspend fun updatePortCargo(item: PortCargoCatalogItem): Boolean = withContext(Dispatchers.IO) {
        delay(200)
        synchronized(mockCargosDatabase) {
            val idx = mockCargosDatabase.indexOfFirst { it.productCode.equals(item.productCode, ignoreCase = true) }
            if (idx == -1) {
                throw NoSuchElementException("Produto ${item.productCode} não foi encontrado para atualização.")
            }
            mockCargosDatabase[idx] = item
            true
        }
    }

    suspend fun deletePortCargo(productCode: String): Boolean = withContext(Dispatchers.IO) {
        delay(150)
        synchronized(mockCargosDatabase) {
            val removed = mockCargosDatabase.removeIf { it.productCode.equals(productCode, ignoreCase = true) }
            if (!removed) {
                throw NoSuchElementException("Produto $productCode não encontrado.")
            }
            true
        }
    }

    // --- ENDPOINTS /api/port/vessels (RF007 / RN007) ---

    suspend fun fetchRegisteredVessels(): List<RegisteredVessel> = withContext(Dispatchers.IO) {
        delay(200)
        synchronized(mockVesselsDatabase) { mockVesselsDatabase.toList() }
    }

    suspend fun addRegisteredVessel(vessel: RegisteredVessel): Boolean = withContext(Dispatchers.IO) {
        delay(250)
        synchronized(mockVesselsDatabase) {
            if (mockVesselsDatabase.any { it.imoCode.equals(vessel.imoCode, ignoreCase = true) }) {
                throw IllegalArgumentException("RN007: Já existe um navio registrado com o Código IMO ${vessel.imoCode}.")
            }
            mockVesselsDatabase.add(0, vessel)
            true
        }
    }

    suspend fun updateRegisteredVessel(vessel: RegisteredVessel): Boolean = withContext(Dispatchers.IO) {
        delay(250)
        synchronized(mockVesselsDatabase) {
            val idx = mockVesselsDatabase.indexOfFirst { it.imoCode.equals(vessel.imoCode, ignoreCase = true) }
            if (idx == -1) {
                throw NoSuchElementException("Navio com IMO ${vessel.imoCode} não foi encontrado no cadastro.")
            }
            mockVesselsDatabase[idx] = vessel
            true
        }
    }

    suspend fun deleteRegisteredVessel(imoCode: String): Boolean = withContext(Dispatchers.IO) {
        delay(200)
        synchronized(mockVesselsDatabase) {
            val removed = mockVesselsDatabase.removeIf { it.imoCode.equals(imoCode, ignoreCase = true) }
            if (!removed) {
                throw NoSuchElementException("Navio com Código IMO $imoCode não encontrado no servidor.")
            }
            true
        }
    }

    // --- ENDPOINTS /api/port/tides/* (RF008 / RN002 / RN008) ---

    suspend fun fetchTideTables(): List<TideTable> = withContext(Dispatchers.IO) {
        delay(200)
        synchronized(mockTideTablesDatabase) { mockTideTablesDatabase.toList() }
    }

    suspend fun activateTideTable(tableId: Long): TideTable = withContext(Dispatchers.IO) {
        delay(250)
        synchronized(mockTideTablesDatabase) {
            val targetIdx = mockTideTablesDatabase.indexOfFirst { it.id == tableId }
            if (targetIdx == -1) {
                throw NoSuchElementException("Tábua de Marés não encontrada para o ID $tableId.")
            }

            for (i in mockTideTablesDatabase.indices) {
                val table = mockTideTablesDatabase[i]
                val isTarget = table.id == tableId
                mockTideTablesDatabase[i] = table.copy(isActive = isTarget)
            }

            val activatedTable = mockTideTablesDatabase[targetIdx]
            TideData.setActiveTable(activatedTable)
            activatedTable
        }
    }
}
