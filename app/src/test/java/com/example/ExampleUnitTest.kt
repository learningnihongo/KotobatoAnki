package com.example

import com.example.data.AnkiApkgManager
import com.example.data.CardEntity
import com.example.data.PreloadedData
import com.example.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `verify all 109 JLPT N3 vocabulary words exist and match`() {
        assertEquals(109, PreloadedData.N3_VOCABULARY_LIST.size)
        val first = PreloadedData.N3_VOCABULARY_LIST.first()
        assertEquals("血液型", first.kanji)
        assertEquals("けつけきがた", first.kana)
        assertEquals("သွေးအမျိုးအစား", first.burmese)

        val last = PreloadedData.N3_VOCABULARY_LIST.last()
        assertEquals("事務", last.kanji)
        assertEquals("じむ", last.kana)
        assertEquals("ရုံးလုပ်ငန်း", last.burmese)
    }

    @Test
    fun `verify Anki TSV text export`() {
        val sampleCards = listOf(
            CardEntity(deckId = 1, kanji = "血液型", kana = "けつけきがた", burmeseMeaning = "သွေးအမျိုးအစား"),
            CardEntity(deckId = 1, kanji = "湖", kana = "みずうみ", burmeseMeaning = "ရေကန်")
        )
        val tsv = AnkiApkgManager.exportToTsvText(sampleCards)
        assertTrue(tsv.contains("血液型 [けつけきがた]\tသွေးအမျိုးအစား\tJLPT_N3"))
        assertTrue(tsv.contains("湖 [みずうみ]\tရေကန်\tJLPT_N3"))
    }

    @Test
    fun `verify FlashcardEntity fields and study progress metadata`() {
        val now = System.currentTimeMillis()
        val card = com.example.data.FlashcardEntity(
            front = "食べる",
            back = "စားသည်",
            deckName = "Japanese Verbs",
            interval = 4,
            easeFactor = 2.6,
            repetitions = 2,
            lapses = 0,
            reviewCount = 3,
            srsState = 2,
            lastReviewedTimestamp = now,
            nextReviewTimestamp = now + (4 * 24 * 60 * 60 * 1000L)
        )

        assertEquals("食べる", card.front)
        assertEquals("စားသည်", card.back)
        assertEquals("Japanese Verbs", card.deckName)
        assertEquals(4, card.interval)
        assertEquals(2.6, card.easeFactor, 0.001)
        assertEquals(2, card.repetitions)
        assertEquals(0, card.lapses)
        assertEquals(3, card.reviewCount)
        assertEquals(2, card.srsState)
        assertEquals(now, card.lastReviewedTimestamp)
        assertTrue(card.nextReviewTimestamp > now)
    }

    @Test
    fun `verify FlashcardEntity and CardEntity interoperability`() {
        val flashcard = com.example.data.FlashcardEntity(
            id = 42L,
            front = "横断歩道",
            back = "လူကူးမျဉ်းကျား",
            deckName = "JLPT N3"
        )

        val cardEntity = flashcard.toCardEntity(deckId = 10L)
        assertEquals("横断歩道", cardEntity.front)
        assertEquals("လူကူးမျဉ်းကျား", cardEntity.back)
        assertEquals("横断歩道", cardEntity.kanji)
        assertEquals("လူကူးမျဉ်းကျား", cardEntity.burmeseMeaning)
        assertEquals(10L, cardEntity.deckId)
    }

    @Test
    fun `verify addFlashcard saves to FlashcardDao with proper defaults`() = kotlinx.coroutines.runBlocking {
        val fakeFlashcards = mutableListOf<FlashcardEntity>()
        val fakeFlashcardDao = object : FlashcardDao {
            override fun getAllCardsFlow() = kotlinx.coroutines.flow.flowOf(fakeFlashcards)
            override fun getCardsByDeckFlow(deckName: String) = kotlinx.coroutines.flow.flowOf(fakeFlashcards.filter { it.deckName == deckName })
            override fun getAllDeckNamesFlow() = kotlinx.coroutines.flow.flowOf(fakeFlashcards.map { it.deckName }.distinct())
            override fun getDueCardsFlow(currentTime: Long) = kotlinx.coroutines.flow.flowOf(fakeFlashcards.filter { it.nextReviewTimestamp <= currentTime })
            override suspend fun getAllCards() = fakeFlashcards
            override suspend fun getCardById(cardId: Long) = fakeFlashcards.find { it.id == cardId }
            override suspend fun getCardsByDeck(deckName: String) = fakeFlashcards.filter { it.deckName == deckName }
            override suspend fun getAllDeckNames() = fakeFlashcards.map { it.deckName }.distinct()
            override suspend fun getDueCards(currentTime: Long) = fakeFlashcards.filter { it.nextReviewTimestamp <= currentTime }
            override suspend fun getCardCountByDeck(deckName: String) = fakeFlashcards.count { it.deckName == deckName }
            override suspend fun getTotalCardCount() = fakeFlashcards.size
            override suspend fun insertCard(card: FlashcardEntity): Long {
                val newId = (fakeFlashcards.size + 1).toLong()
                fakeFlashcards.add(card.copy(id = newId))
                return newId
            }
            override suspend fun insertCards(cards: List<FlashcardEntity>): List<Long> {
                return cards.map { insertCard(it) }
            }
            override suspend fun updateCard(card: FlashcardEntity) {}
            override suspend fun updateCards(cards: List<FlashcardEntity>) {}
            override suspend fun updateStudyProgress(
                cardId: Long, interval: Int, easeFactor: Double, repetitions: Int,
                lapses: Int, srsState: Int, lastReviewedTimestamp: Long, nextReviewTimestamp: Long
            ) {}
            override suspend fun deleteCard(card: FlashcardEntity) { fakeFlashcards.removeIf { it.id == card.id } }
            override suspend fun deleteCardById(cardId: Long) { fakeFlashcards.removeIf { it.id == cardId } }
            override suspend fun deleteCardsByDeck(deckName: String) { fakeFlashcards.removeIf { it.deckName == deckName } }
            override suspend fun deleteAllCards() { fakeFlashcards.clear() }
        }

        val fakeDecks = mutableListOf(DeckEntity(id = 1L, name = "2015/12 JLPT N3 Vocabulary"))
        val fakeDeckDao = object : DeckDao {
            override fun getAllDecksFlow() = kotlinx.coroutines.flow.flowOf(fakeDecks)
            override suspend fun getAllDecks() = fakeDecks
            override suspend fun getDeckById(deckId: Long) = fakeDecks.find { it.id == deckId }
            override suspend fun insertDeck(deck: DeckEntity): Long {
                val newId = (fakeDecks.size + 1).toLong()
                fakeDecks.add(deck.copy(id = newId))
                return newId
            }
            override suspend fun updateDeck(deck: DeckEntity) {}
            override suspend fun deleteDeck(deck: DeckEntity) {}
            override suspend fun getDeckCount() = fakeDecks.size
        }

        val fakeCards = mutableListOf<CardEntity>()
        val fakeCardDao = object : CardDao {
            override fun getCardsForDeckFlow(deckId: Long) = kotlinx.coroutines.flow.flowOf(fakeCards.filter { it.deckId == deckId })
            override suspend fun getCardsForDeck(deckId: Long) = fakeCards.filter { it.deckId == deckId }
            override suspend fun getCardById(cardId: Long) = fakeCards.find { it.id == cardId }
            override fun getStarredCardsFlow(deckId: Long) = kotlinx.coroutines.flow.flowOf(fakeCards.filter { it.deckId == deckId && it.isStarred })
            override suspend fun getAllCards() = fakeCards
            override suspend fun insertCard(card: CardEntity): Long {
                val newId = (fakeCards.size + 1).toLong()
                fakeCards.add(card.copy(id = newId))
                return newId
            }
            override suspend fun insertCards(cards: List<CardEntity>) = cards.map { insertCard(it) }
            override suspend fun updateCard(card: CardEntity) {}
            override suspend fun deleteCard(card: CardEntity) {}
            override suspend fun deleteCardsForDeck(deckId: Long) {}
            override suspend fun getCardCountForDeck(deckId: Long) = fakeCards.count { it.deckId == deckId }
            override suspend fun getMasteredCardCountForDeck(deckId: Long) = fakeCards.count { it.deckId == deckId && it.srsLevel == 3 }
        }

        val repository = FlashcardRepository(fakeDeckDao, fakeCardDao, fakeFlashcardDao)
        val added = repository.addFlashcard(
            front = "守る",
            back = "လိုက်နာသည်",
            deckName = "2015/12 JLPT N3 Vocabulary",
            notes = "ルールを守る"
        )

        assertEquals("守る", added.front)
        assertEquals("လိုက်နာသည်", added.back)
        assertEquals("2015/12 JLPT N3 Vocabulary", added.deckName)
        assertEquals(0, added.interval)
        assertEquals(2.5, added.easeFactor, 0.001)
        assertEquals(0, added.repetitions)
        assertEquals(0, added.srsState)

        // Verify it was stored in FlashcardDao
        assertEquals(1, fakeFlashcards.size)
        assertEquals("守る", fakeFlashcards[0].front)
        assertEquals("လိုက်နာသည်", fakeFlashcards[0].back)

        // Verify it was synchronized into CardDao for existing deck
        assertEquals(1, fakeCards.size)
        assertEquals("守る", fakeCards[0].kanji)
        assertEquals("လိုက်နာသည်", fakeCards[0].burmeseMeaning)
    }
}

