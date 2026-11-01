package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.security.MessageDigest
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
    /**
     * Exports cards to a valid Anki .apkg file (ZIP containing collection.anki2 SQLite database and media json).
     * Complies strictly with AnkiDroid & Anki Desktop Rust backend schema.
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

            val decksJson = buildDecksJson(deckAnkiId, deck.name, deck.description, nowSec)
            val modelsJson = buildModelsJson(modelId, deckAnkiId, nowSec)
            val dconfJson = buildDconfJson(nowSec)
            val confJson = buildConfJson(deckAnkiId, modelId)

            // Insert into col
            val colSql = """
                INSERT INTO col (id, crt, mod, scm, ver, dty, usn, ls, conf, models, decks, dconf, tags)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

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

                    // Generate Front and Back HTML:
                    // Front: Japanese Word (Kanji prompt, or Kana if no Kanji)
                    val frontHtml = buildString {
                        if (card.kanji.isNotBlank()) {
                            append("<div style='font-size:38px; font-weight:bold; color:#1E1B4B; text-align:center; padding:12px 0;'>${escapeHtml(card.kanji)}</div>")
                        } else {
                            append("<div style='font-size:34px; font-weight:bold; color:#1E1B4B; text-align:center; padding:12px 0;'>${escapeHtml(card.kana)}</div>")
                        }
                    }

                    // Back: Hiragana reading (Kana) AND Myanmar meaning clearly and prominently displayed together
                    val backHtml = buildString {
                        append("<div style='text-align:center;'>")
                        if (card.kana.isNotBlank()) {
                            append("<div style='font-size:13px; font-weight:bold; color:#6366F1; letter-spacing:1px; margin-bottom:4px; text-transform:uppercase;'>[ အသံထွက် / READING ]</div>")
                            append("<div style='font-size:26px; font-weight:bold; color:#4338CA; margin-bottom:14px;'>${escapeHtml(card.kana)}</div>")
                        }
                        append("<div style='font-size:13px; font-weight:bold; color:#059669; letter-spacing:1px; margin-bottom:4px; text-transform:uppercase;'>[ မြန်မာအဓိပ္ပာယ် / MEANING ]</div>")
                        append("<div style='font-size:23px; color:#0F172A; line-height:1.6; font-weight:600;'>${escapeHtml(card.burmeseMeaning)}</div>")
                        if (card.notes.isNotBlank()) {
                            append("<div style='font-size:14px; color:#475569; margin-top:14px; background:#F1F5F9; padding:8px 14px; border-radius:8px; display:inline-block;'>${escapeHtml(card.notes)}</div>")
                        }
                        append("</div>")
                    }

                    val flds = "$frontHtml\u001f$backHtml"
                    val sfld = if (card.kanji.isNotBlank()) card.kanji else card.kana
                    val guid = generateAnkiGuid()
                    val csum = calculateAnkiCsum(sfld)

                    noteStmt.bindLong(1, noteId)
                    noteStmt.bindString(2, guid)
                    noteStmt.bindLong(3, modelId)
                    noteStmt.bindLong(4, nowSec)
                    noteStmt.bindLong(5, -1)
                    noteStmt.bindString(6, " JLPT_N3 Japanese_Myanmar ")
                    noteStmt.bindString(7, flds)
                    noteStmt.bindString(8, sfld)
                    noteStmt.bindLong(9, csum)
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
        val lines = file.readLines(Charsets.UTF_8)
        var order = 1
        var isFirstLine = true

        for (rawLine in lines) {
            val line = rawLine.trim().removePrefix("\uFEFF") // Strip UTF-8 BOM if present
            if (line.isBlank() || line.startsWith("#")) continue

            val tokens = parseDelimitedLine(line)
            if (tokens.isEmpty()) continue

            // Check if first line is a header row (e.g. "kanji,kana,meaning" or "front,back")
            if (isFirstLine && isHeaderRow(tokens)) {
                isFirstLine = false
                continue
            }
            isFirstLine = false

            var kanji = ""
            var kana = ""
            var meaning = ""
            var notes = ""

            when {
                tokens.size >= 4 -> {
                    // 4 columns: Kanji, Kana, Burmese Meaning, Notes
                    kanji = cleanHtml(tokens[0])
                    kana = cleanHtml(tokens[1])
                    meaning = cleanHtml(tokens[2])
                    notes = cleanHtml(tokens[3])
                }
                tokens.size == 3 -> {
                    // 3 columns: Kanji, Kana, Burmese Meaning
                    kanji = cleanHtml(tokens[0])
                    kana = cleanHtml(tokens[1])
                    meaning = cleanHtml(tokens[2])
                }
                tokens.size == 2 -> {
                    // 2 columns: Front, Burmese Meaning
                    val front = cleanHtml(tokens[0])
                    meaning = cleanHtml(tokens[1])
                    val parsed = parseFrontField(front)
                    kanji = parsed.first
                    kana = parsed.second
                }
                tokens.size == 1 -> {
                    val front = cleanHtml(tokens[0])
                    val parsed = parseFrontField(front)
                    kanji = parsed.first
                    kana = parsed.second
                }
            }

            if (kana.isBlank() && kanji.isNotBlank()) {
                kana = kanji
            }

            if (kana.isNotBlank() || kanji.isNotBlank() || meaning.isNotBlank()) {
                cards.add(
                    CardEntity(
                        deckId = 0,
                        kanji = kanji,
                        kana = if (kana.isBlank()) kanji else kana,
                        burmeseMeaning = meaning,
                        notes = notes,
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
            .ifBlank { "Imported Flashcards" }

        return ImportResult(deckName = deckName, cards = cards)
    }

    /**
     * Intelligently parses a line formatted as CSV (comma), TSV (tab), or Pipe (|),
     * with full support for quotation marks and escaped commas.
     */
    private fun parseDelimitedLine(line: String): List<String> {
        val delimiter = when {
            line.contains('\t') -> '\t'
            line.contains('|') -> '|'
            line.contains(';') -> ';'
            else -> ','
        }

        val tokens = mutableListOf<String>()
        val current = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    tokens.add(current.toString().trim())
                    current.setLength(0)
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        tokens.add(current.toString().trim())
        return tokens
    }

    private fun isHeaderRow(tokens: List<String>): Boolean {
        if (tokens.isEmpty()) return false
        val firstCol = tokens[0].lowercase()
        val secondCol = tokens.getOrNull(1)?.lowercase() ?: ""
        return firstCol in listOf("kanji", "front", "word", "japanese", "စာလုံး", "expression") ||
               secondCol in listOf("kana", "hiragana", "reading", "back", "meaning", "အသံထွက်", "အဓိပ္ပာယ်")
    }

    /**
     * Generates a CSV formatted string with UTF-8 encoding.
     */
    fun exportToCsvText(cards: List<CardEntity>): String {
        val sb = java.lang.StringBuilder()
        sb.append("Kanji,Kana,BurmeseMeaning,Notes\n")
        cards.forEach { card ->
            val kanji = escapeCsvField(card.kanji)
            val kana = escapeCsvField(card.kana)
            val meaning = escapeCsvField(card.burmeseMeaning)
            val notes = escapeCsvField(card.notes)
            sb.append("$kanji,$kana,$meaning,$notes\n")
        }
        return sb.toString()
    }

    private fun escapeCsvField(value: String): String {
        val cleaned = value.replace("\r", "")
        return if (cleaned.contains(',') || cleaned.contains('"') || cleaned.contains('\n')) {
            "\"" + cleaned.replace("\"", "\"\"") + "\""
        } else {
            cleaned
        }
    }

    fun exportToCsvFile(context: Context, cards: List<CardEntity>, deckName: String): File {
        val cleanName = deckName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val csvFile = File(context.cacheDir, "${cleanName}_flashcards.csv")
        csvFile.writeText(exportToCsvText(cards), Charsets.UTF_8)
        return csvFile
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

    /**
     * Calculates the standard Anki note checksum: first 4 bytes of SHA-1 of the sort field.
     */
    private fun calculateAnkiCsum(sfld: String): Long {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(sfld.toByteArray(Charsets.UTF_8))
            var csum = 0L
            for (i in 0 until 4) {
                csum = (csum shl 8) or (digest[i].toLong() and 0xFFL)
            }
            csum
        } catch (e: Exception) {
            (sfld.hashCode().toLong() and 0xFFFFFFFFL)
        }
    }

    /**
     * Builds the decks JSON object required by Anki / AnkiDroid with all compulsory Rust Serde fields.
     */
    private fun buildDecksJson(
        deckId: Long,
        deckName: String,
        deckDesc: String,
        nowSec: Long
    ): String {
        val root = JSONObject()

        // Default Deck (id: 1)
        val defaultDeck = JSONObject().apply {
            put("id", 1L)
            put("mod", nowSec)
            put("name", "Default")
            put("usn", 0)
            put("lrnToday", JSONArray(listOf(0, 0)))
            put("revToday", JSONArray(listOf(0, 0)))
            put("newToday", JSONArray(listOf(0, 0)))
            put("timeToday", JSONArray(listOf(0, 0)))
            put("collapsed", true)
            put("browserCollapsed", true)
            put("desc", "")
            put("dyn", 0)
            put("conf", 1L)
            put("extendNew", 0)
            put("extendRev", 0)
        }
        root.put("1", defaultDeck)

        // Custom Deck
        val customDeck = JSONObject().apply {
            put("id", deckId)
            put("mod", nowSec)
            put("name", deckName)
            put("usn", -1)
            put("lrnToday", JSONArray(listOf(0, 0)))
            put("revToday", JSONArray(listOf(0, 0)))
            put("newToday", JSONArray(listOf(0, 0)))
            put("timeToday", JSONArray(listOf(0, 0)))
            put("collapsed", false)
            put("browserCollapsed", false)
            put("desc", deckDesc)
            put("dyn", 0)
            put("conf", 1L)
            put("extendNew", 0)
            put("extendRev", 0)
        }
        root.put(deckId.toString(), customDeck)

        return root.toString()
    }

    /**
     * Builds the models JSON object required by Anki with fields, templates, css, and req arrays.
     */
    private fun buildModelsJson(modelId: Long, deckId: Long, nowSec: Long): String {
        val root = JSONObject()

        val model = JSONObject().apply {
            put("id", modelId)
            put("name", "Basic (Japanese-Burmese)")
            put("type", 0)
            put("mod", nowSec)
            put("usn", -1)
            put("sortf", 0)
            put("did", deckId)

            val tmpls = JSONArray()
            val tmpl = JSONObject().apply {
                put("name", "Card 1")
                put("ord", 0)
                put("qfmt", "{{Front}}")
                put("afmt", "{{FrontSide}}\n\n<hr id=answer>\n\n<div style='margin-top:16px; font-size:22px; color:#1E1B4B;'>{{Back}}</div>")
                put("bqfmt", "")
                put("bafmt", "")
                put("did", JSONObject.NULL)
            }
            tmpls.put(tmpl)
            put("tmpls", tmpls)

            val flds = JSONArray()
            val fld1 = JSONObject().apply {
                put("name", "Front")
                put("ord", 0)
                put("sticky", false)
                put("rtl", false)
                put("font", "Arial")
                put("size", 24)
                put("media", JSONArray())
            }
            val fld2 = JSONObject().apply {
                put("name", "Back")
                put("ord", 1)
                put("sticky", false)
                put("rtl", false)
                put("font", "Arial")
                put("size", 22)
                put("media", JSONArray())
            }
            flds.put(fld1)
            flds.put(fld2)
            put("flds", flds)

            put("css", ".card { font-family: arial, sans-serif; font-size: 22px; text-align: center; color: black; background-color: #FAFAFA; padding: 20px; }")
            put("latexPre", "\\documentclass[12pt]{article}\n\\special{papersize=3in,5in}\n\\usepackage[utf8]{inputenc}\n\\usepackage{amssymb,amsmath}\n\\pagestyle{empty}\n\\setlength{\\parindent}{0in}\n\\begin{document}\n")
            put("latexPost", "\\end{document}")
            put("latexsvg", false)

            val reqArr = JSONArray()
            val reqEntry = JSONArray().apply {
                put(0)
                put("any")
                put(JSONArray().apply { put(0) })
            }
            reqArr.put(reqEntry)
            put("req", reqArr)
        }

        root.put(modelId.toString(), model)
        return root.toString()
    }

    /**
     * Builds the dconf JSON object containing deck configurations.
     */
    private fun buildDconfJson(nowSec: Long): String {
        val root = JSONObject()
        val conf1 = JSONObject().apply {
            put("id", 1L)
            put("mod", nowSec)
            put("name", "Default")
            put("usn", 0)
            put("maxTaken", 60)
            put("autoplay", true)
            put("timer", 0)
            put("replayq", true)

            val newConf = JSONObject().apply {
                put("bury", false)
                put("delays", JSONArray(listOf(1.0, 10.0)))
                put("initialFactor", 2500)
                put("ints", JSONArray(listOf(1, 4, 7)))
                put("order", 1)
                put("perDay", 20)
            }
            put("new", newConf)

            val revConf = JSONObject().apply {
                put("bury", false)
                put("ease4", 1.3)
                put("fuzz", 0.05)
                put("ivlFct", 1.0)
                put("maxIvl", 36500)
                put("minSpace", 1)
                put("perDay", 200)
            }
            put("rev", revConf)

            val lapseConf = JSONObject().apply {
                put("delays", JSONArray(listOf(10.0)))
                put("leechAction", 0)
                put("leechFails", 8)
                put("minInt", 1)
                put("mult", 0.0)
            }
            put("lapse", lapseConf)
        }
        root.put("1", conf1)
        return root.toString()
    }

    /**
     * Builds the col.conf JSON configuration.
     */
    private fun buildConfJson(deckId: Long, modelId: Long): String {
        val conf = JSONObject().apply {
            put("nextPos", 1)
            put("estTimes", true)
            put("activeDecks", JSONArray(listOf(1L, deckId)))
            put("sortType", "noteFld")
            put("timeLim", 0)
            put("sortBackwards", false)
            put("addToCur", true)
            put("curDeck", deckId)
            put("curModel", modelId)
            put("collapseTime", 1200)
        }
        return conf.toString()
    }

    /**
     * Generates a TSV text file on disk for sharing or manual file import into AnkiDroid.
     */
    fun exportToTextFile(context: Context, cards: List<CardEntity>, deckName: String): File {
        val cleanName = deckName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val txtFile = File(context.cacheDir, "${cleanName}_anki.txt")
        val content = exportToTsvText(cards)
        txtFile.writeText(content, Charsets.UTF_8)
        return txtFile
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
