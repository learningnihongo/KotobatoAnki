package com.example.util

import com.example.data.CardEntity
import kotlin.random.Random

/**
 * Intelligent Shuffle Modes for Flashcard Practice
 */
enum class ShuffleType(
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    TRUE_RANDOM(
        title = "ကျပန်း စစ်စစ်မွှေမည် (True Random)",
        subtitle = "ကတ်အားလုံးကို Fisher-Yates စနစ်ဖြင့် နေရာအသစ် ကျပန်းမွှေပါသည်",
        iconEmoji = "🔀"
    ),
    SMART_PRIORITY(
        title = "ဦးစားပေးစနစ်ဖြင့် မွှေမည် (Smart Priority)",
        subtitle = "★ ကြယ်ပွင့်နှင့် မကျက်ရသေးသော ခက်ခဲသည့်ကတ်များကို ရှေ့ဆုံးထားမွှေပါသည်",
        iconEmoji = "⭐"
    ),
    UNMASTERED_ONLY(
        title = "မကျက်ရသေးသော ကတ်များသာ (Unmastered)",
        subtitle = "ကျက်ပြီးသော (Mastered) ကတ်များကို ဖယ်ထုတ်၍ မကျက်ရသေးသည်များကိုသာ မွှေပါသည်",
        iconEmoji = "🎯"
    ),
    REMAINING_ONLY(
        title = "ကျန်ရှိသောကတ်များကိုသာ မွှေမည် (Remaining Cards)",
        subtitle = "လေ့လာပြီးကတ်များကို မဖျက်ဘဲ လက်ရှိနေရာမှ ရှေ့ဆက်ကျန်ရှိသည်များကိုသာ မွှေပါသည်",
        iconEmoji = "⏩"
    ),
    RESET_ORIGINAL(
        title = "မူလအစီအစဉ်သို့ ပြန်ထားမည် (Original Order)",
        subtitle = "Deck အတွင်းရှိ မူလသတ်မှတ်ထားသော ဝေါဟာရအစဉ်လိုက်အတိုင်း ပြန်ထားပါသည်",
        iconEmoji = "↩️"
    )
}

object ShuffleHelper {

    /**
     * Executes intelligent card shuffling based on the chosen [ShuffleType].
     * Guarantees that if list size > 1 and a [currentCard] is provided,
     * the new first card is distinct from [currentCard] to ensure immediate visual feedback.
     */
    fun shuffleCards(
        cards: List<CardEntity>,
        currentCard: CardEntity? = null,
        type: ShuffleType = ShuffleType.TRUE_RANDOM
    ): List<CardEntity> {
        if (cards.size <= 1) return cards

        return when (type) {
            ShuffleType.TRUE_RANDOM -> {
                val shuffled = cards.shuffled()
                ensureDifferentFirst(shuffled, currentCard)
            }

            ShuffleType.SMART_PRIORITY -> {
                // Tier 1: Starred cards (Highest priority)
                val starred = cards.filter { it.isStarred }.shuffled()
                // Tier 2: New / unlearned cards (SRS 0 or 1)
                val unlearned = cards.filter { !it.isStarred && it.srsLevel <= 1 }.shuffled()
                // Tier 3: Intermediate review cards (SRS 2)
                val intermediate = cards.filter { !it.isStarred && it.srsLevel == 2 }.shuffled()
                // Tier 4: Mastered cards (SRS 3)
                val mastered = cards.filter { !it.isStarred && it.srsLevel >= 3 }.shuffled()

                val combined = starred + unlearned + intermediate + mastered
                ensureDifferentFirst(combined, currentCard)
            }

            ShuffleType.UNMASTERED_ONLY -> {
                val unmastered = cards.filter { it.srsLevel < 3 }
                val target = if (unmastered.isNotEmpty()) unmastered else cards
                val shuffled = target.shuffled()
                ensureDifferentFirst(shuffled, currentCard)
            }

            ShuffleType.REMAINING_ONLY -> {
                cards.shuffled()
            }

            ShuffleType.RESET_ORIGINAL -> {
                cards.sortedWith(
                    compareBy<CardEntity> { it.orderIndex }
                        .thenBy { it.id }
                )
            }
        }
    }

    /**
     * Shuffles only the remaining cards starting from [fromIndex] up to the end of the list.
     * Retains all cards from 0 until [fromIndex] unchanged.
     */
    fun shuffleRemainingCards(
        cards: List<CardEntity>,
        fromIndex: Int
    ): List<CardEntity> {
        if (fromIndex >= cards.size - 1) return cards
        val completedPart = cards.subList(0, fromIndex + 1)
        val remainingPart = cards.subList(fromIndex + 1, cards.size).shuffled()
        return completedPart + remainingPart
    }

    private fun ensureDifferentFirst(
        list: List<CardEntity>,
        currentCard: CardEntity?
    ): List<CardEntity> {
        if (currentCard == null || list.size <= 1) return list
        if (list.first().id != currentCard.id) return list

        val mutable = list.toMutableList()
        val swapTarget = if (list.size > 2) Random.nextInt(1, minOf(5, list.size)) else 1
        val first = mutable[0]
        mutable[0] = mutable[swapTarget]
        mutable[swapTarget] = first
        return mutable
    }
}
