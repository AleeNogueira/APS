package com.example.myapplication.data.model

/**
 * RF006 - Modelo de Dados do Perfil de Carga Portuária (Porto de Santos)
 */
data class PortCargoCatalogItem(
    val productCode: String,      // Código único do produto (ex: "CRG-01")
    val productName: String,      // Nome da mercadoria (ex: "Soja em Grãos (Exportação)")
    val operationType: String,    // Tipo de operação ("↓ Descarga", "↑ Carga" ou "↓↑ Descarga & Carga")
    val pranchaRateTpd: Double,   // Taxa nominal diária em t/dia (Prancha Média)
    val category: CargoCategory   // Categoria da carga com badge e cor associada
)
