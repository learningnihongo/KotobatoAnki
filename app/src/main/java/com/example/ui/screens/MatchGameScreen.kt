package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.ui.SoundHelper
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import kotlinx.coroutines.delay

enum class TileType { JAPANESE, BURMESE }

data class MatchTile(
    val id: String,
    val cardId: Long,
    val text: String,
    val subText: String = "",
    val type: TileType,
    var isMatched: Boolean = false
)

@Composable
fun MatchGameScreen(
    cards: List<CardEntity>,
    soundHelper: SoundHelper,
    onBack: () -> Unit
) {
    if (cards.size < 6) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "ကစားရန် အနည်းဆုံး ကတ် ၆ ခု လိုအပ်ပါသည်")
        }
        return
    }

    val pairCount = 6
    var tiles by remember(cards) { mutableStateOf(generateTiles(cards, pairCount)) }
    var selectedTile1 by remember { mutableStateOf<MatchTile?>(null) }
    var selectedTile2 by remember { mutableStateOf<MatchTile?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    var isGameWon by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(isGameWon) {
        while (!isGameWon) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    if (isGameWon) {
        MatchVictoryView(
            moves = moves,
            seconds = elapsedSeconds,
            onRestart = {
                tiles = generateTiles(cards, pairCount)
                selectedTile1 = null
                selectedTile2 = null
                moves = 0
                matchedPairs = 0
                elapsedSeconds = 0
                isChecking = false
                isGameWon = false
            },
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("match_game_screen")
    ) {
        // Top Header
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${elapsedSeconds}s",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "ရွှေ့ချက်: $moves",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ဂျပန်စာနှင့် ကိုက်ညီသော မြန်မာအဓိပ္ပာယ်ကို တွဲစပ်ပါ ($matchedPairs / $pairCount တွဲပြီး)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of 12 Tiles
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
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
                        isMatched -> EasyGreen.copy(alpha = 0.2f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        tile.type == TileType.JAPANESE -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "tile_bg"
                )

                val tileBorder by animateColorAsState(
                    targetValue = when {
                        isMatched -> EasyGreen
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
                                    soundHelper.speakJapanese(tile.text)
                                }
                            } else if (selectedTile2 == null && tile.id != selectedTile1?.id) {
                                selectedTile2 = tile
                                moves++
                                isChecking = true

                                // Check match
                                val t1 = selectedTile1!!
                                val t2 = tile

                                if (t1.cardId == t2.cardId && t1.type != t2.type) {
                                    // Match!
                                    soundHelper.playCorrectSound()
                                    tiles = tiles.map {
                                        if (it.cardId == t1.cardId) it.copy(isMatched = true) else it
                                    }
                                    matchedPairs++
                                    selectedTile1 = null
                                    selectedTile2 = null
                                    isChecking = false
                                    if (matchedPairs == pairCount) {
                                        isGameWon = true
                                    }
                                } else {
                                    // Mismatch
                                    soundHelper.playWrongSound()
                                    // Let user see mismatch for 800ms
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        selectedTile1 = null
                                        selectedTile2 = null
                                        isChecking = false
                                    }, 800)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(105.dp)
                        .testTag("match_tile_${tile.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = tileBg,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, tileBorder),
                    shadowElevation = if (isMatched) 0.dp else 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
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
                                        fontSize = if (tile.type == TileType.JAPANESE) 17.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (tile.subText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tile.subText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
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

private fun generateTiles(cards: List<CardEntity>, count: Int): List<MatchTile> {
    val selected = cards.shuffled().take(count)
    val list = mutableListOf<MatchTile>()
    selected.forEach { card ->
        val jpText = if (card.kanji.isNotBlank()) card.kanji else card.kana
        val jpSub = if (card.kanji.isNotBlank() && card.kanji != card.kana) card.kana else ""

        list.add(
            MatchTile(
                id = "jp_${card.id}",
                cardId = card.id,
                text = jpText,
                subText = jpSub,
                type = TileType.JAPANESE
            )
        )
        list.add(
            MatchTile(
                id = "my_${card.id}",
                cardId = card.id,
                text = card.burmeseMeaning,
                type = TileType.BURMESE
            )
        )
    }
    return list.shuffled()
}

@Composable
private fun MatchVictoryView(
    moves: Int,
    seconds: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(EasyGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = EasyGreen
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "ဂုဏ်ယူပါသည်! အောင်မြင်ပါပြီ",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "အတွဲအားလုံးကို အောင်မြင်စွာ တွဲစပ်ပြီးပါပြီ။",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${seconds}s",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ကြာချိန်",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$moves",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "ရွှေ့ချက်",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("match_restart_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ထပ်မံကစားမည် (Play Again)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("match_exit_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ပင်မစာမျက်နှာသို့")
                }
            }
        }
    }
}
