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
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.delay

// =================================================================
// 提供的資料結構 - 假設這些定義在應用程式的頂層或 ViewModel 中
// =================================================================

data class DiaryEntry(
    val mood: String,
    val diary: String
)

// 全域儲存日記資料 (使用 remember/LaunchedEffect 可以更好地整合到 Composable lifecycle)
// 為了這個範例的簡單性，我們保持原樣。
val diaryMap = mutableStateMapOf<LocalDate, DiaryEntry>()

// =================================================================
// Composable UI 實作
// =================================================================

/**
 * 心情日記的主要畫面 Composable
 */
@Composable
fun MoodDiaryScreen() {
    // 狀態管理：選定的心情和日記內容
    var selectedMood by remember { mutableStateOf("") }
    var diaryText by remember { mutableStateOf("") }

    // 儲存狀態的回饋
    var showConfirmation by remember { mutableStateOf(false) }

    // 心情選項清單
    val moods = listOf(
        Pair("開心", "😄"),
        Pair("難過", "😢"),
        Pair("生氣", "😠"),
        Pair("興奮", "🤩"),
        Pair("平靜", "😌")
    )

    // 儲存日記的邏輯
    fun saveDiaryEntry() {
        if (selectedMood.isNotEmpty() && diaryText.isNotEmpty()) {
            val today = LocalDate.now()
            diaryMap[today] = DiaryEntry(mood = selectedMood, diary = diaryText)

            // 顯示確認訊息並重設輸入
            showConfirmation = true
            // 為了真實應用，這邊通常不會清除，而是導航或在 state 中更新
            // diaryText = ""
            // selectedMood = ""
        }
    }

    // 處理確認訊息的計時器
    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            delay(2000) // 顯示 2 秒
            showConfirmation = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 標題
            Text(
                text = "你今天心情如何呢~",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 心情按鈕區塊
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

            // 顯示選定的心情
            if (selectedMood.isNotEmpty()) {
                Text(
                    text = "你選了：$selectedMood $",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "請選擇一個心情來開始記錄吧！",
                    style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }


            // 日記文字輸入框
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
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            // 儲存按鈕
            Button(
                onClick = ::saveDiaryEntry,
                enabled = selectedMood.isNotEmpty() && diaryText.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("儲存日記", fontSize = 18.sp)
            }
        }
    }

    // 儲存成功的彈出式訊息
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            confirmButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("確定")
                }
            },
            title = { Text("儲存成功！") },
            text = { Text("今天的日記已經順利儲存囉！") }
        )
    }
}

/**
 * 單個心情按鈕 Composable
 */
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
                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp, // 讓表情符號更大
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = moodName,
            style = TextStyle(
                fontSize = 12.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// 預覽功能
@Preview(showBackground = true)
@Composable
fun PreviewMoodDiaryScreen() {
    MaterialTheme {
        MoodDiaryScreen()
    }
}