package com.example.dailymood_best

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarPage() {
    val today = LocalDate.now()
    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf(today) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0))
            .padding(16.dp)
    ) {
        Text(
            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            fontSize = 24.sp,
            color = Color(0xFF6B4C3B),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("一","二","三","四","五","六","日").forEach {
                Text(it, fontSize = 16.sp, color = Color(0xFF6B4C3B), textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = daysInMonth + firstDayOfMonth - 1
        val weeks = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1
        var day = 1

        Column {
            for (week in 0 until weeks) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (dow in 1..7) {
                        val cellIndex = week * 7 + dow
                        if (cellIndex < firstDayOfMonth || day > daysInMonth) {
                            Box(modifier = Modifier.size(40.dp))
                        } else {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val entry = diaryMap[date]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSelected) Color(0xFFBFE6BA) else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(day.toString(), fontSize = 14.sp, color = Color(0xFF6B4C3B))
                                    Text(entry?.mood ?: "", fontSize = 16.sp)
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

        val selectedEntry = diaryMap[selectedDate]
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("日期：$selectedDate", fontSize = 18.sp, color = Color(0xFF6B4C3B), modifier = Modifier.padding(bottom = 8.dp))
            Text("心情：${selectedEntry?.mood ?: "無"}", fontSize = 18.sp, color = Color(0xFF6B4C3B), modifier = Modifier.padding(bottom = 4.dp))
            Text("日記：${selectedEntry?.diary ?: "無"}", fontSize = 16.sp, color = Color(0xFF6B4C3B))
        }
    }
}
