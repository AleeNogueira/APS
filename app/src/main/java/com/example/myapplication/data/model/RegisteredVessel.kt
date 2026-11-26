package com.example.myapplication.data.model

/**
 * RF007 / RN007 - Modelo de Dados da Frota de Navios Mercantes e Códigos IMO do Porto de Santos.
 */
data class RegisteredVessel(
    val imoCode: String,            // Chave primária no formato "IMO-xxxxxxx" (ex: "IMO-9810100")
    val shipName: String,           // Nome do navio (ex: "MV Atlantic Star")
    val vesselClass: String,        // Classe da embarcação (ex: "Panamax Graneleiro")
    val maxDraft: Double,           // Calado máximo operacional em metros (ex: 14.2)
    val standardTonnage: Double,    // Tonelagem padrão de operação em toneladas (ex: 68500.0)
    val typicalImportCargo: String, // Carga típica de descarga (ex: "Trigo Importação")
    val typicalExportCargo: String  // Carga típica de carregamento (ex: "Soja em Grãos")
)
