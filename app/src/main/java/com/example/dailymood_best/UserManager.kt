package com.example.dailymood_best

object UserManager {
    var currentUser: String? = null // 存 username
    var currentNickname: String? = null // 存暱稱

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun logout() {
        currentUser = null
        currentNickname = null
        // 登出時清空記憶體中的日記暫存
        diaryMap.clear()
    }
}