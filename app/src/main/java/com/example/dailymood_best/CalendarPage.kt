package com.example.dailymood_best

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun CalendarPage(onEditDate: (LocalDate) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value

    // 心情顏色對照表
    fun getMoodColor(mood: String): Color {
        return when (mood) {
            "開心" -> Color(0xFFFF80AB) // 粉紅
            "難過" -> Color(0xFF4FC3F7) // 淺藍
            "生氣" -> Color(0xFFD32F2F) // 深紅
            "興奮" -> Color(0xFFFF9800) // 橘色
            "平靜" -> Color(0xFF4CAF50) // 綠色
            else -> Color.Gray
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 背景圖片
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        // 2. 日曆內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 標題區 (Delay: 0ms)
            SmoothEntranceAnim(delay = 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .bouncyClick(scaleDown = 0.95f) { showDatePicker = true }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5D4037),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("▼", fontSize = 14.sp, color = Color(0xFF8D6E63))
                }
            }

            // 星期標題 (Delay: 50ms)
            SmoothEntranceAnim(delay = 50) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                        Text(
                            text = it,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 日曆網格 (Delay: 100ms)
            SmoothEntranceAnim(delay = 100) {
                val totalCells = daysInMonth + firstDayOfMonth - 1
                val weeks = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1
                var day = 1

                Column {
                    for (week in 0 until weeks) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (dow in 1..7) {
                                val cellIndex = week * 7 + dow
                                if (cellIndex < firstDayOfMonth || day > daysInMonth) {
                                    Box(modifier = Modifier.size(45.dp))
                                } else {
                                    val date = currentMonth.atDay(day)
                                    val isSelected = date == selectedDate
                                    val isToday = date == LocalDate.now()
                                    val entry = diaryMap[date]

                                    // 日期格子
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isSelected -> Color(0xFF6B4C3B)
                                                    isToday -> Color(0xFFFFCCBC)
                                                    else -> Color.White.copy(alpha = 0.9f)
                                                }
                                            )
                                            .bouncyClick(scaleDown = 0.85f) { selectedDate = date },
                                    ) {
                                        // 日期數字
                                        Text(
                                            text = day.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFF5D4037),
                                            modifier = Modifier.align(Alignment.Center)
                                        )

                                        // 心情圓點
                                        if (entry != null) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 6.dp)
                                                    .size(6.dp)
                                                    .background(
                                                        color = getMoodColor(entry.mood),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                    day++
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 下方顯示區 (Delay: 200ms)
            SmoothEntranceAnim(delay = 200) {
                val selectedEntry = diaryMap[selectedDate]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "日期：$selectedDate",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )

                            val isFuture = selectedDate.isAfter(LocalDate.now())

                            // 使用 Surface 自製按鈕以支援 bouncyClick
                            Surface(
                                color = if (isFuture) Color.LightGray else Color(0xFFFFCCBC),
                                shape = RoundedCornerShape(18.dp), // 全圓角
                                modifier = Modifier
                                    .height(36.dp)
                                    .then(if (!isFuture) Modifier.bouncyClick { onEditDate(selectedDate) } else Modifier)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        "修改 ✎",
                                        color = if (isFuture) Color.White else Color(0xFF5D4037),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFE6D6))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "心情：",
                                fontSize = 18.sp,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Medium
                            )

                            if (selectedEntry != null) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(getMoodColor(selectedEntry.mood), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = selectedEntry.mood,
                                    fontSize = 18.sp,
                                    color = Color(0xFF5D4037),
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "尚未記錄",
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Text(
                            "日記：${selectedEntry?.diary ?: "今天沒有寫日記喔～"}",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF8D6E63)
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        MonthYearPickerDialog(
            initialYearMonth = currentMonth,
            onDismiss = { showDatePicker = false },
            onConfirm = { newYearMonth ->
                currentMonth = newYearMonth
                showDatePicker = false
                selectedDate = newYearMonth.atDay(1)
            }
        )
    }
}

// 保持 MonthYearPickerDialog 不變...
@Composable
fun MonthYearPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectingYear by remember { mutableIntStateOf(initialYearMonth.year) }
    Dialog(onDismissRequest = onDismiss) {
        // Dialog 也可以加個動畫
        SmoothEntranceAnim(delay = 0) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        IconButton(onClick = { selectingYear-- }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF6B4C3B)) }
                        Text("$selectingYear", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B4C3B))
                        IconButton(onClick = { selectingYear++ }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF6B4C3B)) }
                    }
                    HorizontalDivider(color = Color(0xFFFFE0B2), modifier = Modifier.padding(bottom = 16.dp))
                    val months = listOf("JAN" to 1, "FEB" to 2, "MAR" to 3, "APR" to 4, "MAY" to 5, "JUN" to 6, "JUL" to 7, "AUG" to 8, "SEP" to 9, "OCT" to 10, "NOV" to 11, "DEC" to 12)
                    Column {
                        for (row in 0 until 4) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                for (col in 0 until 3) {
                                    val (mName, mVal) = months[row * 3 + col]
                                    val isSel = (initialYearMonth.year == selectingYear && initialYearMonth.monthValue == mVal)
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp).clip(RoundedCornerShape(12.dp)).background(if (isSel) Color(0xFFFFCCBC) else Color.Transparent).clickable { onConfirm(YearMonth.of(selectingYear, mVal)) }) {
                                        Text(mName, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFFBF360C) else Color(0xFF6D4C41))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), border = null) { Text("取消", color = Color.Gray) }
                }
            }
        }
    }
}

// ==========================================
// 本頁專用的動畫工具
// ==========================================
@Composable
private fun SmoothEntranceAnim(
    delay: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500)
    )
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    Box(modifier = modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        translationY = offsetY
    }) { content() }
}

private fun Modifier.bouncyClick(
    scaleDown: Float = 0.92f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { isPressed = true; tryAwaitRelease(); isPressed = false; onClick() }
            )
        }
}