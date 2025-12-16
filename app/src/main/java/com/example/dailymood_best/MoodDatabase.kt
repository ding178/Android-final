package com.example.dailymood_best

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// ==========================================
// 1. Entity (資料表結構)
// ==========================================

// 【新增】使用者資料表
@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val username: String, // 帳號當作 ID
    val password: String,             // 密碼
    val nickname: String              // 暱稱
)

// 【修改】日記資料表 (加入 ownerId)
// 複合主鍵：同一個 ownerId 在同一天 date 只能有一篇日記
@Entity(tableName = "mood_table", primaryKeys = ["date", "ownerId"])
data class MoodEntity(
    val date: String,    // 日期字串 "2023-10-27"
    val ownerId: String, // 擁有者帳號 (對應 UserEntity.username)
    val mood: String,
    val diary: String
)

// ==========================================
// 2. DAO (資料操作介面)
// ==========================================
@Dao
interface MoodDao {
    // --- 使用者相關 ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    // --- 日記相關 (特定使用者的) ---
    @Query("SELECT * FROM mood_table WHERE ownerId = :userId")
    suspend fun getMoodsByUser(userId: String): List<MoodEntity>

    @Query("SELECT * FROM mood_table WHERE date = :date AND ownerId = :userId LIMIT 1")
    suspend fun getMoodByDateAndUser(date: String, userId: String): MoodEntity?

    // --- 通用/鬧鐘相關 ---
    // 這是給鬧鐘用的，不檢查是誰的日記，只要當天有資料就不響鈴
    @Query("SELECT * FROM mood_table WHERE date = :date LIMIT 1")
    suspend fun getAnyMoodByDate(date: String): MoodEntity?

    // 原本的舊方法 (為了相容舊程式碼，或是剛才報錯的地方，我們保留這個名稱但指向通用查詢)
    @Query("SELECT * FROM mood_table WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodEntity?

    // --- 寫入 ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(moodEntity: MoodEntity)

    // 讀取所有日記 (暫時保留，用來做資料遷移或偵錯用)
    @Query("SELECT * FROM mood_table")
    suspend fun getAllMoods(): List<MoodEntity>
}

// ==========================================
// 3. Database (資料庫本體)
// ==========================================
// 注意：entities 陣列裡必須包含 UserEntity 和 MoodEntity
@Database(entities = [MoodEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class MoodDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile
        private var INSTANCE: MoodDatabase? = null

        fun getDatabase(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mood_database"
                )
                    //這行很重要！因為我們改了資料庫結構 (加了 ownerId 和 user_table)
                    //如果沒有這行，App 更新後打開會崩潰。這行會清空舊資料重建。
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}