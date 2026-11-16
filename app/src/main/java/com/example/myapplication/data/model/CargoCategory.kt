package com.example.myapplication.data.model

import androidx.compose.ui.graphics.Color

enum class CargoCategory(
    val label: String,
    val colorHex: String,
    val composeColor: Color
) {
    VEGETABLE_BULK("Granéis Sólidos Vegetais", "#27AE60", Color(0xFF27AE60)),
    CONTAINERS("Contêineres", "#2980B9", Color(0xFF2980B9)),
    PULP("Celulose", "#8E44AD", Color(0xFF8E44AD)),
    LIQUID_BULK("Granéis Líquidos", "#E67E22", Color(0xFFE67E22)),
    GENERAL_CARGO("Carga Geral", "#16A085", Color(0xFF16A085)),
    MINERAL_BULK("Granéis Sólidos Minerais", "#7F8C8D", Color(0xFF7F8C8D))
}
