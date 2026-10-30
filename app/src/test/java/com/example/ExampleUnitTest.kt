package com.example

import com.example.data.AnkiApkgManager
import com.example.data.CardEntity
import com.example.data.PreloadedData
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
}
