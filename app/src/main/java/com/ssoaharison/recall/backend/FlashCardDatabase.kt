package com.ssoaharison.recall.backend

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.User
import com.ssoaharison.recall.backend.entities.WeeklyReview

@Database(
    entities = [
        Card::class,
        Deck::class,
        User::class,
        WeeklyReview::class,
        SpaceRepetitionBox::class,
        CardContent::class,
        CardDefinition::class,],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = FlashCardDatabase.Migration2To3::class),
        AutoMigration(from = 3, to = 4, spec = FlashCardDatabase.Migration3To4::class)
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
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }

    }

    @RenameColumn(tableName = "Deck", fromColumnName = "deck_first_language", toColumnName = "card_content_default_language")
    @RenameColumn(tableName = "Deck", fromColumnName = "deck_second_language", toColumnName = "card_definition_default_language")
    @DeleteColumn(tableName = "Deck", columnName = "card_sum")
    class Migration2To3: AutoMigrationSpec

    @DeleteColumn(tableName = "Deck", columnName = "card_sum")
    class Migration3To4: AutoMigrationSpec

}