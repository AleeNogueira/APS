package com.example.myapplication.data.model

import androidx.compose.ui.graphics.Color

enum class CargoCategory(
    val label: String,
    val colorHex: String,
    val composeColor: Color
) {
    SOLIDOS_VEGETAIS("Granéis Sólidos Vegetais", "#27AE60", Color(0xFF27AE60)),
    CONTEINERES("Contêineres", "#2980B9", Color(0xFF2980B9)),
    CELULOSE("Celulose & Papel", "#8E44AD", Color(0xFF8E44AD)),
    LIQUIDOS("Granéis Líquidos", "#E67E22", Color(0xFFE67E22)),
    SUCOS("Sucos Cítricos", "#F39C12", Color(0xFFF39C12)),
    CARGA_GERAL("Carga Geral & Projetos", "#16A085", Color(0xFF16A085)),
    PASSAGEIROS("Passageiros / Cruzeiros", "#E74C3C", Color(0xFFE74C3C)),
    OFFSHORE("Apoio Offshore", "#34495E", Color(0xFF34495E)),
    SOLIDOS_MINERAIS("Granéis Sólidos Minerais", "#7F8C8D", Color(0xFF7F8C8D)),

    // Aliases mantidos para compatibilidade retroativa
    VEGETABLE_BULK("Granéis Sólidos Vegetais", "#27AE60", Color(0xFF27AE60)),
    CONTAINERS("Contêineres", "#2980B9", Color(0xFF2980B9)),
    PULP("Celulose & Papel", "#8E44AD", Color(0xFF8E44AD)),
    LIQUID_BULK("Granéis Líquidos", "#E67E22", Color(0xFFE67E22)),
    GENERAL_CARGO("Carga Geral & Projetos", "#16A085", Color(0xFF16A085)),
    MINERAL_BULK("Granéis Sólidos Minerais", "#7F8C8D", Color(0xFF7F8C8D))
}
