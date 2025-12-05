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
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun MoodDiaryScreen(
    targetDate: LocalDate = LocalDate.now(),
    onGoToCalendar: () -> Unit = {}
) {
    // 狀態管理
    var selectedMood by remember { mutableStateOf("") }
    var diaryText by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var encouragementMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val moods = listOf(
        Pair("開心", "😄"),
        Pair("難過", "😢"),
        Pair("生氣", "😠"),
        Pair("興奮", "🤩"),
        Pair("平靜", "😌")
    )

    // 自動讀取舊資料
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

    fun getEncouragement(mood: String): String {
        return when (mood) {
            "開心" -> "太棒了！笑容是世界上最強大的力量。\n今天真是美好的一天！"
            "難過" -> "抱抱你，哭完之後會舒服很多的。\n明天太陽依然會升起！"
            "生氣" -> "深呼吸，別讓壞情緒傷了身體。\n冷靜下來，你是最棒的！"
            "興奮" -> "哇！太替你開心了！\n帶著這份衝勁繼續往前衝吧！"
            "平靜" -> "享受這份寧靜，歲月靜好。\n休息是為了走更長遠的路。"
            else -> "日記已經順利儲存囉！\n繼續加油，愛自己多一點！"
        }
    }

    fun saveDiaryEntry() {
        if (selectedMood.isNotEmpty()) {
            encouragementMessage = getEncouragement(selectedMood)
            diaryMap[targetDate] = DiaryEntry(mood = selectedMood, diary = diaryText)

            scope.launch(Dispatchers.IO) {
                val newEntity = MoodEntity(
                    date = targetDate.toString(),
                    mood = selectedMood,
                    diary = diaryText
                )
                moodDatabase.moodDao().insertMood(newEntity)
            }
            showConfirmation = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF0E0)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (targetDate == LocalDate.now()) "你今天心情如何呢~" else "補寫/修改 $targetDate 日記",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B4C3B)
                ),
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                moods.forEach { (moodName, emoji) ->
                    MoodButton(
                        emoji = emoji,
                        moodName = moodName,
                        isSelected = selectedMood == moodName,
                        onClick = { selectedMood = moodName }
                    )
                }
            }

            if (selectedMood.isNotEmpty()) {
                Text(
                    text = "你選了：$selectedMood",
                    style = TextStyle(fontSize = 20.sp, color = Color(0xFF6B4C3B)),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 300.dp)
                    .padding(bottom = 24.dp),
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
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("儲存日記", fontSize = 22.sp)
            }
        }
    }

    // ==========================================
    // 客製化彈出視窗 (Dialog)
    // ==========================================
    if (showConfirmation) {
        Dialog(
            onDismissRequest = { }, // 強制不給點外面關閉
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false // 解除寬度限制，讓我們可以自己設
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // 設定寬度為螢幕的 90%
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)) // 淡黃色背景
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 標題
                    Text(
                        text = "儲存成功！很讚喔👍",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B4C3B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 鼓勵文字
                    Text(
                        text = encouragementMessage,
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center // 文字置中
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 按鈕區塊 (置中並排)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center, // 【關鍵】讓按鈕置中
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左邊按鈕：回到心情
                        OutlinedButton(
                            onClick = { showConfirmation = false },
                            border = BorderStroke(1.dp, Color(0xFF6B4C3B)),
                            modifier = Modifier.weight(1f) // 讓兩個按鈕等寬，看起來更整齊
                        ) {
                            Text("回到心情", fontSize = 16.sp, color = Color(0xFF6B4C3B))
                        }

                        Spacer(modifier = Modifier.width(16.dp)) // 按鈕中間的間距

                        // 右邊按鈕：前往日曆
                        Button(
                            onClick = {
                                showConfirmation = false
                                onGoToCalendar()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4C3B)),
                            modifier = Modifier.weight(1f) // 讓兩個按鈕等寬
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
fun MoodButton(
    emoji: String,
    moodName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            text = moodName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}