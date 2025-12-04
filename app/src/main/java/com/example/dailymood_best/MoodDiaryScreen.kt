package com.example.dailymood_best

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlinx.coroutines.delay

@Composable
fun MoodDiaryScreen() {
    // 狀態管理
    var selectedMood by remember { mutableStateOf("") }
    var diaryText by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    val moods = listOf(
        Pair("開心", "😄"),
        Pair("難過", "😢"),
        Pair("生氣", "😠"),
        Pair("興奮", "🤩"),
        Pair("平靜", "😌")
    )

    fun saveDiaryEntry() {
        if (selectedMood.isNotEmpty() && diaryText.isNotEmpty()) {
            val today = LocalDate.now()
            // 這裡直接使用 DiaryData.kt 裡的全域變數
            diaryMap[today] = DiaryEntry(mood = selectedMood, diary = diaryText)
            showConfirmation = true
        }
    }

    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            delay(2000)
            showConfirmation = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF0E0) // 統一背景色
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "你今天心情如何呢~",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B4C3B)
                ),
                modifier = Modifier.padding(bottom = 24.dp)
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

            // 顯示選中提示
            if (selectedMood.isNotEmpty()) {
                Text(
                    text = "你選了：$selectedMood",
                    style = TextStyle(fontSize = 16.sp, color = Color(0xFF6B4C3B)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "請選擇一個心情來開始記錄吧！",
                    style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = diaryText,
                onValueChange = { diaryText = it },
                label = { Text("記錄今天的日記...") },
                placeholder = { Text("今天發生了什麼事？") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 300.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )

            Button(
                onClick = ::saveDiaryEntry,
                enabled = selectedMood.isNotEmpty() && diaryText.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("儲存日記")
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            confirmButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("確定")
                }
            },
            title = { Text("儲存成功") },
            text = { Text("日記已儲存至日曆！") }
        )
    }
}

// 這個按鈕元件原本在 MainActivity 也有，這裡保留一份即可
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
            .width(60.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text(emoji, fontSize = 32.sp)
        Text(moodName, fontSize = 12.sp)
    }
}