package com.example.dailymood_best

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current

    // 控制是否正在打招呼
    var isGreeting by remember { mutableStateOf(false) }
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
            delay(2500)
            isGreeting = false
        }
    }

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
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "主頁背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. 標題
            SmoothEntranceAnim(delay = 0) {
                Text(
                    text = "Daily Mood ✨",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6B4C3B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 2. 對話框
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

            // 3. 吉祥物
            SmoothEntranceAnim(delay = 150) {
                Image(
                    painter = painterResource(id = R.drawable.koala_mascot),
                    contentDescription = "開心無尾熊吉祥物",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(250.dp)
                        .padding(bottom = 24.dp)
                        .bouncyClick(scaleDown = 0.85f) {
                            val newGreeting = greetings.random()
                            currentGreetingText = newGreeting
                            isGreeting = true

                            val speakText = newGreeting.replace("\n", "，")
                            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                        .graphicsLayer {
                            rotationZ = if (isGreeting) waveRotation else 0f
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                        }
                )
            }

            // 4. 今日心情按鈕
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

            // 5. 下方兩顆按鈕
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
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6B4C3B))
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