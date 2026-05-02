package com.example.mycarhere

import android.content.Context
import androidx.room.*

// ─── Entities ───────────────────────────────────────────────

@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "car_record")
data class CarRecord(
    @PrimaryKey val id: Int = 1,          // 단일 레코드
    val selectedLocationNames: String = "",   // ","로 구분된 장소 이름 목록
    val photoPaths: String = "",              // ","로 구분된 사진 경로 목록
    val audioPaths: String = "",              // ","로 구분된 오디오 경로 목록
    val savedAt: Long = System.currentTimeMillis()
)

// ─── DAOs ────────────────────────────────────────────────────

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY name ASC")
    suspend fun getAll(): List<Place>

    @Insert
    suspend fun insert(place: Place)

    @Update
    suspend fun update(place: Place)

    @Delete
    suspend fun delete(place: Place)
}

@Dao
interface CarRecordDao {
    @Query("SELECT * FROM car_record WHERE id = 1")
    suspend fun get(): CarRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: CarRecord)
}

// ─── Database ────────────────────────────────────────────────

@Database(entities = [Place::class, CarRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun carRecordDao(): CarRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycarhere.db"
                ).build().also { INSTANCE = it }
            }
    }
}
