package com.example.dailymood_best

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
    // 0: 首頁, 1: 心情, 2: 日曆, 3: 統計
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    // 如果點擊「心情」分頁，重設為今天
                    if (index == 1) {
                        editingDate = LocalDate.now()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                // 首頁
                0 -> HomePage(
                    onNavigateToMood = {
                        editingDate = LocalDate.now() // 確保是今天
                        selectedTab = 1
                    },
                    onNavigateToCalendar = { selectedTab = 2 },
                    onNavigateToStats = { selectedTab = 3 }
                )

                // 心情頁面 (Index 1)
                1 -> MoodDiaryScreen(
                    targetDate = editingDate,
                    onGoToCalendar = { selectedTab = 2 } // 跳到日曆是 2
                )

                // 日曆頁面 (Index 2)
                2 -> CalendarPage(
                    onEditDate = { dateToEdit ->
                        editingDate = dateToEdit
                        selectedTab = 1 // 跳回心情頁面是 1
                    }
                )

                // 統計頁面 (Index 3)
                3 -> StatisticsPage()
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
            icon = { Icon(Icons.Filled.Edit, contentDescription = "心情") }, // 換成筆的圖案比較直覺
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