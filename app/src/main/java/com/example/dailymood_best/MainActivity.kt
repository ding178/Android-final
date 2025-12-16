package com.example.dailymood_best

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

// 全域變數
lateinit var moodDatabase: MoodDatabase

class MainActivity : ComponentActivity() {

    // 音樂播放器
    private var bgmPlayer: MediaPlayer? = null
    private var bubblePlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化資料庫
        moodDatabase = MoodDatabase.getDatabase(this)

        // 2. 從資料庫讀取舊資料到記憶體 (diaryMap)
        lifecycleScope.launch(Dispatchers.IO) {
            val savedList = moodDatabase.moodDao().getAllMoods()
            withContext(Dispatchers.Main) {
                savedList.forEach { entity ->
                    try {
                        val date = LocalDate.parse(entity.date)
                        diaryMap[date] = DiaryEntry(entity.mood, entity.diary)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // 3. 設定每日 21:00 提醒
        scheduleDailyReminder()

        // 4. 初始化並播放背景音樂 (BGM)
        try {
            // 請確認 res/raw/bgm.mp3 存在
            bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
            bgmPlayer?.isLooping = true // 設定循環播放
            bgmPlayer?.setVolume(0.5f, 0.5f) // 音量 50%
            bgmPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. 初始化按鈕音效 (Bubble)
        try {
            // 請確認 res/raw/bubble.mp3 存在
            bubblePlayer = MediaPlayer.create(this, R.raw.bubble)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            // 傳入播放音效的函式
            DailyMoodApp(onPlaySound = {
                try {
                    if (bubblePlayer?.isPlaying == true) {
                        bubblePlayer?.seekTo(0)
                    }
                    bubblePlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 釋放音樂資源，避免記憶體洩漏
        bgmPlayer?.release()
        bubblePlayer?.release()
    }

    // 設定每日提醒排程
    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)

        // FLAG_IMMUTABLE 是 Android 12+ (API 31+) 的強制要求
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 17) // 設定為晚上 9 點
            set(Calendar.MINUTE, 43)
            set(Calendar.SECOND, 0)
        }

        // 如果現在已經超過 21:00，就設為明天的 21:00
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            // 設定準時鬧鐘
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Android 12+ 預設允許精確鬧鐘，但如果被使用者關閉可能會有 Exception
            e.printStackTrace()
        }
    }
}

@Composable
fun DailyMoodApp(onPlaySound: () -> Unit) {
    // PagerState 用來控制目前在第幾頁 (總共 4 頁)
    val pagerState = rememberPagerState(pageCount = { 4 })

    // 用來控制滑動動畫的協程
    val scope = rememberCoroutineScope()

    // 記錄目前要編輯的日期
    var editingDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = { index ->
                    // 1. 播放按鈕音效
                    onPlaySound()

                    // 2. 滑動到指定頁面
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }

                    // 3. 如果點擊的是「心情 (index 1)」，重設日期為今天
                    if (index == 1) {
                        editingDate = LocalDate.now()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
            ) { pageIndex ->
                when (pageIndex) {
                    // 第 0 頁：首頁
                    0 -> HomePage(
                        onNavigateToMood = {
                            editingDate = LocalDate.now()
                            onPlaySound() // 點擊首頁大按鈕也播音效
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onNavigateToCalendar = {
                            onPlaySound()
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onNavigateToStats = {
                            onPlaySound()
                            scope.launch { pagerState.animateScrollToPage(3) }
                        }
                    )

                    // 第 1 頁：心情
                    1 -> MoodDiaryScreen(
                        targetDate = editingDate,
                        onGoToCalendar = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )

                    // 第 2 頁：日曆
                    2 -> CalendarPage(
                        onEditDate = { dateToEdit ->
                            editingDate = dateToEdit
                            onPlaySound()
                            // 點擊修改後，滑動回心情頁
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )

                    // 第 3 頁：統計 (這裡會自動使用新版的圓環圖與曲線圖)
                    3 -> StatisticsPage()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFFFFE6D6),
        tonalElevation = 4.dp
    ) {
        // 首頁 (Index 0)
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "首頁") },
            label = { Text("首頁") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        // 心情 (Index 1)
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Edit, contentDescription = "心情") },
            label = { Text("心情") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        // 日曆 (Index 2)
        NavigationBarItem(
            icon = { Icon(Icons.Filled.DateRange, contentDescription = "日曆") },
            label = { Text("日曆") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
        // 統計 (Index 3)
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "統計") },
            label = { Text("統計") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
    }
}