package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

class FlashcardRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val flashcardDao: FlashcardDao? = null
) {
    fun getFlashcardDao(): FlashcardDao? = flashcardDao

    // FlashcardEntity operations
    fun getAllFlashcardsFlow(): Flow<List<FlashcardEntity>>? = flashcardDao?.getAllCardsFlow()
    fun getFlashcardsByDeckFlow(deckName: String): Flow<List<FlashcardEntity>>? = flashcardDao?.getCardsByDeckFlow(deckName)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = flashcardDao?.insertCard(flashcard) ?: -1L
    suspend fun updateFlashcard(flashcard: FlashcardEntity) { flashcardDao?.updateCard(flashcard) }
    suspend fun deleteFlashcard(flashcard: FlashcardEntity) { flashcardDao?.deleteCard(flashcard) }

    suspend fun addFlashcard(
        front: String,
        back: String,
        deckName: String,
        notes: String = ""
    ): FlashcardEntity {
        val trimmedFront = front.trim()
        val trimmedBack = back.trim()
        val trimmedDeck = deckName.trim().ifBlank { "2015/12 JLPT N3 Vocabulary" }

        val entity = FlashcardEntity(
            front = trimmedFront,
            back = trimmedBack,
            deckName = trimmedDeck,
            interval = 0,
            easeFactor = 2.5,
            repetitions = 0,
            lapses = 0,
            reviewCount = 0,
            srsState = 0,
            nextReviewTimestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        val flashcardId = flashcardDao?.insertCard(entity) ?: 0L

        // Also synchronize to DeckDao & CardDao so all UI views & games stay in sync
        val existingDecks = deckDao.getAllDecks()
        val targetDeck = existingDecks.firstOrNull { it.name.equals(trimmedDeck, ignoreCase = true) }
        val deckId = targetDeck?.id ?: deckDao.insertDeck(
            DeckEntity(
                name = trimmedDeck,
                description = "User created flashcard deck"
            )
        )

        val cardEntity = CardEntity(
            deckId = deckId,
            kanji = trimmedFront,
            kana = "",
            burmeseMeaning = trimmedBack,
            notes = notes
        )
        cardDao.insertCard(cardEntity)

        return entity.copy(id = flashcardId)
    }
    suspend fun updateStudyProgress(
        cardId: Long,
        interval: Int,
        easeFactor: Double,
        repetitions: Int,
        lapses: Int,
        srsState: Int,
        lastReviewedTimestamp: Long,
        nextReviewTimestamp: Long
    ) {
        flashcardDao?.updateStudyProgress(
            cardId, interval, easeFactor, repetitions, lapses, srsState, lastReviewedTimestamp, nextReviewTimestamp
        )
    }
    fun getAllDecks(): Flow<List<DeckEntity>> = deckDao.getAllDecksFlow()

    suspend fun getDeckById(deckId: Long): DeckEntity? = deckDao.getDeckById(deckId)

    suspend fun insertDeck(deck: DeckEntity): Long = deckDao.insertDeck(deck)

    suspend fun updateDeck(deck: DeckEntity) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(deck: DeckEntity) = deckDao.deleteDeck(deck)

    fun getCardsForDeckFlow(deckId: Long): Flow<List<CardEntity>> = cardDao.getCardsForDeckFlow(deckId)

    suspend fun getCardsForDeck(deckId: Long): List<CardEntity> = cardDao.getCardsForDeck(deckId)

    suspend fun insertCard(card: CardEntity): Long = cardDao.insertCard(card)

    suspend fun insertCards(cards: List<CardEntity>): List<Long> = cardDao.insertCards(cards)

    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)

    suspend fun deleteCard(card: CardEntity) = cardDao.deleteCard(card)

    suspend fun getCardCountForDeck(deckId: Long): Int = cardDao.getCardCountForDeck(deckId)

    suspend fun getMasteredCountForDeck(deckId: Long): Int = cardDao.getMasteredCardCountForDeck(deckId)

    suspend fun updateCardSrs(card: CardEntity, rating: Int) {
        // rating: 1 = Again, 2 = Hard, 3 = Good, 4 = Easy
        val newReviewCount = card.reviewCount + 1
        val newCorrectCount = if (rating >= 3) card.correctCount + 1 else card.correctCount
        val newSrsLevel = when (rating) {
            1 -> 1 // Still learning
            2 -> card.srsLevel.coerceAtLeast(1)
            3 -> (card.srsLevel + 1).coerceAtMost(3)
            4 -> 3 // Mastered
            else -> card.srsLevel
        }
        val intervalDays = when (newSrsLevel) {
            1 -> 1
            2 -> 3
            3 -> 7
            else -> 1
        }
        val nextReview = System.currentTimeMillis() + (intervalDays * 24 * 60 * 60 * 1000L)

        cardDao.updateCard(
            card.copy(
                reviewCount = newReviewCount,
                correctCount = newCorrectCount,
                srsLevel = newSrsLevel,
                nextReviewDate = nextReview
            )
        )
    }

    suspend fun toggleStar(card: CardEntity) {
        cardDao.updateCard(card.copy(isStarred = !card.isStarred))
    }

    fun exportToApkg(context: Context, deck: DeckEntity, cards: List<CardEntity>): File {
        return AnkiApkgManager.exportToApkg(context, deck, cards)
    }

    fun exportToTextFile(context: Context, cards: List<CardEntity>, deckName: String): File {
        return AnkiApkgManager.exportToTextFile(context, cards, deckName)
    }

    suspend fun importFromUri(context: Context, uri: Uri): Pair<DeckEntity, Int> {
        val importResult = AnkiApkgManager.importFromUri(context, uri)
        val newDeckId = deckDao.insertDeck(
            DeckEntity(
                name = importResult.deckName.ifBlank { "Imported Anki Deck" },
                description = "Imported from Anki (.apkg/tsv) with ${importResult.cards.size} cards."
            )
        )
        val cardsToInsert = importResult.cards.map { it.copy(deckId = newDeckId) }
        cardDao.insertCards(cardsToInsert)
        val deck = deckDao.getDeckById(newDeckId) ?: DeckEntity(id = newDeckId, name = importResult.deckName)
        return Pair(deck, cardsToInsert.size)
    }

    fun exportToTsv(cards: List<CardEntity>): String {
        return AnkiApkgManager.exportToTsvText(cards)
    }
}
