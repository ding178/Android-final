package com.example.dailymood_best

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0)), // 與其他頁面一致的背景色
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "這是數據統計頁面",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B4C3B)
            )
            Text(
                text = "這裡未來會顯示圖表📊",
                fontSize = 16.sp,
                color = Color(0xFF8D6E63)
            )
        }
    }
}