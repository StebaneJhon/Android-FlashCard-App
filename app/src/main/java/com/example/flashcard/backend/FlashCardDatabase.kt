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
    version = 11,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to =7),
        AutoMigration(from = 7, to =8),
        AutoMigration(from = 8, to =9),
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
                ).fallbackToDestructiveMigration().addMigrations(MIGRATION_9_10, MIGRATION_10_11).build().also {
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

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_Deck (
                    deckId TEXT PRIMARY KEY NOT NULL,
                    deck_name TEXT,
                    deck_description TEXT,
                    deck_first_language TEXT,
                    deck_second_language TEXT,
                    deck_color_code TEXT,
                    card_sum INTEGER,
                    deck_category TEXT,
                    is_favorite INTEGER
                )
                """.trimIndent())

                database.execSQL("""
                INSERT INTO new_Deck (deckId, deck_name, deck_description, deck_first_language, deck_second_language, deck_color_code, card_sum, deck_category, is_favorite)
                SELECT deck_name, deck_name, deck_description, deck_first_language, deck_second_language, deck_color_code, card_sum, category, is_favorite FROM Deck
                """.trimIndent())

                database.execSQL("DROP TABLE Deck")
                database.execSQL("ALTER TABLE new_Deck RENAME TO Deck")


                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_Card (
                    cardId TEXT PRIMARY KEY NOT NULL,
                    card_content TEXT,
                    card_value TEXT,
                    deckId TEXT NOT NULL,
                    is_favorite INTEGER,
                    revision_time INTEGER,
                    missed_time INTEGER,
                    creation_date TEXT,
                    last_revision_date TEXT,
                    card_status TEXT,
                    next_miss_memorisation_date TEXT,
                    next_revision_date TEXT,
                    card_type TEXT
                )
                """.trimIndent())

                database.execSQL("""
                INSERT INTO new_Card (cardId, card_content, card_value, deckId, is_favorite, revision_time, missed_time, creation_date, last_revision_date, card_status, next_miss_memorisation_date, next_revision_date, card_type)
                SELECT creationDateTime, card_content, card_value, deckId, is_favorite, revision_time, missed_time, creation_date, last_revision_date, card_status, next_miss_memorisation_date, next_revision_date, card_type FROM Card
                """.trimIndent())

                database.execSQL("DROP TABLE Card")
                database.execSQL("ALTER TABLE new_Card RENAME TO Card")


                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_cardContent (
                    contentId VARCHAR(255) PRIMARY KEY NOT NULL DEFAULT '0:0', 
                    cardId VARCHAR(255) NOT NULL DEFAULT '0:0', 
                    deckId VARCHAR(255), 
                    content VARCHAR(1000)
                )
                """.trimIndent())
                database.execSQL("""
                INSERT INTO new_cardContent (contentId, cardId, content)
                SELECT contentId, cardId, content FROM cardContent
                """.trimIndent())
                database.execSQL("DROP TABLE cardContent")
                database.execSQL("ALTER TABLE new_cardContent RENAME TO cardContent")



                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_cardDefinition (
                    definitionId INTEGER PRIMARY KEY, 
                    cardId TEXT NOT NULL, 
                    deckId TEXT, 
                    contentId TEXT NOT NULL, 
                    definition TEXT, 
                    isCorrectDefinition INTEGER
                )
                """.trimIndent())
                database.execSQL("""
                INSERT INTO new_cardDefinition (definitionId, definition, cardId, contentId, isCorrectDefinition)
                SELECT definitionId, definition, definitionId, definitionId, isCorrectDefinition FROM cardDefinition
                """.trimIndent())
                database.execSQL("DROP TABLE cardDefinition")
                database.execSQL("ALTER TABLE new_cardDefinition RENAME TO cardDefinition")

            }

        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS new_Card (
                    cardId TEXT PRIMARY KEY NOT NULL,
                    deckId TEXT NOT NULL,
                    is_favorite INTEGER,
                    revision_time INTEGER,
                    missed_time INTEGER,
                    creation_date TEXT,
                    last_revision_date TEXT,
                    card_status TEXT,
                    next_miss_memorisation_date TEXT,
                    next_revision_date TEXT,
                    card_type TEXT
                )
                """.trimIndent())
                database.execSQL("""
                INSERT INTO new_Card (cardId, deckId, is_favorite, revision_time, missed_time, creation_date, last_revision_date, card_status, next_miss_memorisation_date, next_revision_date, card_type)
                SELECT cardId, deckId, is_favorite, revision_time, missed_time, creation_date, last_revision_date, card_status, next_miss_memorisation_date, next_revision_date, card_type FROM Card
                """.trimIndent())
                database.execSQL("DROP TABLE Card")
                database.execSQL("ALTER TABLE new_Card RENAME TO Card")

                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_cardContent (
                    contentId VARCHAR(255) PRIMARY KEY NOT NULL DEFAULT '0:0', 
                    cardId VARCHAR(255) NOT NULL DEFAULT '0:0', 
                    deckId VARCHAR(255), 
                    content VARCHAR(1000) NOT NULL DEFAULT '0:0'
                )
                """.trimIndent())
                database.execSQL("""
                INSERT INTO new_cardContent (contentId, cardId, deckId, content)
                SELECT contentId, cardId, deckId, content FROM cardContent
                """.trimIndent())
                database.execSQL("DROP TABLE cardContent")
                database.execSQL("ALTER TABLE new_cardContent RENAME TO cardContent")

                database.execSQL("""
                CREATE TABLE IF NOT EXISTS new_cardDefinition (
                    definitionId INTEGER PRIMARY KEY, 
                    cardId TEXT NOT NULL, 
                    deckId TEXT, 
                    contentId TEXT NOT NULL, 
                    definition TEXT NOT NULL, 
                    isCorrectDefinition INTEGER NOT NULL
                )
                """.trimIndent())
                database.execSQL("""
                INSERT INTO new_cardDefinition (definitionId, cardId, deckId, contentId, definition, isCorrectDefinition)
                SELECT definitionId, cardId, deckId, contentId, definition, isCorrectDefinition FROM cardDefinition
                """.trimIndent())
                database.execSQL("DROP TABLE cardDefinition")
                database.execSQL("ALTER TABLE new_cardDefinition RENAME TO cardDefinition")

            }

        }

    }

}