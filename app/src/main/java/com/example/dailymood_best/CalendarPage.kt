package com.example.dailymood_best

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarPage(onEditDate: (LocalDate) -> Unit) {
    // 【修改】將 currentMonth 改為可變狀態 (var)，預設為現在
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 控制是否顯示「年月選擇器」
    var showDatePicker by remember { mutableStateOf(false) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0)) // 溫暖的背景
            .padding(16.dp)
    ) {
        // ==========================================
        // 1. 頂部標題區 (可點擊切換月份)
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp)) // 點擊時的水波紋會有圓角
                .clickable { showDatePicker = true } // 點擊開啟選擇器
                .padding(8.dp), // 內部留白
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                fontSize = 32.sp, // 【優化】字體加大，看起來更清晰
                fontWeight = FontWeight.ExtraBold, // 【優化】使用特粗體，解決「畫素粗」的視覺感
                color = Color(0xFF5D4037), // 深褐色，更有氣質
                letterSpacing = 1.sp // 字距稍微拉開，增加空氣感
            )
            // 加上一個小箭頭提示可以點擊
            Spacer(modifier = Modifier.width(8.dp))
            Text("▼", fontSize = 14.sp, color = Color(0xFF8D6E63))
        }

        // ==========================================
        // 2. 星期標題
        // ==========================================
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

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================================
        // 3. 日曆網格 (邏輯不變，優化視覺)
        // ==========================================
        val totalCells = daysInMonth + firstDayOfMonth - 1
        val weeks = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1
        var day = 1

        Column {
            for (week in 0 until weeks) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (dow in 1..7) {
                        val cellIndex = week * 7 + dow
                        if (cellIndex < firstDayOfMonth || day > daysInMonth) {
                            Box(modifier = Modifier.size(45.dp)) // 空白格
                        } else {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val entry = diaryMap[date]

                            // 日期格子的外觀
                            Box(
                                modifier = Modifier
                                    .size(45.dp) // 格子稍微加大
                                    .clip(RoundedCornerShape(12.dp)) // 圓角矩形，比圓形更有現代感
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF6B4C3B) // 選中變成深色
                                            isToday -> Color(0xFFFFCCBC)    // 今天是淡橘色
                                            else -> Color.White             // 其他是白色
                                        }
                                    )
                                    .clickable { selectedDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF5D4037)
                                    )
                                    // 如果有心情，顯示一個小點或表情
                                    if (entry != null) {
                                        Text(entry.mood, fontSize = 12.sp)
                                    }
                                }
                            }
                            day++
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 4. 下方顯示區 (日記卡片)
        // ==========================================
        val selectedEntry = diaryMap[selectedDate]

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp) // 更圓潤的卡片
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

                    Button(
                        onClick = { onEditDate(selectedDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCBC)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("修改 ✎", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFE6D6))

                Text(
                    "心情：${selectedEntry?.mood ?: "尚未記錄"}",
                    fontSize = 18.sp,
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "日記：${selectedEntry?.diary ?: "今天沒有寫日記喔～"}",
                    fontSize = 16.sp,
                    lineHeight = 24.sp, // 增加行高，閱讀更舒服
                    color = Color(0xFF8D6E63)
                )
            }
        }
    }

    // ==========================================
    // 5. 彈出式年月選擇器 (MonthYearPicker)
    // ==========================================
    if (showDatePicker) {
        MonthYearPickerDialog(
            initialYearMonth = currentMonth,
            onDismiss = { showDatePicker = false },
            onConfirm = { newYearMonth ->
                currentMonth = newYearMonth
                showDatePicker = false
                // 切換月份時，預設選中該月1號，或者保持原本日期(如果還在同個月)
                selectedDate = newYearMonth.atDay(1)
            }
        )
    }
}

/**
 * 自定義的優雅年月選擇器
 */
@Composable
fun MonthYearPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    // 暫存選擇器內的年份狀態
    var selectingYear by remember { mutableIntStateOf(initialYearMonth.year) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // 淡黃色背景
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 年份切換區 ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    IconButton(onClick = { selectingYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year", tint = Color(0xFF6B4C3B))
                    }
                    Text(
                        text = "$selectingYear",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4C3B)
                    )
                    IconButton(onClick = { selectingYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year", tint = Color(0xFF6B4C3B))
                    }
                }

                HorizontalDivider(color = Color(0xFFFFE0B2), modifier = Modifier.padding(bottom = 16.dp))

                // --- 月份選擇區 (3x4 網格) ---
                val months = listOf(
                    "JAN" to 1, "FEB" to 2, "MAR" to 3, "APR" to 4,
                    "MAY" to 5, "JUN" to 6, "JUL" to 7, "AUG" to 8,
                    "SEP" to 9, "OCT" to 10, "NOV" to 11, "DEC" to 12
                )

                // 簡單的手刻 Grid，保持輕量
                Column {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val (monthName, monthValue) = months[index]
                                val isSelected = (initialYearMonth.year == selectingYear && initialYearMonth.monthValue == monthValue)

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.5f) // 讓按鈕寬一點
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFFFCCBC) else Color.Transparent)
                                        .clickable {
                                            onConfirm(YearMonth.of(selectingYear, monthValue))
                                        }
                                ) {
                                    Text(
                                        text = monthName,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFBF360C) else Color(0xFF6D4C41)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 取消按鈕
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    border = null // 無邊框，純文字風格
                ) {
                    Text("取消", color = Color.Gray)
                }
            }
        }
    }
}