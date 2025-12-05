package com.example.dailymood_best

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info // 替代統計圖示
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomePage(
    onNavigateToMood: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0)) // 溫暖背景
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 頁面標題
        Text(
            text = "Daily Mood ✨",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6B4C3B),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // ==========================================
        // 1. 今日心情按鈕 (特別樣式)
        // ==========================================
        // 使用 Box + Brush 做出特別的漸層底色
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp) // 最大的按鈕
                .clip(RoundedCornerShape(24.dp))
                .clickable { onNavigateToMood() },
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        // 漸層色：從珊瑚粉到溫暖橘
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF8A65), Color(0xFFFFB74D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "紀錄今日心情",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "寫下你的故事...",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. 日曆 & 統計按鈕 (並排顯示)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // 按鈕中間的間距
        ) {
            // 日曆按鈕
            HomeMenuButton(
                title = "查看日曆",
                icon = Icons.Filled.DateRange,
                color = Color(0xFFFFF8E1), // 淡黃色
                textColor = Color(0xFF5D4037),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCalendar
            )

            // 統計按鈕 (假設你原本說的三個按鈕最後一個是統計)
            HomeMenuButton(
                title = "數據統計",
                icon = Icons.Filled.Info, // 暫時用 Info 圖示代表統計
                color = Color(0xFFE0F2F1), // 淡綠色
                textColor = Color(0xFF00695C),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStats
            )
        }
    }
}

// 抽取出來的共用小按鈕元件
@Composable
fun HomeMenuButton(
    title: String,
    icon: ImageVector,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}