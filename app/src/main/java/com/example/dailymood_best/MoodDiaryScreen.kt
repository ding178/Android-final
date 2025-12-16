package com.example.dailymood_best

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import com.google.ai.client.generativeai.GenerativeModel

// 包含：無尾熊圖片、搖擺動畫、取消功能、即時金句顯示
@Composable
fun MoodDiaryScreen(
    targetDate: LocalDate = LocalDate.now(),
    onGoToCalendar: () -> Unit = {}
) {
    var selectedMood by remember { mutableStateOf("") }
    var diaryText by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var encouragementMessage by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Gemini 2.5 模型
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.API_KEY
        )
    }

    // 心情對應圖片
    val moods = listOf(
        Pair("開心", R.drawable.koala_happy),
        Pair("難過", R.drawable.koala_sad),
        Pair("生氣", R.drawable.koala_angry),
        Pair("興奮", R.drawable.koala_excited),
        Pair("平靜", R.drawable.koala_calm)
    )

    LaunchedEffect(targetDate) {
        val entry = diaryMap[targetDate]
        if (entry != null) {
            selectedMood = entry.mood
            diaryText = entry.diary
        } else {
            selectedMood = ""
            diaryText = ""
        }
    }

    // 這裡定義金句，供下方 UI 直接呼叫顯示，也供 AI 失敗時備用
    fun getMoodQuote(mood: String): String {
        return when (mood) {
            "開心" -> "太棒了！笑容是世界上最強大的力量。"
            "難過" -> "抱抱你，明天太陽依然會升起！"
            "生氣" -> "深呼吸，冷靜下來，你是最棒的！"
            "興奮" -> "哇！太替你開心了，繼續往前衝吧！"
            "平靜" -> "享受這份寧靜，享受周遭的人事物。"
            else -> "請選擇一個心情來開始記錄吧！"
        }
    }

    fun saveDiaryEntry() {
        if (selectedMood.isNotEmpty()) {
            scope.launch {
                showConfirmation = true
                isGenerating = true
                encouragementMessage = "正在為你生成專屬小語..."

                // 在 MoodDiaryScreen.kt 的 saveDiaryEntry 函式中
// 找到 val newEntity = MoodEntity(...) 這一行，修改如下：

                withContext(Dispatchers.IO) {
                    diaryMap[targetDate] = DiaryEntry(mood = selectedMood, diary = diaryText)

                    // 這裡要取得當前登入的使用者 ID
                    val currentUser = UserManager.currentUser ?: "guest"

                    val newEntity = MoodEntity(
                        date = targetDate.toString(),
                        ownerId = currentUser, // 加入 ownerId
                        mood = selectedMood,
                        diary = diaryText
                    )
                    moodDatabase.moodDao().insertMood(newEntity)
                }


                try {
                    val prompt = "你是一位溫暖、有同理心的朋友，形象是一隻可愛的無尾熊。使用者今天的心情是「$selectedMood」。" +
                            "使用者的日記內容是：「$diaryText」。" +
                            "請根據心情和日記內容，給予一段溫暖的鼓勵或回應。" +
                            "條件：請用繁體中文，語氣溫柔、帶點無尾熊的慵懶可愛感，長度控制在 60 字以內。"

                    val response = withContext(Dispatchers.IO) {
                        generativeModel.generateContent(prompt)
                    }
                    encouragementMessage = response.text ?: getMoodQuote(selectedMood)

                } catch (e: Exception) {
                    e.printStackTrace()
                    encouragementMessage = "發生錯誤：\n${e.message}\n\n(請截圖這個畫面給我)"
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景圖片
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (targetDate == LocalDate.now()) "你今天心情如何呢~" else "補寫/修改 $targetDate 日記",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B4C3B)),
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            // 心情按鈕區
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceAround) {
                moods.forEach { (moodName, imageResId) ->
                    MoodButton(
                        imageResId = imageResId,
                        moodName = moodName,
                        isSelected = selectedMood == moodName,
                        onClick = {
                            selectedMood = if (selectedMood == moodName) "" else moodName
                        }
                    )
                }
            }

            // =========================================================
            // 【修改重點】根據選擇的心情，直接顯示對應金句
            // =========================================================
            if (selectedMood.isNotEmpty()) {
                Text(
                    text = getMoodQuote(selectedMood), // 直接呼叫函式顯示金句
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFF6B4C3B), // 深褐色
                        textAlign = TextAlign.Center // 文字置中，因為金句比較長
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "請選擇一個心情來開始記錄吧！",
                    style = TextStyle(fontSize = 20.sp, color = Color.Gray),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = diaryText,
                onValueChange = { diaryText = it },
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text("記錄這天的日記...", fontSize = 16.sp) },
                placeholder = { Text("這天發生了什麼事？(選填)", fontSize = 16.sp) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 300.dp).padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )

            Button(
                onClick = ::saveDiaryEntry,
                enabled = selectedMood.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("儲存日記", fontSize = 22.sp)
            }
        }
    }

    if (showConfirmation) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isGenerating) "無尾熊正在思考中..." else "儲存成功！很讚喔👍",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B4C3B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isGenerating) {
                        CircularProgressIndicator(color = Color(0xFF6B4C3B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在為你生成專屬小語...", color = Color.Gray)
                    } else {
                        Text(
                            text = encouragementMessage,
                            fontSize = 20.sp,
                            lineHeight = 32.sp,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmation = false },
                            border = BorderStroke(1.dp, Color(0xFF6B4C3B)),
                            modifier = Modifier.weight(1f),
                            enabled = !isGenerating
                        ) {
                            Text("回到心情", fontSize = 16.sp, color = Color(0xFF6B4C3B))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                showConfirmation = false
                                onGoToCalendar()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4C3B)),
                            modifier = Modifier.weight(1f),
                            enabled = !isGenerating
                        ) {
                            Text("前往日曆", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// MoodButton (保持慢速搖擺 + 100dp + 文字固定)
@Composable
fun MoodButton(
    imageResId: Int,
    moodName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 80.dp else 60.dp,
        label = "size"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wobble")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(color = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent)
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = moodName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(animatedSize)
                .clip(RoundedCornerShape(12.dp))
                .graphicsLayer {
                    rotationZ = if (isSelected) rotation else 0f
                }
        )
        Text(
            text = moodName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B4C3B),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}