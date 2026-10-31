package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity to store flashcard collections locally.
 *
 * Includes:
 * - [front]: Front text / question / prompt
 * - [back]: Back text / answer / definition
 * - [deckName]: Name of the deck or collection to which the card belongs
 * - Study progress metadata:
 *   - [interval]: Spaced repetition interval in days
 *   - [easeFactor]: SM-2 / Anki ease multiplier (default 2.5)
 *   - [repetitions]: Consecutive successful reviews
 *   - [lapses]: Times failed or forgotten
 *   - [reviewCount]: Total review count
 *   - [srsState]: Current learning stage (0: New, 1: Learning, 2: Review, 3: Mastered)
 *   - [lastReviewedTimestamp]: Timestamp of the last review in milliseconds
 *   - [nextReviewTimestamp]: Timestamp when the card is next due in milliseconds
 *   - [createdAt]: Timestamp when the card was created
 */
@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["deckName"]),
        Index(value = ["nextReviewTimestamp"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "front")
    val front: String,

    @ColumnInfo(name = "back")
    val back: String,

    @ColumnInfo(name = "deckName")
    val deckName: String,

    // Study progress metadata
    @ColumnInfo(name = "interval")
    val interval: Int = 0,

    @ColumnInfo(name = "easeFactor")
    val easeFactor: Double = 2.5,

    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 0,

    @ColumnInfo(name = "lapses")
    val lapses: Int = 0,

    @ColumnInfo(name = "reviewCount")
    val reviewCount: Int = 0,

    @ColumnInfo(name = "srsState")
    val srsState: Int = 0,

    @ColumnInfo(name = "lastReviewedTimestamp")
    val lastReviewedTimestamp: Long? = null,

    @ColumnInfo(name = "nextReviewTimestamp")
    val nextReviewTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
