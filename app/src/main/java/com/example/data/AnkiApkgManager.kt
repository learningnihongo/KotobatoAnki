package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object AnkiApkgManager {
    private const val TAG = "AnkiApkgManager"

    data class ImportResult(
        val deckName: String,
        val cards: List<CardEntity>
    )

    /**
     * Exports cards to a valid Anki .apkg file (ZIP containing collection.anki2 SQLite database and media json).
     */
    fun exportToApkg(
        context: Context,
        deck: DeckEntity,
        cards: List<CardEntity>
    ): File {
        val cacheDir = context.cacheDir
        val exportDir = File(cacheDir, "anki_export_${System.currentTimeMillis()}").apply { mkdirs() }
        val ankiDbFile = File(exportDir, "collection.anki2")
        val mediaFile = File(exportDir, "media")

        // 1. Write media file with empty JSON mapping
        mediaFile.writeText("{}")

        // 2. Create collection.anki2 SQLite Database
        if (ankiDbFile.exists()) ankiDbFile.delete()
        val db = SQLiteDatabase.openOrCreateDatabase(ankiDbFile, null)
        try {
            createAnkiSchema(db)

            val nowSec = System.currentTimeMillis() / 1000
            val nowMs = System.currentTimeMillis()
            val deckAnkiId = 1600000000001L
            val modelId = 1600000000000L

            // Insert into col
            val colSql = """
                INSERT INTO col (id, crt, mod, scm, ver, dty, usn, ls, conf, models, decks, dconf, tags)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val sanitizedDeckName = deck.name.replace("\"", "\\\"")
            val sanitizedDesc = deck.description.replace("\"", "\\\"")

            val modelsJson = """
                {
                  "$modelId": {
                    "id": $modelId,
                    "name": "Basic (Japanese-Burmese)",
                    "type": 0,
                    "mod": $nowSec,
                    "usn": -1,
                    "sortf": 0,
                    "did": $deckAnkiId,
                    "tmpls": [
                      {
                        "name": "Card 1",
                        "ord": 0,
                        "qfmt": "{{Front}}",
                        "afmt": "{{FrontSide}}<hr id=answer><div style='margin-top:16px; font-size:22px; color:#1E1B4B;'>{{Back}}</div>",
                        "did": null
                      }
                    ],
                    "flds": [
                      {"name": "Front", "ord": 0, "sticky": false, "rtl": false, "font": "Arial", "size": 24},
                      {"name": "Back", "ord": 1, "sticky": false, "rtl": false, "font": "Arial", "size": 22}
                    ],
                    "css": ".card { font-family: arial, sans-serif; font-size: 22px; text-align: center; color: black; background-color: #FAFAFA; padding: 20px; }"
                  }
                }
            """.trimIndent().replace("\n", "")

            val decksJson = """
                {
                  "1": {"id": 1, "name": "Default", "mod": $nowSec, "usn": 0, "desc": "", "collapsed": false},
                  "$deckAnkiId": {
                    "id": $deckAnkiId,
                    "name": "$sanitizedDeckName",
                    "mod": $nowSec,
                    "usn": -1,
                    "desc": "$sanitizedDesc",
                    "collapsed": false
                  }
                }
            """.trimIndent().replace("\n", "")

            val dconfJson = """
                {
                  "1": {
                    "id": 1,
                    "name": "Default",
                    "mod": $nowSec,
                    "usn": 0,
                    "maxTaken": 60,
                    "autoplay": true,
                    "timer": 0,
                    "replayq": true,
                    "new": {"bury": false, "delays": [1.0, 10.0], "initialFactor": 2500, "ints": [1, 4, 0], "order": 1, "perDay": 20},
                    "lapse": {"delays": [10.0], "leechAction": 0, "leechFails": 8, "minInt": 1, "mult": 0.0},
                    "rev": {"bury": false, "ease4": 1.3, "fuzz": 0.05, "ivlFct": 1.0, "maxIvl": 36500, "minSpace": 1, "perDay": 200}
                  }
                }
            """.trimIndent().replace("\n", "")

            val confJson = """{"nextPos": 1, "estTimes": true, "activeDecks": [$deckAnkiId], "sortType": "noteFld", "timeLim": 0, "sortBackwards": false, "addToCur": true, "curDeck": $deckAnkiId, "curModel": $modelId, "collapseTime": 1200}"""

            db.execSQL(
                colSql,
                arrayOf(
                    1L,
                    nowSec,
                    nowMs,
                    nowMs,
                    11,
                    0,
                    0,
                    0,
                    confJson,
                    modelsJson,
                    decksJson,
                    dconfJson,
                    "{}"
                )
            )

            // Insert Notes and Cards
            db.beginTransaction()
            try {
                val noteStmt = db.compileStatement(
                    """
                    INSERT INTO notes (id, guid, mid, mod, usn, tags, flds, sfld, csum, flags, data)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                )
                val cardStmt = db.compileStatement(
                    """
                    INSERT INTO cards (id, nid, did, ord, mod, usn, type, queue, due, ivl, factor, reps, lapses, left, odue, odid, flags, data)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                )

                var baseId = nowMs
                cards.forEachIndexed { index, card ->
                    val noteId = baseId + (index * 2)
                    val cardId = noteId + 1

                    // Generate Front and Back HTML
                    val frontHtml = buildString {
                        if (card.kanji.isNotBlank() && card.kanji != card.kana) {
                            append("<div style='font-size:32px; font-weight:bold; color:#1E1B4B;'>${escapeHtml(card.kanji)}</div>")
                            append("<div style='font-size:18px; color:#475569; margin-top:4px;'>${escapeHtml(card.kana)}</div>")
                        } else {
                            append("<div style='font-size:30px; font-weight:bold; color:#1E1B4B;'>${escapeHtml(card.kana)}</div>")
                        }
                    }
                    val backHtml = "<div style='font-size:22px; color:#0F172A; line-height:1.4;'>${escapeHtml(card.burmeseMeaning)}</div>"

                    val flds = "$frontHtml\u001f$backHtml"
                    val sfld = if (card.kanji.isNotBlank()) card.kanji else card.kana
                    val guid = generateAnkiGuid()

                    noteStmt.bindLong(1, noteId)
                    noteStmt.bindString(2, guid)
                    noteStmt.bindLong(3, modelId)
                    noteStmt.bindLong(4, nowSec)
                    noteStmt.bindLong(5, -1)
                    noteStmt.bindString(6, " JLPT_N3 Japanese_Myanmar ")
                    noteStmt.bindString(7, flds)
                    noteStmt.bindString(8, sfld)
                    noteStmt.bindLong(9, (sfld.hashCode().toLong() and 0xFFFFFFFFL))
                    noteStmt.bindLong(10, 0)
                    noteStmt.bindString(11, "")
                    noteStmt.executeInsert()

                    cardStmt.bindLong(1, cardId)
                    cardStmt.bindLong(2, noteId)
                    cardStmt.bindLong(3, deckAnkiId)
                    cardStmt.bindLong(4, 0)
                    cardStmt.bindLong(5, nowSec)
                    cardStmt.bindLong(6, -1)
                    cardStmt.bindLong(7, 0)
                    cardStmt.bindLong(8, 0)
                    cardStmt.bindLong(9, (index + 1).toLong())
                    cardStmt.bindLong(10, 0)
                    cardStmt.bindLong(11, 2500)
                    cardStmt.bindLong(12, 0)
                    cardStmt.bindLong(13, 0)
                    cardStmt.bindLong(14, 0)
                    cardStmt.bindLong(15, 0)
                    cardStmt.bindLong(16, 0)
                    cardStmt.bindLong(17, 0)
                    cardStmt.bindString(18, "")
                    cardStmt.executeInsert()
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } finally {
            db.close()
        }

        // 3. Zip into .apkg file
        val cleanName = deck.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val finalApkgFile = File(cacheDir, "${cleanName}.apkg")
        if (finalApkgFile.exists()) finalApkgFile.delete()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(finalApkgFile))).use { zos ->
            // Add collection.anki2
            val ankiEntry = ZipEntry("collection.anki2")
            zos.putNextEntry(ankiEntry)
            FileInputStream(ankiDbFile).use { it.copyTo(zos) }
            zos.closeEntry()

            // Add media
            val mediaEntry = ZipEntry("media")
            zos.putNextEntry(mediaEntry)
            FileInputStream(mediaFile).use { it.copyTo(zos) }
            zos.closeEntry()
        }

        // Clean up temporary sqlite & media
        ankiDbFile.delete()
        mediaFile.delete()
        exportDir.delete()

        return finalApkgFile
    }

    /**
     * Imports an Anki .apkg file or TSV/CSV text file.
     */
    fun importFromUri(context: Context, uri: Uri): ImportResult {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_deck"

        val tempImportDir = File(context.cacheDir, "anki_import_${System.currentTimeMillis()}").apply { mkdirs() }
        val tempInputFile = File(tempImportDir, "input_file")

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempInputFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Could not read file from selected URI")

        try {
            // Check if it's a ZIP archive (starts with PK)
            val isZip = isZipFile(tempInputFile)
            if (isZip) {
                return importFromApkgFile(tempImportDir, tempInputFile, fileName)
            } else {
                // Parse as TSV / Text
                return importFromTextFile(tempInputFile, fileName)
            }
        } finally {
            tempImportDir.deleteRecursively()
        }
    }

    private fun importFromApkgFile(importDir: File, apkgFile: File, rawFileName: String): ImportResult {
        // Unzip
        val unzippedDir = File(importDir, "unzipped").apply { mkdirs() }
        ZipInputStream(BufferedInputStream(FileInputStream(apkgFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(unzippedDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { zis.copyTo(it) }
                }
                entry = zis.nextEntry
            }
        }

        val anki2File = File(unzippedDir, "collection.anki2")
        val anki21File = File(unzippedDir, "collection.anki21")
        val dbFile = when {
            anki21File.exists() -> anki21File
            anki2File.exists() -> anki2File
            else -> throw IllegalArgumentException("Invalid .apkg archive: collection.anki2 not found")
        }

        val parsedCards = mutableListOf<CardEntity>()
        var deckName = rawFileName.removeSuffix(".apkg").replace('_', ' ')

        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            // Try extracting deck name from col table
            try {
                val cursorCol = db.rawQuery("SELECT decks FROM col LIMIT 1", null)
                if (cursorCol.moveToFirst()) {
                    val decksJson = cursorCol.getString(0)
                    val extractedName = extractDeckNameFromJson(decksJson)
                    if (extractedName.isNotBlank() && extractedName != "Default") {
                        deckName = extractedName
                    }
                }
                cursorCol.close()
            } catch (e: Exception) {
                Log.w(TAG, "Could not extract deck name from col", e)
            }

            // Query notes
            val cursorNotes = db.rawQuery("SELECT flds FROM notes", null)
            var order = 1
            while (cursorNotes.moveToNext()) {
                val flds = cursorNotes.getString(0) ?: ""
                val parts = flds.split("\u001f")
                if (parts.isNotEmpty()) {
                    val frontRaw = parts[0]
                    val backRaw = if (parts.size > 1) parts[1] else ""

                    val (kanji, kana) = parseFrontField(frontRaw)
                    val meaning = cleanHtml(backRaw)

                    if (kana.isNotBlank() || kanji.isNotBlank() || meaning.isNotBlank()) {
                        parsedCards.add(
                            CardEntity(
                                deckId = 0,
                                kanji = kanji,
                                kana = if (kana.isBlank()) kanji else kana,
                                burmeseMeaning = meaning,
                                orderIndex = order++
                            )
                        )
                    }
                }
            }
            cursorNotes.close()
        } finally {
            db.close()
        }

        if (parsedCards.isEmpty()) {
            throw IllegalArgumentException("No cards found in the .apkg file")
        }

        return ImportResult(deckName = deckName, cards = parsedCards)
    }

    private fun importFromTextFile(file: File, rawFileName: String): ImportResult {
        val cards = mutableListOf<CardEntity>()
        val lines = file.readLines()
        var order = 1
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) continue

            // Split by tab or pipe or comma
            val tokens = when {
                trimmed.contains('\t') -> trimmed.split('\t')
                trimmed.contains('|') -> trimmed.split('|')
                trimmed.contains(',') -> trimmed.split(',')
                else -> listOf(trimmed)
            }

            if (tokens.isNotEmpty()) {
                val front = cleanHtml(tokens[0].trim())
                val back = if (tokens.size > 1) cleanHtml(tokens[1].trim()) else ""
                val third = if (tokens.size > 2) cleanHtml(tokens[2].trim()) else ""

                val (kanji, kana) = if (third.isNotBlank()) {
                    Pair(front, back)
                } else {
                    parseFrontField(front)
                }
                val meaning = if (third.isNotBlank()) third else back

                cards.add(
                    CardEntity(
                        deckId = 0,
                        kanji = kanji,
                        kana = if (kana.isBlank()) kanji else kana,
                        burmeseMeaning = meaning,
                        orderIndex = order++
                    )
                )
            }
        }

        val deckName = rawFileName
            .removeSuffix(".txt")
            .removeSuffix(".tsv")
            .removeSuffix(".csv")
            .replace('_', ' ')

        return ImportResult(deckName = deckName, cards = cards)
    }

    /**
     * Generates a TSV formatted string for easy copy/paste or export into Anki.
     */
    fun exportToTsvText(cards: List<CardEntity>): String {
        val sb = StringBuilder()
        sb.append("#separator:tab\n")
        sb.append("#html:false\n")
        sb.append("#tags column:3\n")
        cards.forEach { card ->
            val front = if (card.kanji.isNotBlank() && card.kanji != card.kana) {
                "${card.kanji} [${card.kana}]"
            } else {
                card.kana
            }
            val back = card.burmeseMeaning.replace("\t", " ")
            sb.append("$front\t$back\tJLPT_N3\n")
        }
        return sb.toString()
    }

    private fun isZipFile(file: File): Boolean {
        if (!file.exists() || file.length() < 4) return false
        FileInputStream(file).use { fis ->
            val b1 = fis.read()
            val b2 = fis.read()
            return b1 == 0x50 && b2 == 0x4B // 'P', 'K'
        }
    }

    private fun parseFrontField(raw: String): Pair<String, String> {
        val text = cleanHtml(raw)
        // Detect patterns like "Kanji [Kana]" or "Kanji (Kana)"
        val bracketMatch = Regex("([^\\[\\]()]+)[\\[(]([^\\[\\]()]+)[\\])]").find(text)
        return if (bracketMatch != null) {
            val kanji = bracketMatch.groupValues[1].trim()
            val kana = bracketMatch.groupValues[2].trim()
            Pair(kanji, kana)
        } else {
            Pair(text, text)
        }
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<hr\\s*/?>", RegexOption.IGNORE_CASE), "\n---\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br>")
    }

    private fun extractDeckNameFromJson(json: String): String {
        // Simple regex extractor for deck name: "name":\s*"([^"]+)"
        val matches = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").findAll(json)
        for (match in matches) {
            val name = match.groupValues[1]
            if (name != "Default") return name
        }
        return ""
    }

    private fun generateAnkiGuid(): String {
        return UUID.randomUUID().toString().replace("-", "").take(10)
    }

    private fun createAnkiSchema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE col (
                id              integer primary key,
                crt             integer not null,
                mod             integer not null,
                scm             integer not null,
                ver             integer not null,
                dty             integer not null,
                usn             integer not null,
                ls              integer not null,
                conf            text not null,
                models          text not null,
                decks           text not null,
                dconf           text not null,
                tags            text not null
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE notes (
                id              integer primary key,
                guid            text not null,
                mid             integer not null,
                mod             integer not null,
                usn             integer not null,
                tags            text not null,
                flds            text not null,
                sfld            text not null,
                csum            integer not null,
                flags           integer not null,
                data            text not null
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cards (
                id              integer primary key,
                nid             integer not null,
                did             integer not null,
                ord             integer not null,
                mod             integer not null,
                usn             integer not null,
                type            integer not null,
                queue           integer not null,
                due             integer not null,
                ivl             integer not null,
                factor          integer not null,
                reps            integer not null,
                lapses          integer not null,
                left            integer not null,
                odue            integer not null,
                odid            integer not null,
                flags           integer not null,
                data            text not null
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE revlog (
                id              integer primary key,
                cid             integer not null,
                usn             integer not null,
                ease            integer not null,
                ivl             integer not null,
                lastIvl         integer not null,
                factor          integer not null,
                time            integer not null,
                type            integer not null
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE graves (
                usn             integer not null,
                oid             integer not null,
                type            integer not null
            );
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX ix_notes_usn on notes (usn);")
        db.execSQL("CREATE INDEX ix_cards_usn on cards (usn);")
        db.execSQL("CREATE INDEX ix_cards_nid on cards (nid);")
        db.execSQL("CREATE INDEX ix_cards_sched on cards (did, queue, due);")
        db.execSQL("CREATE INDEX ix_revlog_usn on revlog (usn);")
        db.execSQL("CREATE INDEX ix_revlog_cid on revlog (cid);")
    }
}
