package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.ui.SoundHelper
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import com.example.ui.theme.HardOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TileType { JAPANESE, MEANING }

data class MatchTile(
    val id: String,
    val card: CardEntity,
    val text: String,
    val subText: String = "",
    val type: TileType,
    val isMatched: Boolean = false,
    val isHinted: Boolean = false
)

enum class MatchDifficulty(val pairs: Int, val label: String) {
    EASY(4, "လွယ်ကူ (4 Pairs)"),
    NORMAL(6, "ပုံမှန် (6 Pairs)"),
    MASTER(8, "ကျွမ်းကျင် (8 Pairs)")
}

enum class MatchTargetType(val label: String) {
    BURMESE("ဂျပန် ↔ မြန်မာအဓိပ္ပာယ်"),
    KANA_READING("Kanji ↔ Kana အသံထွက်")
}

@Composable
fun MatchGameScreen(
    decks: List<DeckEntity>,
    cardsByDeck: Map<Long, List<CardEntity>>,
    initialDeck: DeckEntity? = null,
    soundHelper: SoundHelper,
    onToggleStar: (CardEntity) -> Unit = {},
    onBack: () -> Unit
) {
    // Deck selection: null represents "All Cards (including imported)"
    var selectedDeckId by remember { mutableStateOf<Long?>(initialDeck?.id) }
    var selectedDifficulty by remember { mutableStateOf(MatchDifficulty.NORMAL) }
    var selectedTargetType by remember { mutableStateOf(MatchTargetType.BURMESE) }

    val allCards = remember(cardsByDeck) { cardsByDeck.values.flatten() }

    val activeDeckCards = remember(selectedDeckId, cardsByDeck, allCards) {
        if (selectedDeckId == null || selectedDeckId == -998L) {
            allCards
        } else if (selectedDeckId == -999L) {
            allCards.filter { it.isStarred }
        } else {
            cardsByDeck[selectedDeckId] ?: allCards
        }
    }

    if (activeDeckCards.size < 4) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ရွေးချယ်ထားသော စာရင်းတွင် ကစားရန် အနည်းဆုံး ကတ် ၄ ခု လိုအပ်ပါသည်",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { selectedDeckId = null }) {
                    Text("ဝေါဟာရအားလုံးဖြင့် ကစားမည်")
                }
                OutlinedButton(onClick = onBack) {
                    Text("နောက်သို့ ပြန်သွားမည်")
                }
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val pairCount = selectedDifficulty.pairs.coerceAtMost(activeDeckCards.size)

    var tiles by remember(activeDeckCards, selectedDifficulty, selectedTargetType) {
        mutableStateOf(generateIntelligentTiles(activeDeckCards, pairCount, selectedTargetType))
    }
    var selectedTile1 by remember { mutableStateOf<MatchTile?>(null) }
    var selectedTile2 by remember { mutableStateOf<MatchTile?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    var isGameWon by remember { mutableStateOf(false) }

    // Intelligence Metrics
    var comboCount by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var hintsRemaining by remember { mutableIntStateOf(3) }
    val mismatchedCards = remember { mutableStateListOf<CardEntity>() }

    // Timer
    LaunchedEffect(isGameWon) {
        while (!isGameWon) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val restartGame = {
        tiles = generateIntelligentTiles(activeDeckCards, pairCount, selectedTargetType)
        selectedTile1 = null
        selectedTile2 = null
        moves = 0
        matchedPairs = 0
        elapsedSeconds = 0
        isChecking = false
        isGameWon = false
        comboCount = 0
        maxCombo = 0
        hintsRemaining = 3
        mismatchedCards.clear()
    }

    if (isGameWon) {
        IntelligentMatchVictoryView(
            moves = moves,
            seconds = elapsedSeconds,
            maxCombo = maxCombo,
            mismatchedCards = mismatchedCards.distinctBy { it.id },
            soundHelper = soundHelper,
            onToggleStar = onToggleStar,
            onRestart = restartGame,
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("match_game_screen")
    ) {
        // 1. Top Bar: Back, Timer, Moves, Combo Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("match_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Timer & Combo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${elapsedSeconds}s",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (comboCount >= 2) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = HardOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = HardOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "x$comboCount Combo!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = HardOrange
                            )
                        }
                    }
                }
            }

            // Smart Hint Button
            OutlinedButton(
                onClick = {
                    if (hintsRemaining > 0 && !isChecking) {
                        val unmatched = tiles.filter { !it.isMatched }
                        val firstUnmatched = unmatched.firstOrNull { it.type == TileType.JAPANESE }
                        val matchingPair = unmatched.firstOrNull { it.card.id == firstUnmatched?.card?.id && it.type == TileType.MEANING }
                        if (firstUnmatched != null && matchingPair != null) {
                            hintsRemaining--
                            tiles = tiles.map {
                                if (it.id == firstUnmatched.id || it.id == matchingPair.id) it.copy(isHinted = true) else it
                            }
                            scope.launch {
                                delay(1200L)
                                tiles = tiles.map { it.copy(isHinted = false) }
                            }
                        }
                    }
                },
                enabled = hintsRemaining > 0 && !isChecking,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("smart_hint_button")
            ) {
                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldStar)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hint ($hintsRemaining)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Deck & Import Selector Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedDeckId == null || selectedDeckId == -998L,
                    onClick = {
                        selectedDeckId = null
                        restartGame()
                    },
                    label = { Text("အားလုံးပေါင်း (${allCards.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }

            item {
                FilterChip(
                    selected = selectedDeckId == -999L,
                    onClick = {
                        selectedDeckId = -999L
                        restartGame()
                    },
                    label = { Text("★ Starred (${allCards.count { it.isStarred }})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldStar.copy(alpha = 0.25f))
                )
            }

            items(decks) { deck ->
                val count = cardsByDeck[deck.id]?.size ?: 0
                FilterChip(
                    selected = selectedDeckId == deck.id,
                    onClick = {
                        selectedDeckId = deck.id
                        restartGame()
                    },
                    label = { Text("${deck.name} ($count)") }
                )
            }
        }

        // 3. Difficulty & Match Mode Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MatchDifficulty.values().forEach { diff ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedDifficulty == diff) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedDifficulty = diff
                                restartGame()
                            }
                    ) {
                        Text(
                            text = when (diff) {
                                MatchDifficulty.EASY -> "4 Pairs"
                                MatchDifficulty.NORMAL -> "6 Pairs"
                                MatchDifficulty.MASTER -> "8 Pairs"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedDifficulty == diff) FontWeight.Bold else FontWeight.Normal),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (selectedDifficulty == diff) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Target Type Toggle (Burmese vs Kana)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        selectedTargetType = if (selectedTargetType == MatchTargetType.BURMESE) MatchTargetType.KANA_READING else MatchTargetType.BURMESE
                        restartGame()
                    }
            ) {
                Text(
                    text = if (selectedTargetType == MatchTargetType.BURMESE) "မြန်မာအဓိပ္ပာယ်" else "Kana အသံထွက်",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress text
        Text(
            text = "အတွဲစပ်ပါ: $matchedPairs / $pairCount တွဲပြီး | ရွှေ့ချက်: $moves",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Tiles Grid
        val columnsCount = if (selectedDifficulty == MatchDifficulty.MASTER) 4 else 3

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tiles, key = { it.id }) { tile ->
                val isSelected = tile.id == selectedTile1?.id || tile.id == selectedTile2?.id
                val isMatched = tile.isMatched

                val tileBg by animateColorAsState(
                    targetValue = when {
                        isMatched -> EasyGreen.copy(alpha = 0.18f)
                        tile.isHinted -> GoldStar.copy(alpha = 0.35f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        tile.type == TileType.JAPANESE -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    },
                    label = "tile_bg"
                )

                val tileBorder by animateColorAsState(
                    targetValue = when {
                        isMatched -> EasyGreen
                        tile.isHinted -> GoldStar
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    },
                    label = "tile_border"
                )

                Surface(
                    onClick = {
                        if (!isChecking && !isMatched && !isSelected) {
                            if (selectedTile1 == null) {
                                selectedTile1 = tile
                                soundHelper.playFlipSound()
                                if (tile.type == TileType.JAPANESE) {
                                    val speakWord = if (tile.card.kana.isNotBlank()) tile.card.kana else tile.card.kanji
                                    soundHelper.speakJapanese(speakWord)
                                }
                            } else if (selectedTile2 == null && tile.id != selectedTile1?.id) {
                                selectedTile2 = tile
                                moves++
                                isChecking = true

                                val t1 = selectedTile1!!
                                val t2 = tile

                                if (t1.card.id == t2.card.id && t1.type != t2.type) {
                                    // Match Success!
                                    soundHelper.playCorrectSound()
                                    val speakWord = if (t1.card.kana.isNotBlank()) t1.card.kana else t1.card.kanji
                                    soundHelper.speakJapanese(speakWord)

                                    tiles = tiles.map {
                                        if (it.card.id == t1.card.id) it.copy(isMatched = true) else it
                                    }
                                    matchedPairs++
                                    comboCount++
                                    if (comboCount > maxCombo) maxCombo = comboCount

                                    selectedTile1 = null
                                    selectedTile2 = null
                                    isChecking = false
                                    if (matchedPairs == pairCount) {
                                        isGameWon = true
                                    }
                                } else {
                                    // Mismatch!
                                    soundHelper.playWrongSound()
                                    comboCount = 0
                                    mismatchedCards.add(t1.card)
                                    mismatchedCards.add(t2.card)

                                    scope.launch {
                                        delay(750L)
                                        selectedTile1 = null
                                        selectedTile2 = null
                                        isChecking = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(if (selectedDifficulty == MatchDifficulty.MASTER) 88.dp else 102.dp)
                        .testTag("match_tile_${tile.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = tileBg,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, tileBorder),
                    shadowElevation = if (isMatched) 0.dp else 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMatched) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Matched",
                                tint = EasyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tile.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = if (tile.type == TileType.JAPANESE) 16.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (tile.subText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tile.subText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntelligentMatchVictoryView(
    moves: Int,
    seconds: Int,
    maxCombo: Int,
    mismatchedCards: List<CardEntity>,
    soundHelper: SoundHelper,
    onToggleStar: (CardEntity) -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        soundHelper.playVictoryCheer()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(EasyGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = EasyGreen
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ဂုဏ်ယူပါသည်! အောင်မြင်ပါပြီ 🎉",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "အတွဲအားလုံးကို အောင်မြင်စွာ တွဲစပ်ပြီးပါပြီ။",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(value = "${seconds}s", label = "ကြာချိန်", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                    MetricBox(value = "$moves", label = "ရွှေ့ချက်", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                    MetricBox(value = "x$maxCombo", label = "Max Combo", modifier = Modifier.weight(1f), color = HardOrange)
                }

                // Challenging/Mismatched Words Review
                if (mismatchedCards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ခက်ခဲသော ဝေါဟာရများ (${mismatchedCards.size} ခု မှားယွင်းခဲ့သည်):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            mismatchedCards.take(4).forEach { card ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${card.kanji} (${card.kana}) — ${card.burmeseMeaning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { onToggleStar(card) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (card.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Star",
                                            tint = if (card.isStarred) GoldStar else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestart,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ထပ်ကစားမည်")
                    }

                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ပြီးပါပြီ")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(value: String, label: String, modifier: Modifier, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun generateIntelligentTiles(
    cards: List<CardEntity>,
    pairCount: Int,
    targetType: MatchTargetType
): List<MatchTile> {
    val selectedCards = cards.shuffled().take(pairCount)
    val tileList = mutableListOf<MatchTile>()

    selectedCards.forEach { card ->
        // Japanese tile
        val japaneseText = if (card.kanji.isNotBlank()) card.kanji else card.kana
        val subReading = if (card.kanji.isNotBlank() && card.kanji != card.kana) card.kana else ""

        tileList.add(
            MatchTile(
                id = "jp_${card.id}",
                card = card,
                text = japaneseText,
                subText = subReading,
                type = TileType.JAPANESE
            )
        )

        // Matching tile (Burmese meaning OR Kana reading)
        val matchText = if (targetType == MatchTargetType.BURMESE) {
            card.burmeseMeaning
        } else {
            if (card.kana.isNotBlank()) card.kana else card.burmeseMeaning
        }

        tileList.add(
            MatchTile(
                id = "mean_${card.id}",
                card = card,
                text = matchText,
                type = TileType.MEANING
            )
        )
    }

    return tileList.shuffled()
}
