import java.time.LocalDate
import androidx.compose.runtime.mutableStateMapOf

data class DiaryEntry(
    val mood: String,
    val diary: String
)

// 全域儲存日記資料
val diaryMap = mutableStateMapOf<LocalDate, DiaryEntry>()
