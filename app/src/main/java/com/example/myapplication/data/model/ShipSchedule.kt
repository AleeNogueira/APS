package com.example.myapplication.data.model

data class ShipSchedule(
    val id: Long,
    val shipName: String,
    val cargoName: String,
    val tonnage: Double,
    val startDay: Int,
    val endDay: Int,
    val draft: Double, // Calado do navio em metros
    val status: MooringStatus
) {
    init {
        require(startDay in 1..30) { "startDay deve estar entre 1 e 30" }
        require(endDay in 1..30) { "endDay deve estar entre 1 e 30" }
        require(startDay <= endDay) { "startDay não pode ser maior que endDay" }
    }
}
