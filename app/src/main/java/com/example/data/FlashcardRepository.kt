package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

class FlashcardRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao
) {
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
