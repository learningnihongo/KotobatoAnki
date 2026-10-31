package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for local flashcard collections and study progress metadata.
 */
@Dao
interface FlashcardDao {

    // ---------------------------------------------------------
    // Observable Queries (Flow) for reactive UI
    // ---------------------------------------------------------

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun getAllCardsFlow(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY id ASC")
    fun getCardsByDeckFlow(deckName: String): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT deckName FROM flashcards ORDER BY deckName ASC")
    fun getAllDeckNamesFlow(): Flow<List<String>>

    @Query("SELECT * FROM flashcards WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    fun getDueCardsFlow(currentTime: Long): Flow<List<FlashcardEntity>>

    // ---------------------------------------------------------
    // One-Shot Queries (suspend)
    // ---------------------------------------------------------

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    suspend fun getAllCards(): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY id ASC")
    suspend fun getCardsByDeck(deckName: String): List<FlashcardEntity>

    @Query("SELECT DISTINCT deckName FROM flashcards ORDER BY deckName ASC")
    suspend fun getAllDeckNames(): List<String>

    @Query("SELECT * FROM flashcards WHERE nextReviewTimestamp <= :currentTime ORDER BY nextReviewTimestamp ASC")
    suspend fun getDueCards(currentTime: Long): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckName = :deckName")
    suspend fun getCardCountByDeck(deckName: String): Int

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getTotalCardCount(): Int

    // ---------------------------------------------------------
    // Insert Operations
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>): List<Long>

    // ---------------------------------------------------------
    // Update Operations
    // ---------------------------------------------------------

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Update
    suspend fun updateCards(cards: List<FlashcardEntity>)

    /**
     * Atomically updates spaced repetition study progress metadata for a given card.
     */
    @Query("""
        UPDATE flashcards
        SET interval = :interval,
            easeFactor = :easeFactor,
            repetitions = :repetitions,
            lapses = :lapses,
            reviewCount = reviewCount + 1,
            srsState = :srsState,
            lastReviewedTimestamp = :lastReviewedTimestamp,
            nextReviewTimestamp = :nextReviewTimestamp
        WHERE id = :cardId
    """)
    suspend fun updateStudyProgress(
        cardId: Long,
        interval: Int,
        easeFactor: Double,
        repetitions: Int,
        lapses: Int,
        srsState: Int,
        lastReviewedTimestamp: Long,
        nextReviewTimestamp: Long
    )

    // ---------------------------------------------------------
    // Delete Operations
    // ---------------------------------------------------------

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM flashcards WHERE deckName = :deckName")
    suspend fun deleteCardsByDeck(deckName: String)

    @Query("DELETE FROM flashcards")
    suspend fun deleteAllCards()
}
