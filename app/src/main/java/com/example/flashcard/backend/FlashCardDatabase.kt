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
    version = 1
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
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }

    }

}