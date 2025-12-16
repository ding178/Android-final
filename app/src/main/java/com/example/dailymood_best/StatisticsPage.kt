package com.example.dailymood_best

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val moodScoreMap = mapOf("興奮" to 100, "開心" to 50, "平靜" to 0, "難過" to -50, "生氣" to -100)
val scoreMoodMap = moodScoreMap.entries.associate { (k, v) -> v to k }
val moodColorMap = mapOf(100 to Color(0xFFFFB74D), 50 to Color(0xFFFFCC80), 0 to Color(0xFFA5D6A7), -50 to Color(0xFF90CAF9), -100 to Color(0xFFEF9A9A))

@Composable
fun StatisticsPage() {
    var viewMode by remember { mutableIntStateOf(0) }
    var baseDate by remember { mutableStateOf(LocalDate.now()) }
    val scope = rememberCoroutineScope()

    val generativeModel = remember {
        GenerativeModel(modelName = "gemini-2.5-flash", apiKey = BuildConfig.API_KEY)
    }

    var aiComment by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val (startDate, endDate) = remember(baseDate, viewMode) {
        if (viewMode == 0) {
            val start = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = baseDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            start to end
        } else {
            val yearMonth = YearMonth.from(baseDate)
            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()
            start to end
        }
    }

    LaunchedEffect(startDate, endDate, viewMode) {
        aiComment = null
        isGenerating = false
    }

    val chartData = remember(startDate, endDate, diaryMap.toMap()) {
        val days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val entry = diaryMap[date]
            val score = entry?.let { moodScoreMap[it.mood] }
            date to score
        }
    }

    val validScores = chartData.mapNotNull { it.second }
    val entryCount = validScores.size
    val moodCounts = remember(validScores) { validScores.groupingBy { it }.eachCount() }
    val orderedScores = listOf(100, 50, 0, -50, -100)

    fun generateAiAnalysis() {
        if (validScores.isEmpty()) { aiComment = "這段時間還沒有紀錄喔，快去寫日記吧！🐨"; return }
        isGenerating = true
        scope.launch {
            val maxCount = moodCounts.values.maxOrNull() ?: 0
            val dominantScores = moodCounts.filter { it.value == maxCount }.keys.toList()
            val dominantMoodNames = dominantScores.mapNotNull { scoreMoodMap[it] }
            val moodString = dominantMoodNames.joinToString("與")
            val emojis = listOf("🐨", "✨", "💪", "🌈", "🎈", "🌻", "🍵", "🪁")

            try {
                val prompt = """
                    使用者在${if(viewMode==0) "本週" else "本月"}最常出現的心情是「$moodString」。
                    請以一隻溫暖、療癒的無尾熊口吻，針對這個主要心情給予一句簡短的評語或鼓勵。
                    規則：1. 繁體中文。2. 25個字以內。3. 生氣請給予安撫，開心請一起慶祝。
                """.trimIndent()
                val response = withContext(Dispatchers.IO) { generativeModel.generateContent(prompt) }
                val cleanText = response.text?.trim() ?: "無尾熊正在休息..."
                aiComment = "$cleanText ${emojis.random()}"
            } catch (e: Exception) {
                e.printStackTrace()
                aiComment = getLocalFallbackComment(dominantMoodNames) + " ${emojis.random()}"
            } finally { isGenerating = false }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0E0)),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SmoothEntranceAnim(delay = 0) {
                Text("心情趨勢分析 📈", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6B4C3B), modifier = Modifier.padding(bottom = 24.dp, top = 8.dp))
            }
        }
        item {
            SmoothEntranceAnim(delay = 50) {
                SegmentedControl(selectedIndex = viewMode, items = listOf("本週趨勢", "本月分佈"), onValueChange = { viewMode = it })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            SmoothEntranceAnim(delay = 100) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    IconButton(onClick = { baseDate = if (viewMode == 0) baseDate.minusWeeks(1) else baseDate.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF6B4C3B))
                    }
                    Text(
                        text = if (viewMode == 0) {
                            val formatter = DateTimeFormatter.ofPattern("MM/dd")
                            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
                        } else "${startDate.year} 年 ${startDate.monthValue} 月",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037)
                    )
                    IconButton(onClick = { baseDate = if (viewMode == 0) baseDate.plusWeeks(1) else baseDate.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF6B4C3B))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (entryCount == 0) {
            item {
                SmoothGraphCard(delay = 150) {
                    Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("這段時間沒有日記資料喔 ☁️", color = Color.Gray)
                    }
                }
            }
        } else {
            if (viewMode == 0) {
                item {
                    SmoothGraphCard(delay = 150) {
                        Box(modifier = Modifier.height(300.dp)) { MoodRadarChart(data = chartData) }
                    }
                }
            } else {
                item {
                    SmoothGraphCard(delay = 150) {
                        Box(modifier = Modifier.height(240.dp)) { MoodPieChart(data = chartData) }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SmoothLabel(text = "\uD83D\uDD78\uFE0F 心情雷達分析", delay = 200)
                }
                item {
                    SmoothGraphCard(delay = 250) {
                        Box(modifier = Modifier.height(300.dp)) { MoodRadarChart(data = chartData) }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { SmoothLabel(text = "📊 心情統計", delay = 0) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                orderedScores.forEachIndexed { index, score ->
                    val count = moodCounts[score] ?: 0
                    val color = moodColorMap[score] ?: Color.Gray
                    val name = scoreMoodMap[score] ?: ""
                    Box(modifier = Modifier.weight(1f)) {
                        SmoothMoodCountCard(delay = 50 + (index * 50), moodName = name, count = count, color = color)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Crossfade(targetState = aiComment, label = "AiAnalysis") { comment ->
                if (comment == null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color(0xFFFFB74D), shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp).then(if (!isGenerating && entryCount > 0) Modifier.bouncyClick { generateAiAnalysis() } else Modifier)
                        ) {
                            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("無尾熊思考中...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("聽聽無尾熊怎麼說", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    SmoothAiCommentCard(delay = 0, title = "✨ 無尾熊的心情小語", content = comment, containerColor = Color(0xFFFFF3E0), titleColor = Color(0xFFE65100), textColor = Color(0xFF5D4037))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun getLocalFallbackComment(moods: List<String>): String {
    return when {
        moods.contains("生氣") -> "深呼吸，把壞心情都吹走，你很棒的！"
        moods.contains("難過") -> "抱抱你，明天太陽依然會升起喔。"
        moods.contains("興奮") || moods.contains("開心") -> "太棒了！繼續保持這份閃亮的心情！"
        else -> "平平淡淡也是一種幸福，享受當下吧！"
    }
}

@Composable
fun MoodRadarChart(data: List<Pair<LocalDate, Int?>>) {
    val scores = data.mapNotNull { it.second }
    val counts = scores.groupingBy { it }.eachCount()
    val maxCount = (counts.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val moodOrder = listOf(100 to "興奮", 50 to "開心", 0 to "平靜", -50 to "難過", -100 to "生氣")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (min(size.width, size.height) / 2) * 0.75f
        val angleStep = 360f / 5
        val gridColor = Color(0xFFBCAAA4)
        val steps = 4
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        for (i in 1..steps) {
            val r = radius * (i / steps.toFloat())
            val path = Path()
            for (j in 0 until 5) {
                val angle = (angleStep * j - 90) * (Math.PI / 180)
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = gridColor.copy(alpha = 0.5f), style = Stroke(width = 2f, pathEffect = dashPathEffect))
        }
        for (j in 0 until 5) {
            val angle = (angleStep * j - 90) * (Math.PI / 180)
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            drawLine(gridColor.copy(alpha = 0.3f), center, Offset(x, y), strokeWidth = 2f)
        }

        val dataPath = Path()
        val pathPoints = mutableListOf<Offset>()
        moodOrder.forEachIndexed { index, (score, label) ->
            val count = counts[score] ?: 0
            val value = count.toFloat() / maxCount
            val r = radius * value
            val angle = (angleStep * index - 90) * (Math.PI / 180)
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            pathPoints.add(Offset(x, y))
        }
        dataPath.close()

        val gradientBrush = Brush.radialGradient(colors = listOf(Color(0xFFFFCC80).copy(alpha = 0.6f), Color(0xFFFF7043).copy(alpha = 0.4f)), center = center, radius = radius)
        drawPath(dataPath, brush = gradientBrush)
        val cornerEffect = PathEffect.cornerPathEffect(20f)
        drawPath(dataPath, color = Color(0xFFE64A19), style = Stroke(width = 6f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round, pathEffect = cornerEffect))
        pathPoints.forEach { offset ->
            drawCircle(Color.White, radius = 9f, center = offset)
            drawCircle(Color(0xFFE64A19), radius = 9f, center = offset, style = Stroke(width = 3f))
        }
        val textPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#5D4037"); textSize = 36f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
        moodOrder.forEachIndexed { index, (score, label) ->
            val angle = (angleStep * index - 90) * (Math.PI / 180)
            val labelRadius = radius * 1.25f
            val x = center.x + labelRadius * cos(angle).toFloat()
            val y = center.y + labelRadius * sin(angle).toFloat() + 12f
            drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
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
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1.2f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    var startAngle = -90f
                    val strokeWidth = 25.dp.toPx()
                    moodCounts.keys.sortedDescending().forEach { score ->
                        val count = moodCounts[score] ?: 0
                        val sweepAngle = (count.toFloat() / total) * 360f
                        val color = moodColorMap[score] ?: Color.Gray
                        drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth))
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", fontSize = 14.sp, color = Color.Gray); Text("$total", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037)); Text("Days", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(modifier = Modifier.weight(0.8f).padding(start = 32.dp), verticalArrangement = Arrangement.Center) {
                moodCounts.keys.sortedDescending().forEach { score ->
                    val count = moodCounts[score] ?: 0
                    val percent = ((count.toFloat() / total) * 100).toInt()
                    val color = moodColorMap[score] ?: Color.Gray
                    val label = scoreMoodMap[score] ?: "未知"
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(color, CircleShape)); Spacer(modifier = Modifier.width(8.dp)); Text(text = "$label $percent%", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5D4037))
                    }
                }
            }
        }
    }
}

@Composable
fun SmoothGraphCard(delay: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SmoothEntranceAnim(delay = delay, modifier = modifier) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
            Box(modifier = Modifier.padding(20.dp).fillMaxWidth()) { content() }
        }
    }
}
@Composable
fun SmoothMoodCountCard(delay: Int, moodName: String, count: Int, color: Color) {
    SmoothEntranceAnim(delay = delay) {
        Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.75f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = moodName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$count", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D4037))
                Text(text = "天", fontSize = 10.sp, color = Color(0xFF5D4037).copy(alpha = 0.8f))
            }
        }
    }
}
@Composable
fun SmoothAiCommentCard(delay: Int, title: String, content: String, modifier: Modifier = Modifier, containerColor: Color, titleColor: Color, textColor: Color) {
    SmoothEntranceAnim(delay = delay, modifier = modifier) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontSize = 14.sp, color = titleColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), shadowElevation = 0.dp) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = content, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textColor, textAlign = TextAlign.Center, lineHeight = 28.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun SmoothLabel(text: String, delay: Int) {
    SmoothEntranceAnim(delay = delay) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), modifier = Modifier.padding(bottom = 12.dp, start = 8.dp).fillMaxWidth())
    }
}
@Composable
fun SegmentedControl(selectedIndex: Int, items: List<String>, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFFFD180).copy(alpha = 0.3f)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        items.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (isSelected) Color(0xFF6B4C3B) else Color.Transparent).clickable { onValueChange(index) }, contentAlignment = Alignment.Center) {
                Text(text = text, color = if (isSelected) Color.White else Color(0xFF6B4C3B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ==========================================
// 本頁專用的動畫工具
// ==========================================
@Composable
private fun SmoothEntranceAnim(delay: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delay.toLong()); isVisible = true }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(500))
    val offsetY by animateFloatAsState(targetValue = if (isVisible) 0f else 100f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    Box(modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha; translationY = offsetY }) { content() }
}
private fun Modifier.bouncyClick(scaleDown: Float = 0.92f, onClick: () -> Unit): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) scaleDown else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    this.graphicsLayer { scaleX = scale; scaleY = scale }.pointerInput(Unit) { detectTapGestures(onPress = { isPressed = true; tryAwaitRelease(); isPressed = false; onClick() }) }
}