package com.example.dailymood_best

import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDate

// 1. 定義日記的資料結構
data class DiaryEntry(
    val mood: String,
    val diary: String
)

// 2. 定義全域變數 diaryMap
// 因為放在獨立檔案且在 class 之外，所以專案內的所有檔案都能直接讀取到它
val diaryMap = mutableStateMapOf<LocalDate, DiaryEntry>()