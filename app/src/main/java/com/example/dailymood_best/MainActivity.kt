package com.example.dailymood_best

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyMoodApp()
        }
    }
}

@Composable
fun DailyMoodApp() {
    // 0: 心情, 1: 日曆, 2: 統計
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> MoodDiaryScreen()
                1 -> CalendarPage()
                2 -> StatisticsPage() // 新增的頁面
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
        // 第一個按鈕：心情
        NavigationBarItem(
            icon = { Text("😊", fontSize = 24.sp) },
            label = { Text("心情") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        // 第二個按鈕：日曆
        NavigationBarItem(
            icon = { Text("📅", fontSize = 24.sp) },
            label = { Text("日曆") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        // 第三個按鈕：統計 (新增的)
        NavigationBarItem(
            icon = { Text("📊", fontSize = 24.sp) },
            label = { Text("統計") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}