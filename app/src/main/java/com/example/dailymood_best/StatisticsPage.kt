package com.example.dailymood_best

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

// 心情分數對照表
val moodScoreMap = mapOf(
    "興奮" to 100,
    "開心" to 50,
    "平靜" to 0,
    "難過" to -50,
    "生氣" to -100
)

// 反查表 (用於顯示統計摘要)
val scoreMoodMap = moodScoreMap.entries.associate { (k, v) -> v to k }

// 心情顏色對照表 (圓餅圖用)
val moodColorMap = mapOf(
    100 to Color(0xFFFFB74D), // 興奮 (亮橘)
    50 to Color(0xFFFFCC80),  // 開心 (淡橘)
    0 to Color(0xFFA5D6A7),   // 平靜 (淡綠)
    -50 to Color(0xFF90CAF9), // 難過 (淡藍)
    -100 to Color(0xFFEF9A9A) // 生氣 (淡紅)
)

@Composable
fun StatisticsPage() {
    // 狀態：0 = 週檢視 (折線圖), 1 = 月檢視 (圓餅圖)
    var viewMode by remember { mutableIntStateOf(0) }
    // 基準日期 (預設今天)
    var baseDate by remember { mutableStateOf(LocalDate.now()) }

    // 根據檢視模式計算開始與結束日期
    val (startDate, endDate) = remember(baseDate, viewMode) {
        if (viewMode == 0) {
            // 週模式：週一 ~ 週日
            val start = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = baseDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            start to end
        } else {
            // 月模式：1號 ~ 月底
            val yearMonth = YearMonth.from(baseDate)
            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()
            start to end
        }
    }

    // 準備圖表數據
    val chartData = remember(startDate, endDate, diaryMap.toMap()) {
        val days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val entry = diaryMap[date]
            val score = entry?.let { moodScoreMap[it.mood] }
            date to score // Pair<LocalDate, Int?>
        }
    }

    // 計算統計數據
    val validScores = chartData.mapNotNull { it.second }
    val averageScore = if (validScores.isNotEmpty()) validScores.average().roundToInt() else 0
    val entryCount = validScores.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "心情趨勢分析 📈",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6B4C3B),
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
        )

        // 1. 切換按鈕 (週/月)
        SegmentedControl(
            selectedIndex = viewMode,
            items = listOf("本週趨勢 (折線)", "本月分佈 (圓餅)"),
            onValueChange = { viewMode = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 日期導航欄
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            IconButton(onClick = {
                baseDate = if (viewMode == 0) baseDate.minusWeeks(1) else baseDate.minusMonths(1)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF6B4C3B))
            }

            Text(
                text = if (viewMode == 0) {
                    val formatter = DateTimeFormatter.ofPattern("MM/dd")
                    "${startDate.format(formatter)} - ${endDate.format(formatter)}"
                } else {
                    "${startDate.year} 年 ${startDate.monthValue} 月"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            IconButton(onClick = {
                baseDate = if (viewMode == 0) baseDate.plusWeeks(1) else baseDate.plusMonths(1)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF6B4C3B))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 圖表卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.padding(20.dp).fillMaxSize()) {
                if (entryCount == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("這段時間沒有日記資料喔 ☁️", color = Color.Gray)
                    }
                } else {
                    if (viewMode == 0) {
                        MoodLineChart(data = chartData)
                    } else {
                        MoodPieChart(data = chartData)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 統計數據摘要
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "紀錄天數",
                value = "$entryCount 天",
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0F7FA),
                textColor = Color(0xFF006064)
            )

            // --- 修改開始 ---
            StatCard(
                title = "平均心情",
                // 原本是: value = "$averageScore 分", subText = getMoodDescription(averageScore)
                // 修改後: 直接把評語放在 value，並移除 subText
                value = getMoodDescription(averageScore),
                subText = null,
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFF3E0),
                textColor = Color(0xFFE65100)
            )
            // --- 修改結束 ---
        }
    }
}

fun getMoodDescription(score: Int): String {
    return when {
        score >= 80 -> "充滿活力！🤩"
        score >= 40 -> "心情不錯 😊"
        score >= -10 -> "平平淡淡 🍵"
        score >= -60 -> "有點低落 🌧️"
        else -> "需要抱抱 🫂"
    }
}

// ==========================================
// 圖表元件區
// ==========================================

// ==========================================
// 圖表元件區 (修改後)
// ==========================================

@Composable
fun MoodLineChart(data: List<Pair<LocalDate, Int?>>) {
    val gridColor = Color(0xFFE0E0E0) // 網格顏色變淡
    val lineColor = Color(0xFFFF8A65)
    val dotColor = Color(0xFFD84315)

    // Y 軸文字畫筆 (改成畫文字)
    val textPaintY = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8D6E63")
            textSize = 32f // 字體稍微大一點
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }
    val textPaintX = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val leftPadding = 80.dp.toPx() // 左邊留寬一點給文字
        val bottomPadding = 40.dp.toPx()

        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding

        fun getY(score: Int): Float {
            val normalized = (score + 100) / 200f
            return chartHeight * (1 - normalized)
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

        // 1. 畫背景網格與 Y 軸文字 (改成心情文字，且移除 0 的特殊線)
        // 定義要顯示的刻度與對應文字
        val levels = listOf(
            100 to "興奮",
            50 to "開心",
            0 to "平靜",
            -50 to "難過",
            -100 to "生氣"
        )

        levels.forEach { (score, label) ->
            val y = getY(score)
            // 畫文字
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftPadding - 20f,
                y + 10f,
                textPaintY
            )
            // 畫網格線 (全部統一樣式，沒有特殊 0 線)
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) // 虛線
            )
        }

        // 2. 準備曲線路徑 (Bézier Curve)
        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, pair ->
            val score = pair.second
            val x = leftPadding + (index * stepX)
            if (score != null) {
                val y = getY(score)
                points.add(Offset(x, y))
            }
        }

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)

            // 使用 cubicTo 繪製平滑曲線
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]

                // 控制點邏輯：X 取兩點中間，Y 維持水平，產生 S 型曲線
                val controlPoint1 = Offset((p1.x + p2.x) / 2, p1.y)
                val controlPoint2 = Offset((p1.x + p2.x) / 2, p2.y)

                path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
            }

            // 3. 畫線
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // 4. 畫漸層填充 (選用，讓畫面豐富一點)
            val fillPath = Path()
            fillPath.addPath(path)
            fillPath.lineTo(points.last().x, chartHeight)
            fillPath.lineTo(points.first().x, chartHeight)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = chartHeight
                )
            )
        }

        // 5. 畫圓點
        points.forEach { offset ->
            drawCircle(Color.White, radius = 12f, center = offset)
            drawCircle(dotColor, radius = 8f, center = offset)
        }

        // 6. 畫 X 軸日期
        val stepLabel = if (data.size <= 7) 1 else 5
        data.forEachIndexed { index, pair ->
            if (index % stepLabel == 0) {
                val label = "${pair.first.dayOfMonth}"
                val x = leftPadding + (index * stepX)
                drawContext.canvas.nativeCanvas.drawText(label, x, height - 5f, textPaintX)
            }
        }
    }
}

@Composable
fun MoodPieChart(data: List<Pair<LocalDate, Int?>>) {
    val scores = data.mapNotNull { it.second }
    val moodCounts = scores.groupingBy { it }.eachCount()
    val total = scores.size

    if (total == 0) return

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側：圓環圖
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    var startAngle = -90f
                    val strokeWidth = 30.dp.toPx()

                    moodCounts.keys.sortedDescending().forEach { score ->
                        val count = moodCounts[score] ?: 0
                        val sweepAngle = (count.toFloat() / total) * 360f
                        val color = moodColorMap[score] ?: Color.Gray

                        // 畫彩色圓弧
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweepAngle
                    }
                }

                // 中間 Total 文字
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", fontSize = 16.sp, color = Color.Gray)
                    Text("$total", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                    Text("Days", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // 右側：圖例 (文字區)
            Column(
                modifier = Modifier
                    .weight(0.8f) // 這裡控制寬度比例
                    .padding(start = 32.dp), // ★ 改這裡：原本是 8.dp，改成 32.dp 讓它往右移
                verticalArrangement = Arrangement.Center
            ) {
                moodCounts.keys.sortedDescending().forEach { score ->
                    val count = moodCounts[score] ?: 0
                    val percent = ((count.toFloat() / total) * 100).toInt()
                    val color = moodColorMap[score] ?: Color.Gray
                    val label = scoreMoodMap[score] ?: "未知"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$label $percent%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedControl(
    selectedIndex: Int,
    items: List<String>,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFD180).copy(alpha = 0.3f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF6B4C3B) else Color.Transparent)
                    .clickable { onValueChange(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else Color(0xFF6B4C3B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subText: String? = null,
    modifier: Modifier = Modifier,
    color: Color,
    textColor: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 14.sp, color = textColor.copy(alpha = 0.8f))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            if (subText != null) {
                Text(subText, fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))
            }
        }
    }
}