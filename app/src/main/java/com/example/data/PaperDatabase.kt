package com.example.data

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
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "papers")
data class PaperEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val schoolName: String,
    val boardName: String,
    val className: String,
    val subject: String,
    val examType: String,
    val timeAllowed: String,
    val maxMarks: Int,
    val difficulty: String,
    val instructionsJson: String,
    val sectionsJson: String,
    val textbookImagesCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_api_keys")
data class ApiKeyEntity(
    @PrimaryKey val userId: String,
    val encryptedKey: String,
    val maskedKey: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?
}

@Dao
interface PaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity)

    @Update
    suspend fun updatePaper(paper: PaperEntity)

    @Query("SELECT * FROM papers WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPapersByUser(userId: String): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE id = :paperId LIMIT 1")
    suspend fun getPaperById(paperId: String): PaperEntity?

    @Query("DELETE FROM papers WHERE id = :paperId")
    suspend fun deletePaper(paperId: String)

    @Query("SELECT COUNT(*) FROM papers WHERE userId = :userId")
    suspend fun countPapersByUser(userId: String): Int
}

@Dao
interface ApiKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveApiKey(apiKey: ApiKeyEntity)

    @Query("SELECT * FROM user_api_keys WHERE userId = :userId LIMIT 1")
    suspend fun getApiKey(userId: String): ApiKeyEntity?

    @Query("DELETE FROM user_api_keys WHERE userId = :userId")
    suspend fun deleteApiKey(userId: String)
}

@Database(
    entities = [UserEntity::class, PaperEntity::class, ApiKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PaperDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun paperDao(): PaperDao
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        @Volatile
        private var INSTANCE: PaperDatabase? = null

        fun getDatabase(context: Context): PaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaperDatabase::class.java,
                    "paper_genius_ai.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
