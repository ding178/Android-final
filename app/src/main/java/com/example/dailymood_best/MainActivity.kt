package com.example.dailymood_best

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// 範例全域日記資料
val diaryMap = mutableStateMapOf<java.time.LocalDate, DiaryEntry>()
data class DiaryEntry(val mood: String, val diary: String)

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
    var selectedTab by remember { mutableStateOf(0) } // 0: 心情, 1: 日曆

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> MoodPage()       // 你的心情頁面
                1 -> CalendarPage()   // 日曆頁面
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
            icon = { Text("😊", fontSize = 24.sp) },
            label = { Text("心情") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Text("📅", fontSize = 24.sp) },
            label = { Text("日曆") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
    }
}

// 你原本的 MoodPage()
@Composable
fun MoodPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0E0))
            .padding(16.dp)
    ) {
        Text("這裡是心情頁面", color = Color(0xFF6B4C3B))
    }
}
