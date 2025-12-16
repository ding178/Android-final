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
import kotlinx.coroutines.launch
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

        // 2. 設定鬧鐘 (測試版：10秒後響鈴)
        scheduleDailyReminder()

        // 3. 初始化並播放背景音樂 (BGM)
        try {
            bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
            bgmPlayer?.isLooping = true
            bgmPlayer?.setVolume(0.5f, 0.5f)
            bgmPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. 初始化按鈕音效 (Bubble)
        try {
            bubblePlayer = MediaPlayer.create(this, R.raw.bubble)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            // 控制是否已登入的狀態
            // 如果 UserManager 中已經有 user (例如自動登入)，則初始值為 true
            var isLoggedIn by remember { mutableStateOf(UserManager.isLoggedIn()) }

            if (!isLoggedIn) {
                // --- 顯示登入頁 ---
                LoginPage(
                    onLoginSuccess = {
                        isLoggedIn = true
                        playBubbleSound() // 登入成功播放音效
                    }
                )
            } else {
                // --- 顯示主程式 ---
                DailyMoodApp(
                    onPlaySound = { playBubbleSound() },
                    onLogout = {
                        // 執行登出邏輯
                        UserManager.logout()
                        isLoggedIn = false
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bgmPlayer?.release()
        bubblePlayer?.release()
    }

    // 播放氣泡音效的輔助函式
    private fun playBubbleSound() {
        try {
            if (bubblePlayer?.isPlaying == true) {
                bubblePlayer?.seekTo(0)
            }
            bubblePlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 設定每日提醒排程 (目前為測試模式：10秒後響鈴)
    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- 測試模式 Start: 10 秒後響鈴 ---
        val triggerTime = System.currentTimeMillis() + 10 * 1000

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        // --- 測試模式 End ---

        /* 正式版請改回以下程式碼：
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 17) // 設定為晚上 9 點
            set(Calendar.MINUTE, 43)
            set(Calendar.HOUR_OF_DAY, 21) // 晚上 9 點
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        */
    }
}

@Composable
fun DailyMoodApp(
    onPlaySound: () -> Unit,
    onLogout: () -> Unit // 【新增】接收登出功能
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var editingDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = { index ->
                    onPlaySound()
                    scope.launch { pagerState.animateScrollToPage(index) }
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
                    // 第 0 頁：首頁 (傳入 onLogout)
                    0 -> HomePage(
                        onNavigateToMood = {
                            editingDate = LocalDate.now()
                            onPlaySound()
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onNavigateToCalendar = {
                            onPlaySound()
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onNavigateToStats = {
                            onPlaySound()
                            scope.launch { pagerState.animateScrollToPage(3) }
                        },
                        onLogout = onLogout // 傳遞登出事件
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
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )

                    // 第 3 頁：統計
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
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "首頁") },
            label = { Text("首頁") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Edit, contentDescription = "心情") },
            label = { Text("心情") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.DateRange, contentDescription = "日曆") },
            label = { Text("日曆") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "統計") },
            label = { Text("統計") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
    }
}