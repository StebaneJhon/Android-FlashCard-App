package com.example.flashcard.backend

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.WeeklyReview

@Database(
    entities = [
        Card::class,
        Deck::class,
        User::class,
        WeeklyReview::class,
        SpaceRepetitionBox::class,
        CardContent::class,
        CardDefinition::class,],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to =7),
        AutoMigration(from = 7, to =8),
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
                database.execSQL("CREATE TABLE IF NOT EXISTS spaceRepetitionBox (levelId INT PRIMARY KEY, levelNme varchar(255), levelColor varchar(255), levelRepeatIn INT, levelRevisionMargin INT)")
                database.execSQL("CREATE TABLE IF NOT EXISTS cardContent (contentId INT PRIMARY KEY, cardId INT, content varchar(255))")
                database.execSQL("CREATE TABLE IF NOT EXISTS cardDefinition (definitionId INT PRIMARY KEY, cardId INT, contentId INT, definition varchar(255), isCorrectDefinition BOOLEAN)")
            }

        }
    }

}