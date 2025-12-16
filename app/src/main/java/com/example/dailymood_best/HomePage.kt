package com.example.dailymood_best

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomePage(
    onNavigateToMood: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // 控制是否正在打招呼
    var isGreeting by remember { mutableStateOf(false) }
    // 記錄目前要顯示哪一句問候語 (合併後只保留這一個宣告)
    var currentGreetingText by remember { mutableStateOf("") }

    // TTS (文字轉語音) 引擎狀態
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // 初始化 TTS
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.TAIWAN)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 調整聲音參數：變高變快 = 比較可愛
                    tts?.setPitch(1.4f)
                    tts?.setSpeechRate(1.1f)
                }
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // 控制右上角選單是否展開
    var showMenu by remember { mutableStateOf(false) }

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

    LaunchedEffect(isGreeting) {
        if (isGreeting) {
            delay(2500) // 等待對話框顯示時間
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "主頁背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        // ==========================================
        // 1. 右上角人像與下拉選單
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                // 圓形頭像按鈕
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { showMenu = true },
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFF6B4C3B)),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "使用者選單",
                        tint = Color(0xFF6B4C3B),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // 下拉選單
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFFFFF8E1))
                ) {
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
                        onClick = { /* 純顯示 */ }
                    )

                    HorizontalDivider(color = Color(0xFFFFD180))

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
        // 2. 中央主要內容
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // (A) 標題區塊
            SmoothEntranceAnim(delay = 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hi, ${UserManager.currentNickname ?: "朋友"}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Daily Mood ✨",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B4C3B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

// (B) 雲朵對話框 (無尾熊說話)
            SmoothEntranceAnim(delay = 50) {
                Column(
                    modifier = Modifier.height(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    AnimatedVisibility(
                        visible = isGreeting,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        CloudBubble(text = currentGreetingText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // (C) 吉祥物 (無尾熊) - 整合了點擊動畫與TTS
            SmoothEntranceAnim(delay = 150) {
                Image(
                    painter = painterResource(id = R.drawable.koala_mascot),
                    contentDescription = "開心無尾熊吉祥物",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(250.dp)
                        .padding(bottom = 24.dp)
                        .bouncyClick(scaleDown = 0.85f) {
                            // 1. 設定新台詞
                            val newGreeting = greetings.random()
                            currentGreetingText = newGreeting
                            isGreeting = true

                            // 2. 播放語音 (把換行符號拿掉唸起來比較順)
                            val speakText = newGreeting.replace("\n", "，")
                            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                        .graphicsLayer {
                            // 3. 設定揮手動畫
                            rotationZ = if (isGreeting) waveRotation else 0f
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                        }
                )
            }

            // (D) 今日心情按鈕
            SmoothEntranceAnim(delay = 300) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .bouncyClick { onNavigateToMood() },
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
                                Text(text = "紀錄今日心情", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "寫下你的故事...", fontSize = 15.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // (E) 下方功能按鈕 (日曆與統計)
            SmoothEntranceAnim(delay = 450) {
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
}

// 雲朵對話框元件
@Composable
fun CloudBubble(text: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 8.dp)) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp),
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, Color(0xFF6B4C3B))
        ) {
            Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = Color(0xFF5D4037), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            .bouncyClick { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
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
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bouncyScale"
    )
    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { isPressed = true; tryAwaitRelease(); isPressed = false; onClick() }
            )
        }
}