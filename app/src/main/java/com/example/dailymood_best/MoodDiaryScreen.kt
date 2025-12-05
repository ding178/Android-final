package com.example.dailymood_best

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
// 引入 Gemini AI
import com.google.ai.client.generativeai.GenerativeModel
import com.example.dailymood_best.BuildConfig
@Composable
fun MoodDiaryScreen(
    targetDate: LocalDate = LocalDate.now(),
    onGoToCalendar: () -> Unit = {}
) {
    var selectedMood by remember { mutableStateOf("") }
    var diaryText by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var encouragementMessage by remember { mutableStateOf("") }

    // 【新增】AI 生成狀態： true = 正在思考中, false = 思考完畢
    var isGenerating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // 1. 初始化 Gemini 模型
    // 使用 "gemini-1.5-flash" 模型，速度快且便宜(免費)
// 測試用的寫法
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.API_KEY // <--- 把你的 Key 直接貼在這裡，加上雙引號
        )
    }

    val moods = listOf(
        Pair("開心", "😄"),
        Pair("難過", "😢"),
        Pair("生氣", "😠"),
        Pair("興奮", "🤩"),
        Pair("平靜", "😌")
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

    // 原本的寫死回應 (當作備案，萬一沒網路時使用)
    fun getFallbackMessage(mood: String): String {
        return when (mood) {
            "開心" -> "太棒了！笑容是世界上最強大的力量。"
            "難過" -> "抱抱你，明天太陽依然會升起！"
            "生氣" -> "深呼吸，冷靜下來，你是最棒的！"
            "興奮" -> "哇！太替你開心了，繼續往前衝吧！"
            "平靜" -> "享受這份寧靜，休息是為了走更長遠的路。"
            else -> "日記已儲存，繼續加油！"
        }
    }

    fun saveDiaryEntry() {
        if (selectedMood.isNotEmpty()) {
            scope.launch {
                // 1. 先顯示彈窗，並進入「生成中」狀態
                showConfirmation = true
                isGenerating = true
                encouragementMessage = "正在為你生成專屬小語..." // 預設文字

                // 2. 儲存日記到資料庫 (IO Thread)
                withContext(Dispatchers.IO) {
                    diaryMap[targetDate] = DiaryEntry(mood = selectedMood, diary = diaryText)
                    val newEntity = MoodEntity(
                        date = targetDate.toString(),
                        mood = selectedMood,
                        diary = diaryText
                    )
                    moodDatabase.moodDao().insertMood(newEntity)
                }

                // 3. 呼叫 AI 生成回應 (IO Thread)
                try {
                    val prompt = "你是一位溫暖、有同理心的朋友。使用者今天的心情是「$selectedMood」。" +
                            "使用者的日記內容是：「$diaryText」。" +
                            "請根據心情和日記內容，給予一段溫暖的鼓勵或回應。" +
                            "條件：請用繁體中文，語氣溫柔，長度控制在 50 字以內，不要太長。"

                    // 開始生成
                    val response = withContext(Dispatchers.IO) {
                        generativeModel.generateContent(prompt)
                    }

                    // 生成完成，更新文字
                    encouragementMessage = response.text ?: getFallbackMessage(selectedMood)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // 【修改】把原本的備案文字換掉，改成顯示「真正的錯誤訊息」
                    // 這樣我們就知道是 401 (Key錯), 404 (找不到), 還是 Host (沒網路)
                    encouragementMessage = "發生錯誤：\n${e.message}\n\n(請截圖這個畫面給我)"
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF0E0)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (targetDate == LocalDate.now()) "你今天心情如何呢~" else "補寫/修改 $targetDate 日記",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B4C3B)),
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceAround) {
                moods.forEach { (moodName, emoji) ->
                    MoodButton(emoji = emoji, moodName = moodName, isSelected = selectedMood == moodName, onClick = { selectedMood = moodName })
                }
            }

            if (selectedMood.isNotEmpty()) {
                Text(text = "你選了：$selectedMood", style = TextStyle(fontSize = 20.sp, color = Color(0xFF6B4C3B)), modifier = Modifier.padding(bottom = 16.dp))
            } else {
                Text(text = "請選擇一個心情來開始記錄吧！", style = TextStyle(fontSize = 20.sp, color = Color.Gray), modifier = Modifier.padding(bottom = 16.dp))
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
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
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
                        text = if (isGenerating) "AI 正在思考中..." else "儲存成功！很讚喔👍",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B4C3B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 【重點】如果是生成狀態，顯示轉圈圈；否則顯示文字
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color(0xFF6B4C3B),
                            modifier = Modifier.size(48.dp)
                        )
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

                    // 按鈕區塊 (生成時鎖住按鈕，避免使用者亂按)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmation = false },
                            border = BorderStroke(1.dp, Color(0xFF6B4C3B)),
                            modifier = Modifier.weight(1f),
                            enabled = !isGenerating // 生成時不能按
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
                            enabled = !isGenerating // 生成時不能按
                        ) {
                            Text("前往日曆", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodButton(emoji: String, moodName: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp).clickable(onClick = onClick)
            .background(color = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(text = moodName, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
    }
}