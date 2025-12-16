package com.example.dailymood_best

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginPage(onLoginSuccess: () -> Unit) {
    var isRegisterMode by remember { mutableStateOf(false) } // 切換登入/註冊模式
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") } // 只有註冊時需要

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景
        Image(
            painter = painterResource(id = R.drawable.home_background), // 借用首頁背景
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegisterMode) "註冊新帳號" else "歡迎回來",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 輸入框區
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("帳號") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密碼") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isRegisterMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("怎麼稱呼你？") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 按鈕區
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "請輸入帳號密碼", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch(Dispatchers.IO) {
                        val dao = moodDatabase.moodDao()
                        if (isRegisterMode) {
                            // 註冊邏輯
                            if (nickname.isBlank()) {
                                withContext(Dispatchers.Main) { Toast.makeText(context, "請輸入暱稱", Toast.LENGTH_SHORT).show() }
                                return@launch
                            }
                            try {
                                dao.registerUser(UserEntity(username, password, nickname))
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "註冊成功！請登入", Toast.LENGTH_SHORT).show()
                                    isRegisterMode = false // 切回登入模式
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { Toast.makeText(context, "帳號已存在", Toast.LENGTH_SHORT).show() }
                            }
                        } else {
                            // 登入邏輯
                            val user = dao.getUser(username)
                            withContext(Dispatchers.Main) {
                                if (user != null && user.password == password) {
                                    // 登入成功
                                    UserManager.currentUser = user.username
                                    UserManager.currentNickname = user.nickname

                                    // 載入該使用者的日記
                                    loadUserDiaries(user.username)

                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4C3B))
            ) {
                Text(if (isRegisterMode) "註冊" else "登入", fontSize = 18.sp)
            }

            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    text = if (isRegisterMode) "已有帳號？點此登入" else "還沒有帳號？點此註冊",
                    color = Color(0xFF8D6E63)
                )
            }
        }
    }
}

// 輔助函式：載入特定使用者的日記
suspend fun loadUserDiaries(userId: String) {
    withContext(Dispatchers.IO) {
        val savedList = moodDatabase.moodDao().getMoodsByUser(userId)
        withContext(Dispatchers.Main) {
            diaryMap.clear() // 先清空舊資料
            savedList.forEach { entity ->
                try {
                    val date = java.time.LocalDate.parse(entity.date)
                    diaryMap[date] = DiaryEntry(entity.mood, entity.diary)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}