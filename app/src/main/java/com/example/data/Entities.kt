package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val kanji: String,            // e.g. 血液型 or 比べる
    val kana: String,             // e.g. けつけきがた or くらべる
    val burmeseMeaning: String,   // e.g. သွေးအမျိုးအစား
    val notes: String = "",       // optional notes/example
    val orderIndex: Int = 0,
    val isStarred: Boolean = false,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val srsLevel: Int = 0,        // 0: New, 1: Learning, 2: Review, 3: Mastered
    val nextReviewDate: Long = 0L
)

val CardEntity.front: String
    get() = if (kanji.isNotBlank()) kanji else kana

val CardEntity.back: String
    get() = burmeseMeaning

fun FlashcardEntity.toCardEntity(deckId: Long, orderIndex: Int = 0): CardEntity {
    return CardEntity(
        id = id,
        deckId = deckId,
        kanji = front,
        kana = "",
        burmeseMeaning = back,
        orderIndex = orderIndex,
        reviewCount = reviewCount,
        srsLevel = srsState,
        nextReviewDate = nextReviewTimestamp
    )
}

fun CardEntity.toFlashcardEntity(deckName: String): FlashcardEntity {
    return FlashcardEntity(
        id = id,
        front = if (kanji.isNotBlank()) kanji else kana,
        back = burmeseMeaning,
        deckName = deckName,
        reviewCount = reviewCount,
        srsState = srsLevel,
        nextReviewTimestamp = nextReviewDate
    )
}

