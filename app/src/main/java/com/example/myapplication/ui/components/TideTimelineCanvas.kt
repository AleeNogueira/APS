package com.example.myapplication.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Berth
import com.example.myapplication.data.model.ShipSchedule
import com.example.myapplication.data.model.TideData

@Composable
fun TideTimelineCanvas(
    berth: Berth,
    modifier: Modifier = Modifier,
    onShipClick: ((ShipSchedule) -> Unit)? = null
) {
    // Reutilização de objetos de texto do Canvas para evitar alocações dentro de 60 FPS
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 28f
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val subTextPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 22f
            color = android.graphics.Color.argb(220, 255, 255, 255)
        }
    }

    val dayLabelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 22f
            color = android.graphics.Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }
    }

    val tideWarningPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 20f
            color = android.graphics.Color.RED
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val canvasHeight = 160.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .pointerInput(berth.schedules) {
                detectTapGestures { tapOffset ->
                    val totalWidth = size.width
                    val dayWidth = totalWidth / 30f
                    val shipTopY = 32.dp.toPx()
                    val shipHeightY = 64.dp.toPx()

                    if (tapOffset.y in shipTopY..(shipTopY + shipHeightY)) {
                        val tappedDay = ((tapOffset.x / dayWidth).toInt() + 1).coerceIn(1, 30)
                        val hitShip = berth.schedules.find { schedule ->
                            tappedDay in schedule.startDay..schedule.endDay
                        }
                        if (hitShip != null && onShipClick != null) {
                            onShipClick(hitShip)
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val totalDays = 30
        val dayWidth = width / totalDays.toFloat()

        val headerHeight = 28.dp.toPx()
        val shipTop = 32.dp.toPx()
        val shipHeight = 62.dp.toPx()
        val tideGraphTop = 100.dp.toPx()
        val tideGraphHeight = height - tideGraphTop - 8.dp.toPx()

        // 1. Sombreamento visual para dias com maré baixa crítica (RN002)
        for (day in 1..totalDays) {
            val isCritical = TideData.isCriticalLowTideDay(day)
            val left = (day - 1) * dayWidth

            if (isCritical) {
                drawRect(
                    color = Color(0x2BEE5253), // Sombreamento avermelhado transparente
                    topLeft = Offset(left, headerHeight),
                    size = Size(dayWidth, height - headerHeight)
                )

                // Alerta no topo do dia crítico
                drawContext.canvas.nativeCanvas.drawText(
                    "⚠️",
                    left + dayWidth / 2f,
                    headerHeight - 4f,
                    tideWarningPaint
                )
            }

            // Linhas verticais separadoras dos dias
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(left, 0f),
                end = Offset(left, height),
                strokeWidth = 1f
            )

            // Rótulo numérico dos dias (1 a 30)
            if (day == 1 || day % 2 != 0 || day == 30) {
                drawContext.canvas.nativeCanvas.drawText(
                    day.toString(),
                    left + dayWidth / 2f,
                    headerHeight - 8f,
                    dayLabelPaint
                )
            }
        }

        // Linha base inferior
        drawLine(
            color = Color(0xFFB0BEC5),
            start = Offset(0f, headerHeight),
            end = Offset(width, headerHeight),
            strokeWidth = 1.5f
        )

        // 2. Curva astronômica de marés (48 medições/dia) renderizada com Path suavizado
        val tidePath = Path()
        var firstPoint = true

        val minTideValue = 0.0
        val maxTideValue = 4.2

        for (day in 1..totalDays) {
            val measurements = TideData.dailyTideMeasurements[day] ?: emptyList()
            val stepWidth = dayWidth / measurements.size.toFloat()

            for ((stepIndex, tideHeightValue) in measurements.withIndex()) {
                val x = (day - 1) * dayWidth + stepIndex * stepWidth
                // Inverte Y para que maré alta fique mais no topo e maré baixa mais embaixo
                val normalizedTide = ((tideHeightValue - minTideValue) / (maxTideValue - minTideValue)).toFloat()
                val y = tideGraphTop + tideGraphHeight * (1f - normalizedTide)

                if (firstPoint) {
                    tidePath.moveTo(x, y)
                    firstPoint = false
                } else {
                    tidePath.lineTo(x, y)
                }
            }
        }

        // Desenha a linha da curva de maré
        drawPath(
            path = tidePath,
            color = Color(0xFF0288D1), // Azul marinho fluído
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Linha indicadora do nível crítico de maré baixa (1.1m)
        val criticalY = tideGraphTop + tideGraphHeight * (1f - ((TideData.CRITICAL_LOW_TIDE_THRESHOLD - minTideValue) / (maxTideValue - minTideValue)).toFloat())
        drawLine(
            color = Color(0x77FF0000),
            start = Offset(0f, criticalY),
            end = Offset(width, criticalY),
            strokeWidth = 1.5f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // 3. Renderização dos Cascos/Faixas dos Navios atracados/programados (RN005 - Cores)
        berth.schedules.forEach { schedule ->
            val shipLeft = (schedule.startDay - 1) * dayWidth + 2f
            val shipRight = schedule.endDay * dayWidth - 2f
            val shipWidth = (shipRight - shipLeft).coerceAtLeast(10f)

            val statusColor = schedule.status.composeColor

            // Desenha a faixa do navio com cantos arredondados (Formato de casco marinho)
            drawRoundRect(
                color = statusColor,
                topLeft = Offset(shipLeft, shipTop),
                size = Size(shipWidth, shipHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Borda do casco
            drawRoundRect(
                color = Color(0x44000000),
                topLeft = Offset(shipLeft, shipTop),
                size = Size(shipWidth, shipHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Texto interno (Nome do navio + calado/tonelagem) se houver espaço na faixa
            if (shipWidth > 40f) {
                val shipNameText = schedule.shipName
                val infoText = "${schedule.cargoName} • Calado: ${schedule.draft}m"

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.clipRect(
                    shipLeft + 6f,
                    shipTop + 2f,
                    shipRight - 6f,
                    shipTop + shipHeight - 2f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    shipNameText,
                    shipLeft + 8f,
                    shipTop + 26f,
                    textPaint
                )

                drawContext.canvas.nativeCanvas.drawText(
                    infoText,
                    shipLeft + 8f,
                    shipTop + 52f,
                    subTextPaint
                )

                drawContext.canvas.nativeCanvas.restore()
            }
        }
    }
}
