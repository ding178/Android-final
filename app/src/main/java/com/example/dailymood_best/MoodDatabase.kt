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
@Entity(tableName = "mood_table")
data class MoodEntity(
    @PrimaryKey val date: String, // 使用日期字串 "2023-10-27" 當作主鍵 (ID)
    val mood: String,
    val diary: String
)

// ==========================================
// 2. DAO (資料操作介面)
// ==========================================
@Dao
interface MoodDao {
    // 讀取所有日記
    @Query("SELECT * FROM mood_table")
    suspend fun getAllMoods(): List<MoodEntity>

    // 新增或更新日記 (如果日期一樣，就覆蓋舊的)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(moodEntity: MoodEntity)

    // 刪除某一篇日記 (目前沒用到，先寫著備用)
    @Query("DELETE FROM mood_table WHERE date = :date")
    suspend fun deleteMood(date: String)
}

// ==========================================
// 3. Database (資料庫本體)
// ==========================================
@Database(entities = [MoodEntity::class], version = 1, exportSchema = false)
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
                    "mood_database" // 資料庫檔案名稱
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}