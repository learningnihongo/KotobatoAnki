package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [DeckEntity::class, CardEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anki_flashcards.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.deckDao(), database.cardDao())
                }
            }
        }

        private suspend fun populateInitialData(deckDao: DeckDao, cardDao: CardDao) {
            val deckId = deckDao.insertDeck(
                DeckEntity(
                    name = "2015/12 JLPT N3 Vocabulary",
                    description = "109 Essential Japanese Words with Hiragana, Kanji & Myanmar meanings (by Tr. Win)."
                )
            )

            val cards = PreloadedData.N3_VOCABULARY_LIST.map { item ->
                CardEntity(
                    deckId = deckId,
                    kanji = if (item.kanji == "-") item.kana else item.kanji,
                    kana = item.kana,
                    burmeseMeaning = item.burmese,
                    orderIndex = item.no
                )
            }
            cardDao.insertCards(cards)
        }
    }
}
