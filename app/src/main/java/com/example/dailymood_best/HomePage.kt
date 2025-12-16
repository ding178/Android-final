package com.example.dailymood_best

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun HomePage(
    onNavigateToMood: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogout: () -> Unit
) {
    // 控制是否正在打招呼
    var isGreeting by remember { mutableStateOf(false) }
    // 控制右上角選單是否展開
    var showMenu by remember { mutableStateOf(false) }

    // 記錄目前要顯示哪一句問候語
    var currentGreetingText by remember { mutableStateOf("") }

    // 5 句隨機問候語清單
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
            delay(2000)
            isGreeting = false
        }
    }

    // 揮手動畫
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

        // ==========================================
        // 【修改重點】右上角改為人像與下拉選單
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, end = 24.dp), // 調整位置
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                // 1. 圓形頭像按鈕
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { showMenu = true }, // 點擊展開選單
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFF6B4C3B)),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person, // 使用內建人像圖示
                        contentDescription = "使用者選單",
                        tint = Color(0xFF6B4C3B),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // 2. 下拉選單 (DropdownMenu)
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFFFFF8E1)) // 暖色背景
                ) {
                    // 顯示名稱 (不可點擊，僅作顯示)
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("目前登入：", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = UserManager.currentNickname ?: "使用者",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        },
                        onClick = { /* 純顯示，不做動作 */ }
                    )

                    HorizontalDivider(color = Color(0xFFFFD180))

                    // 登出按鈕
                    DropdownMenuItem(
                        text = {
                            Text("登出帳號", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        },
                        onClick = {
                            showMenu = false
                            onLogout()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "登出",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    )
                }
            }
        }
        // ==========================================

        // 前景內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 歡迎詞 (保留在畫面中央也很溫馨)
            Text(
                text = "Hi, ${UserManager.currentNickname ?: "朋友"}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D6E63),
                modifier = Modifier.padding(bottom = 4.dp)
            )

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
                this@Column.AnimatedVisibility(
                    visible = isGreeting,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
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
                    .clickable {
                        currentGreetingText = greetings.random()
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