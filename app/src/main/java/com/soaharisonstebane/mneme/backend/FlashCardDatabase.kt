package com.soaharisonstebane.mneme.backend

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardContent
import com.soaharisonstebane.mneme.backend.entities.CardDefinition
import com.soaharisonstebane.mneme.backend.entities.Deck
import com.soaharisonstebane.mneme.backend.entities.SpaceRepetitionBox
import com.soaharisonstebane.mneme.backend.entities.User
import com.soaharisonstebane.mneme.backend.entities.WeeklyReview

@Database(
    entities = [
        Card::class,
        Deck::class,
        User::class,
        WeeklyReview::class,
        SpaceRepetitionBox::class,
        CardContent::class,
        CardDefinition::class,],
    version = 1,
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
                    "app_database"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }

    }

}