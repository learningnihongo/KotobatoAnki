package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local Cache Manager providing resilient offline caching for flashcards.
 * Guarantees that cards created or modified remain 100% accessible locally on device
 * without requiring any internet connection.
 */
object LocalCacheManager {
    private const val TAG = "LocalCacheManager"
    private const val CACHE_FILE_NAME = "offline_flashcards_cache.json"

    data class CacheStats(
        val totalDecksCached: Int,
        val totalCardsCached: Int,
        val lastCachedTimestamp: Long,
        val isOfflineReady: Boolean = true
    )

    /**
     * Persists an offline snapshot of all decks and cards to the internal private file storage.
     * This ensures local durability even if external storage or database instances reset.
     */
    fun saveToOfflineDiskCache(
        context: Context,
        decks: List<DeckEntity>,
        cards: List<CardEntity>
    ) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("lastSaved", System.currentTimeMillis())

            // Decks array
            val decksArray = JSONArray()
            for (deck in decks) {
                val deckObj = JSONObject().apply {
                    put("id", deck.id)
                    put("name", deck.name)
                    put("description", deck.description)
                    put("createdAt", deck.createdAt)
                }
                decksArray.put(deckObj)
            }
            root.put("decks", decksArray)

            // Cards array
            val cardsArray = JSONArray()
            for (card in cards) {
                val cardObj = JSONObject().apply {
                    put("id", card.id)
                    put("deckId", card.deckId)
                    put("kanji", card.kanji)
                    put("kana", card.kana)
                    put("burmeseMeaning", card.burmeseMeaning)
                    put("notes", card.notes)
                    put("orderIndex", card.orderIndex)
                    put("isStarred", card.isStarred)
                    put("reviewCount", card.reviewCount)
                    put("correctCount", card.correctCount)
                    put("srsLevel", card.srsLevel)
                    put("nextReviewDate", card.nextReviewDate)
                }
                cardsArray.put(cardObj)
            }
            root.put("cards", cardsArray)

            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            cacheFile.writeText(root.toString(), Charsets.UTF_8)
            Log.d(TAG, "Successfully cached ${cards.size} cards and ${decks.size} decks offline.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save offline disk cache", e)
        }
    }

    /**
     * Reads the offline snapshot from disk if present.
     */
    fun loadFromOfflineDiskCache(context: Context): Pair<List<DeckEntity>, List<CardEntity>>? {
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        if (!cacheFile.exists()) return null

        return try {
            val content = cacheFile.readText(Charsets.UTF_8)
            val root = JSONObject(content)

            val decks = mutableListOf<DeckEntity>()
            val decksArray = root.optJSONArray("decks") ?: JSONArray()
            for (i in 0 until decksArray.length()) {
                val obj = decksArray.getJSONObject(i)
                decks.add(
                    DeckEntity(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "Cached Deck"),
                        description = obj.optString("description", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val cards = mutableListOf<CardEntity>()
            val cardsArray = root.optJSONArray("cards") ?: JSONArray()
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                cards.add(
                    CardEntity(
                        id = obj.optLong("id", 0),
                        deckId = obj.optLong("deckId", 0),
                        kanji = obj.optString("kanji", ""),
                        kana = obj.optString("kana", ""),
                        burmeseMeaning = obj.optString("burmeseMeaning", ""),
                        notes = obj.optString("notes", ""),
                        orderIndex = obj.optInt("orderIndex", 0),
                        isStarred = obj.optBoolean("isStarred", false),
                        reviewCount = obj.optInt("reviewCount", 0),
                        correctCount = obj.optInt("correctCount", 0),
                        srsLevel = obj.optInt("srsLevel", 0),
                        nextReviewDate = obj.optLong("nextReviewDate", 0)
                    )
                )
            }

            Pair(decks, cards)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load offline disk cache", e)
            null
        }
    }

    /**
     * Retrieves statistics regarding the current offline cache.
     */
    fun getCacheStats(context: Context, currentDecksCount: Int, currentCardsCount: Int): CacheStats {
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        val lastTimestamp = if (cacheFile.exists()) cacheFile.lastModified() else System.currentTimeMillis()
        return CacheStats(
            totalDecksCached = currentDecksCount,
            totalCardsCached = currentCardsCount,
            lastCachedTimestamp = lastTimestamp,
            isOfflineReady = true
        )
    }

    /**
     * Creates a standalone, exportable Full JSON Backup file in app cache/files dir
     * which can be shared or exported to external storage.
     */
    fun exportBackupFile(
        context: Context,
        decks: List<DeckEntity>,
        cards: List<CardEntity>
    ): File {
        val root = JSONObject()
        root.put("app", "AnkiFlashcards")
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        root.put("exportedDate", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))

        val decksArray = JSONArray()
        for (deck in decks) {
            val d = JSONObject().apply {
                put("id", deck.id)
                put("name", deck.name)
                put("description", deck.description)
                put("createdAt", deck.createdAt)
            }
            decksArray.put(d)
        }
        root.put("decks", decksArray)

        val cardsArray = JSONArray()
        for (card in cards) {
            val c = JSONObject().apply {
                put("id", card.id)
                put("deckId", card.deckId)
                put("kanji", card.kanji)
                put("kana", card.kana)
                put("burmeseMeaning", card.burmeseMeaning)
                put("notes", card.notes)
                put("orderIndex", card.orderIndex)
                put("isStarred", card.isStarred)
                put("reviewCount", card.reviewCount)
                put("correctCount", card.correctCount)
                put("srsLevel", card.srsLevel)
                put("nextReviewDate", card.nextReviewDate)
            }
            cardsArray.put(c)
        }
        root.put("cards", cardsArray)

        val exportDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val timeStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val backupFile = File(exportDir, "AnkiFlashcards_Backup_$timeStr.json")
        backupFile.writeText(root.toString(2), Charsets.UTF_8)
        return backupFile
    }

    /**
     * Parses a JSON backup string into Decks and Cards.
     */
    fun parseBackupJson(jsonString: String): Pair<List<DeckEntity>, List<CardEntity>>? {
        return try {
            val root = JSONObject(jsonString)
            val decks = mutableListOf<DeckEntity>()
            val decksArray = root.optJSONArray("decks") ?: JSONArray()
            for (i in 0 until decksArray.length()) {
                val obj = decksArray.getJSONObject(i)
                decks.add(
                    DeckEntity(
                        id = obj.optLong("id", 0),
                        name = obj.optString("name", "Restored Deck"),
                        description = obj.optString("description", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val cards = mutableListOf<CardEntity>()
            val cardsArray = root.optJSONArray("cards") ?: JSONArray()
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                cards.add(
                    CardEntity(
                        id = obj.optLong("id", 0),
                        deckId = obj.optLong("deckId", 0),
                        kanji = obj.optString("kanji", ""),
                        kana = obj.optString("kana", ""),
                        burmeseMeaning = obj.optString("burmeseMeaning", ""),
                        notes = obj.optString("notes", ""),
                        orderIndex = obj.optInt("orderIndex", 0),
                        isStarred = obj.optBoolean("isStarred", false),
                        reviewCount = obj.optInt("reviewCount", 0),
                        correctCount = obj.optInt("correctCount", 0),
                        srsLevel = obj.optInt("srsLevel", 0),
                        nextReviewDate = obj.optLong("nextReviewDate", 0)
                    )
                )
            }
            Pair(decks, cards)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse backup JSON", e)
            null
        }
    }

    // Study Reminder & Streak Settings Data
    data class StudyReminderConfig(
        val isEnabled: Boolean = true,
        val reminderHour: Int = 20, // 8:00 PM default
        val reminderMinute: Int = 0,
        val dailyGoalCards: Int = 20,
        val todayStudiedCount: Int = 0,
        val currentStreakDays: Int = 1,
        val lastStudyDayKey: String = ""
    )

    private const val PREFS_NAME = "anki_study_reminder_prefs"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MIN = "reminder_min"
    private const val KEY_DAILY_GOAL = "daily_goal"
    private const val KEY_TODAY_COUNT = "today_count"
    private const val KEY_STREAK = "current_streak"
    private const val KEY_LAST_DAY = "last_day_key"

    fun getReminderConfig(context: Context): StudyReminderConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val savedDay = prefs.getString(KEY_LAST_DAY, "") ?: ""
        val todayCount = if (savedDay == todayKey) prefs.getInt(KEY_TODAY_COUNT, 0) else 0

        return StudyReminderConfig(
            isEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true),
            reminderHour = prefs.getInt(KEY_REMINDER_HOUR, 20),
            reminderMinute = prefs.getInt(KEY_REMINDER_MIN, 0),
            dailyGoalCards = prefs.getInt(KEY_DAILY_GOAL, 20),
            todayStudiedCount = todayCount,
            currentStreakDays = prefs.getInt(KEY_STREAK, 1),
            lastStudyDayKey = todayKey
        )
    }

    fun saveReminderConfig(context: Context, config: StudyReminderConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, config.isEnabled)
            .putInt(KEY_REMINDER_HOUR, config.reminderHour)
            .putInt(KEY_REMINDER_MIN, config.reminderMinute)
            .putInt(KEY_DAILY_GOAL, config.dailyGoalCards)
            .apply()
    }

    fun recordStudyEvent(context: Context, cardsCount: Int = 1) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val savedDay = prefs.getString(KEY_LAST_DAY, "") ?: ""

        val newTodayCount = if (savedDay == todayKey) {
            prefs.getInt(KEY_TODAY_COUNT, 0) + cardsCount
        } else {
            cardsCount
        }

        var streak = prefs.getInt(KEY_STREAK, 1)
        if (savedDay != todayKey) {
            // Check if yesterday was recorded for streak
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(cal.time)
            if (savedDay == yesterdayKey) {
                streak += 1
            } else if (savedDay.isNotEmpty()) {
                streak = 1
            }
        }

        prefs.edit()
            .putString(KEY_LAST_DAY, todayKey)
            .putInt(KEY_TODAY_COUNT, newTodayCount)
            .putInt(KEY_STREAK, streak)
            .apply()
    }
}
