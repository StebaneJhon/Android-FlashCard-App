package com.example.flashcard.backend

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcard.entities.Card
import com.example.flashcard.entities.Deck

@Database(
    entities = [Card::class, Deck::class],
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
                    "flash_card_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }

}