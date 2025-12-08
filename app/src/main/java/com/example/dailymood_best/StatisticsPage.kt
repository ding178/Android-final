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
            StatCard(
                title = "平均心情",
                value = "$averageScore 分",
                subText = getMoodDescription(averageScore),
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFF3E0),
                textColor = Color(0xFFE65100)
            )
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

@Composable
fun MoodLineChart(data: List<Pair<LocalDate, Int?>>) {
    val gridColor = Color(0xFFF5F5F5)
    val lineColor = Color(0xFFFF8A65)
    val dotColor = Color(0xFFD84315)
    val zeroLineColor = Color(0xFF81D4FA)

    val textPaintY = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8D6E63")
            textSize = 30f
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
        val leftPadding = 50.dp.toPx()
        val bottomPadding = 40.dp.toPx()

        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding

        fun getY(score: Int): Float {
            val normalized = (score + 100) / 200f
            return chartHeight * (1 - normalized)
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

        // 1. 畫背景與 Y 軸
        val levels = listOf(100, 50, 0, -50, -100)
        levels.forEach { level ->
            val y = getY(level)
            drawContext.canvas.nativeCanvas.drawText(
                level.toString(),
                leftPadding - 15f,
                y + 10f,
                textPaintY
            )
            drawLine(
                color = if (level == 0) zeroLineColor else gridColor,
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = if (level == 0) 4f else 2f,
                pathEffect = if (level == 0) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }

        // 2. 準備路徑
        val path = Path()
        var firstPoint = true
        var lastX = 0f
        var lastY = 0f // 雖然沒用到，但保留結構
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, pair ->
            val score = pair.second
            val x = leftPadding + (index * stepX)
            if (score != null) {
                val y = getY(score)
                points.add(Offset(x, y))

                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                } else {
                    // 【修改重點】這裡改成 lineTo 變成直線
                    path.lineTo(x, y)
                }
                lastX = x
                lastY = y
            }
        }

        // 3. 畫線與漸層
        if (points.isNotEmpty()) {
            drawPath(path = path, color = lineColor, style = Stroke(width = 6f))

            // 漸層填充也要跟著改
            val fillPath = Path()
            fillPath.addPath(path)
            fillPath.lineTo(lastX, getY(-100))
            fillPath.lineTo(points.first().x, getY(-100))
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.0f)),
                    startY = 0f,
                    endY = chartHeight
                )
            )
        }

        // 4. 畫圓點
        points.forEach { offset ->
            drawCircle(Color.White, radius = 10f, center = offset)
            drawCircle(dotColor, radius = 7f, center = offset)
        }

        // 5. 畫 X 軸
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

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1.2f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                var startAngle = -90f
                moodCounts.keys.sortedDescending().forEach { score ->
                    val count = moodCounts[score] ?: 0
                    val sweepAngle = (count.toFloat() / total) * 360f
                    val color = moodColorMap[score] ?: Color.Gray

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Stroke(width = 3f)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(0.8f)
                .padding(start = 8.dp),
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