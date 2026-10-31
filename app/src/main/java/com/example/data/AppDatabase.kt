package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeckEntity::class, CardEntity::class, FlashcardEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun flashcardDao(): FlashcardDao

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
                    .fallbackToDestructiveMigration()
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
                    populateInitialData(database.deckDao(), database.cardDao(), database.flashcardDao())
                }
            }
        }

        private suspend fun populateInitialData(
            deckDao: DeckDao,
            cardDao: CardDao,
            flashcardDao: FlashcardDao
        ) {
            val deckName = "2015/12 JLPT N3 Vocabulary"
            val deckId = deckDao.insertDeck(
                DeckEntity(
                    name = deckName,
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

            val flashcards = PreloadedData.N3_VOCABULARY_LIST.map { item ->
                val frontText = if (item.kanji == "-") item.kana else item.kanji
                val backText = "${item.kana} — ${item.burmese}"
                FlashcardEntity(
                    front = frontText,
                    back = backText,
                    deckName = deckName,
                    interval = 0,
                    easeFactor = 2.5,
                    repetitions = 0,
                    lapses = 0,
                    reviewCount = 0,
                    srsState = 0,
                    nextReviewTimestamp = System.currentTimeMillis()
                )
            }
            flashcardDao.insertCards(flashcards)
        }
    }
}
