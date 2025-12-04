package com.example.dailymood_best

import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDate

// 定義資料結構 (全專案唯一一份)
data class DiaryEntry(
    val mood: String,
    val diary: String
)

// 定義全域變數 (全專案唯一一份)
val diaryMap = mutableStateMapOf<LocalDate, DiaryEntry>()