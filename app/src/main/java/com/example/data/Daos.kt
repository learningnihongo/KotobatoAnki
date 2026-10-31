package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt ASC")
    fun getAllDecksFlow(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks ORDER BY createdAt ASC")
    suspend fun getAllDecks(): List<DeckEntity>

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCount(): Int
}

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY orderIndex ASC, id ASC")
    fun getCardsForDeckFlow(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY orderIndex ASC, id ASC")
    suspend fun getCardsForDeck(deckId: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND isStarred = 1")
    fun getStarredCardsFlow(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards")
    suspend fun getAllCards(): List<CardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>): List<Long>

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Long)

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    suspend fun getCardCountForDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND srsLevel = 3")
    suspend fun getMasteredCardCountForDeck(deckId: Long): Int
}
