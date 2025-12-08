package com.example.dailymood_best

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import kotlinx.coroutines.delay

@Composable
fun HomePage(
    onNavigateToMood: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    // 控制是否正在打招呼
    var isGreeting by remember { mutableStateOf(false) }

    // 【新增】記錄目前要顯示哪一句問候語
    var currentGreetingText by remember { mutableStateOf("") }

    // 【新增】5 句隨機問候語清單 (暖心風格)
    val greetings = remember {
        listOf(
            "今天也要元氣滿滿喔！\n無尾熊幫你加油！",
            "看到你真開心！\n記得要多笑一笑喔！",
            "你今天過得還好嗎？\n記得多愛自己一點！",
            "休息一下，喝口水吧！\n別把自己累壞囉～",
            "不管發生什麼事，\n我都會在這裡陪你！"
        )
    }

    // 自動計時器
    LaunchedEffect(isGreeting) {
        if (isGreeting) {
            // 【修改】時間縮短 0.5 秒 (原本 3000 -> 改成 2500)
            delay(2000)
            isGreeting = false
        }
    }

    // 揮手動畫 (快速搖擺)
    val infiniteTransition = rememberInfiniteTransition(label = "koalaWave")
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景圖片
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "主頁背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        // 前景內容
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 雲朵對話框顯示區
            Box(
                modifier = Modifier.height(80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isGreeting,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                ) {
                    // 【修改】這裡傳入隨機挑選出來的文字
                    CloudBubble(text = currentGreetingText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 首頁吉祥物 (無尾熊)
            Image(
                painter = painterResource(id = R.drawable.koala_mascot),
                contentDescription = "開心無尾熊吉祥物",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 24.dp)
                    // 【修改】點擊邏輯
                    .clickable {
                        // 1. 先隨機挑一句話
                        currentGreetingText = greetings.random()
                        // 2. 再開啟顯示開關 (這會觸發動畫和計時器)
                        isGreeting = true
                    }
                    .graphicsLayer {
                        rotationZ = if (isGreeting) waveRotation else 0f
                        transformOrigin = TransformOrigin(0.5f, 1.0f)
                    }
            )

            // 今日心情按鈕
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigateToMood() },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
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

                        Spacer(modifier = Modifier.width(24.dp))

                        Column {
                            Text(
                                text = "紀錄今日心情",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "寫下你的故事...",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 日曆 & 統計按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeMenuButton(
                    title = "查看日曆",
                    icon = Icons.Filled.DateRange,
                    color = Color(0xFFFFF8E1),
                    textColor = Color(0xFF5D4037),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCalendar
                )

                HomeMenuButton(
                    title = "數據統計",
                    icon = Icons.Filled.Info,
                    color = Color(0xFFE0F2F1),
                    textColor = Color(0xFF00695C),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStats
                )
            }
        }
    }
}

// 雲朵對話框元件 (保持不變)
@Composable
fun CloudBubble(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6B4C3B))
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color(0xFF5D4037),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// 共用小按鈕元件 (保持不變)
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