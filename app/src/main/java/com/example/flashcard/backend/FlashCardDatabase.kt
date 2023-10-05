package com.example.flashcard.backend

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.WeeklyReview

@Database(
    entities = [Card::class, Deck::class, User::class, WeeklyReview::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
abstract class FlashCardDatabase : RoomDatabase() {

    abstract fun flashCardDao(): FlashCardDao

    companion object {
        @Volatile
        private var INSTANCE: FlashCardDatabase? = null

        fun getDatabase(context: Context): FlashCardDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlashCardDatabase::class.java,
                    "flash_card_database"
                ).addMigrations(migration3To4).build().also {
                    INSTANCE = it
                }
            }
        }

        val migration3To4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS user (userId INT PRIMARY KEY, name varchar(255), initial varchar(255), status varchar(255))")
                database.execSQL("CREATE TABLE IF NOT EXISTS weeklyReview (dayId INT PRIMARY KEY, dayName varchar(255), date varchar(255), revisedCardSum INT, colorGrade INT)")
            }

        }
    }

}