package com.example.dailymood_best

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
// 需要新增這幾個 import
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush // 如果你想用漸層綠色代替圖片的話可以留著，圖片版則用不到
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth

// 全域變數
lateinit var moodDatabase: MoodDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        moodDatabase = MoodDatabase.getDatabase(this)

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

        setContent {
            DailyMoodApp()
        }
    }
}

@Composable
fun DailyMoodApp() {
    // 【新增】PagerState 用來控制目前在第幾頁 (總共 4 頁)
    val pagerState = rememberPagerState(pageCount = { 4 })

    // 用來控制滑動動畫的協程
    val scope = rememberCoroutineScope()

    // 記錄目前要編輯的日期
    var editingDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                // 【連動】BottomBar 的選取狀態，直接聽 Pager 現在滑到哪一頁
                selectedTab = pagerState.currentPage,
                onTabSelected = { index ->
                    // 當點擊底部按鈕時，命令 Pager 滑動到那一頁
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }

                    // 如果點擊的是「心情 (index 1)」，重設日期為今天
                    if (index == 1) {
                        editingDate = LocalDate.now()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // 【關鍵修改】使用 HorizontalPager 取代原本的 when()
            // 這讓頁面可以左右滑動
            HorizontalPager(
                state = pagerState,
                // userScrollEnabled = true // 預設就是 true，允許手指滑動
            ) { pageIndex ->
                // 根據頁碼顯示對應的畫面
                when (pageIndex) {
                    // 第 0 頁：首頁
                    0 -> HomePage(
                        onNavigateToMood = {
                            editingDate = LocalDate.now()
                            scope.launch { pagerState.animateScrollToPage(1) } // 滑到心情頁
                        },
                        onNavigateToCalendar = {
                            scope.launch { pagerState.animateScrollToPage(2) } // 滑到日曆頁
                        },
                        onNavigateToStats = {
                            scope.launch { pagerState.animateScrollToPage(3) } // 滑到統計頁
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
                            // 點擊修改後，滑動回心情頁 (Index 1)
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