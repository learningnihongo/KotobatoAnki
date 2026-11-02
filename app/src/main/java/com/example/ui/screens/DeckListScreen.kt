package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import com.example.util.ShuffleHelper
import com.example.util.ShuffleType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.data.LocalCacheManager
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GoodBlue
import com.example.ui.theme.HardOrange

@Composable
fun DeckListScreen(
    decks: List<DeckEntity>,
    cardsByDeck: Map<Long, List<CardEntity>>,
    onSelectDeckForStudy: (DeckEntity) -> Unit,
    onSelectDeckForQuiz: (DeckEntity) -> Unit,
    onSelectDeckForMatch: (DeckEntity) -> Unit,
    onSelectDeckForList: (DeckEntity) -> Unit,
    onOpenAnkiSync: (DeckEntity, List<CardEntity>) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onDeleteDeck: (DeckEntity) -> Unit,
    onNavigateToDeckManager: () -> Unit,
    onOpenStarredReview: () -> Unit = {},
    onOpenMasteredReview: () -> Unit = {},
    onOpenAllCardsList: () -> Unit = {},
    onSelectDeckForShuffleStudy: (DeckEntity) -> Unit = {},
    onStartShuffledStudy: (DeckEntity, ShuffleType) -> Unit = { deck, _ -> onSelectDeckForShuffleStudy(deck) },
    allStarredCards: List<CardEntity> = emptyList(),
    allMasteredCards: List<CardEntity> = emptyList(),
    reminderConfig: LocalCacheManager.StudyReminderConfig? = null,
    onOpenStudyProgress: () -> Unit = {},
    onOpenReminderSettings: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {}
) {
    var showCreateDeckDialog by remember { mutableStateOf(false) }
    var showShuffleConfigDialog by remember { mutableStateOf(false) }

    val allCards = cardsByDeck.values.flatten()
    val totalCount = allCards.size
    val starredCount = allStarredCards.size
    val masteredCount = allMasteredCards.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("deck_list_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = if (decks.size <= 1) (decks.firstOrNull()?.name ?: "JLPT N3 Vocab") else "${decks.size} Decks · $totalCount လုံး",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Offline",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldStar.copy(alpha = 0.35f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Anki Ready",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Flashcard Game & Anki Input",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ဂျပန်စာ ဝေါဟာရ စုစုပေါင်း $totalCount လုံး Flashcards ဂိမ်းနှင့် Anki စနစ်",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Stats Row (Interactive 1-tap navigation)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPill(
                                count = "$totalCount",
                                label = "ဝေါဟာရအားလုံး",
                                emoji = "📚",
                                modifier = Modifier.weight(1f),
                                onClick = onOpenAllCardsList
                            )
                            StatPill(
                                count = "$masteredCount",
                                label = "ကျက်ပြီး (Mastered)",
                                emoji = "🏆",
                                modifier = Modifier.weight(1f),
                                onClick = onOpenMasteredReview
                            )
                            StatPill(
                                count = "$starredCount",
                                label = "★ မှတ်ထားသော",
                                emoji = "⭐",
                                modifier = Modifier.weight(1f),
                                onClick = onOpenStarredReview
                            )
                        }
                    }
                }
            }
        }

        // Study Progress & Streak Interactive Banner Card
        item {
            val masteryPercent = if (totalCount > 0) (masteredCount.toFloat() / totalCount.toFloat()) else 0f
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStudyProgress() }
                    .testTag("study_progress_dashboard_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = HardOrange.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = HardOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${reminderConfig?.currentStreakDays ?: 1} ရက် ဆက်တိုက်",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = HardOrange.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "🔥 Streak",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = HardOrange,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "ယနေ့ စာကျက်ပန်းတိုင်: ${reminderConfig?.todayStudiedCount ?: 0} / ${reminderConfig?.dailyGoalCards ?: 20} ကတ်",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Study Progress",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { masteryPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EasyGreen,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ဝေါဟာရ အလွတ်ရမှု (Mastery): ${(masteryPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = EasyGreen
                        )
                        Text(
                            text = "တိုးတက်မှု အသေးစိတ်ကြည့်မည် →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Quick Tools Row: Reminder Settings & Backup/Restore
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reminder Settings Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenReminderSettings() }
                        .testTag("dashboard_reminder_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "သတိပေးချက်",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${String.format("%02d:%02d", reminderConfig?.reminderHour ?: 20, reminderConfig?.reminderMinute ?: 0)} PM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Backup & Restore Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenBackupRestore() }
                        .testTag("dashboard_backup_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ဒေတာ Backup",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "အရန်သိမ်း/ပြန်ယူ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }



        // Section: Game Modes
        item {
            Text(
                text = "ဂိမ်းပုံစံများ ရွေးချယ်ပါ (Game Modes)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            val primaryDeck = decks.firstOrNull()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: SRS Flip & Shuffle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameModeCard(
                        title = "SRS Flashcard",
                        subTitle = "ကတ်လှန်လေ့ကျင့်ခန်း",
                        icon = Icons.Default.Style,
                        color = MaterialTheme.colorScheme.primary,
                        tag = "launch_study_flip_button",
                        modifier = Modifier.weight(1f),
                        onClick = { primaryDeck?.let { onSelectDeckForStudy(it) } }
                    )

                    GameModeCard(
                        title = "Shuffle Cards",
                        subTitle = "ကျပန်းမွှေလေ့ကျင့်",
                        icon = Icons.Default.Shuffle,
                        color = HardOrange,
                        tag = "launch_shuffle_study_button",
                        modifier = Modifier.weight(1f),
                        onClick = { showShuffleConfigDialog = true }
                    )
                }

                // Row 2: Speed Quiz & Match Pairs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameModeCard(
                        title = "Speed Quiz",
                        subTitle = "အမြန်ဖြေ ၄ ရွေးချယ်",
                        icon = Icons.Default.Quiz,
                        color = MaterialTheme.colorScheme.secondary,
                        tag = "launch_quiz_game_button",
                        modifier = Modifier.weight(1f),
                        onClick = { primaryDeck?.let { onSelectDeckForQuiz(it) } }
                    )

                    GameModeCard(
                        title = "Match Pairs",
                        subTitle = "အတွဲစပ်ဂိမ်း",
                        icon = Icons.Default.GridView,
                        color = EasyGreen,
                        tag = "launch_match_game_button",
                        modifier = Modifier.weight(1f),
                        onClick = { primaryDeck?.let { onSelectDeckForMatch(it) } }
                    )
                }
            }
        }

        // Section: Anki Quick Action Banner
        item {
            val firstDeck = decks.firstOrNull()
            val firstDeckCards = firstDeck?.let { cardsByDeck[it.id] } ?: emptyList()

            Surface(
                onClick = {
                    if (firstDeck != null) {
                        onOpenAnkiSync(firstDeck, firstDeckCards)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("anki_banner_button"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anki ထဲသို့ ထည့်သွင်း / ထုတ်ယူရန်",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Anki .apkg Format ဖြင့် Export/Share လုပ်ပြီး AnkiDroid ထဲသို့ တိုက်ရိုက် Input ပြုလုပ်ပါ",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section: Decks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "သင်၏ Flashcard Decks များ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.TextButton(
                        onClick = onNavigateToDeckManager,
                        modifier = Modifier.testTag("manage_all_decks_button")
                    ) {
                        Text(
                            text = "စီမံရန် (Manage)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    IconButton(
                        onClick = { showCreateDeckDialog = true },
                        modifier = Modifier.testTag("create_new_deck_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Deck",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Deck Items
        items(decks, key = { it.id }) { deck ->
            val cards = cardsByDeck[deck.id] ?: emptyList()
            DeckCardItem(
                deck = deck,
                cards = cards,
                onStudy = { onSelectDeckForStudy(deck) },
                onViewList = { onSelectDeckForList(deck) },
                onAnkiSync = { onOpenAnkiSync(deck, cards) },
                onDelete = { onDeleteDeck(deck) },
                canDelete = decks.size > 1
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showCreateDeckDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDeckDialog = false },
            onCreate = { name, desc ->
                onCreateDeck(name, desc)
                showCreateDeckDialog = false
            }
        )
    }

    if (showShuffleConfigDialog) {
        ShuffleStudyConfigDialog(
            decks = decks,
            cardsByDeck = cardsByDeck,
            allCardsCount = totalCount,
            starredCount = starredCount,
            onDismiss = { showShuffleConfigDialog = false },
            onConfirm = { targetDeck, shuffleType ->
                showShuffleConfigDialog = false
                onStartShuffledStudy(targetDeck, shuffleType)
            }
        )
    }
}

@Composable
private fun StatPill(
    count: String,
    label: String,
    modifier: Modifier = Modifier,
    emoji: String = "",
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (emoji.isNotBlank()) {
                    Text(text = emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "နှိပ်၍ကြည့်ရန် 👆",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subTitle: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DeckCardItem(
    deck: DeckEntity,
    cards: List<CardEntity>,
    onStudy: () -> Unit,
    onViewList: () -> Unit,
    onAnkiSync: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_item_${deck.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (deck.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deck.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${cards.size} Cards",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStudy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("deck_study_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("လေ့ကျင့်မည်")
                }

                OutlinedButton(
                    onClick = onViewList,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("deck_vocab_list_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("စာရင်း")
                }

                // Anki .apkg button
                IconButton(
                    onClick = onAnkiSync,
                    modifier = Modifier.testTag("deck_anki_button_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Anki Sync",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("deck_delete_button_${deck.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "ကတ်အုပ်စုအသစ် ဖန်တီးမည်",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Deck အမည် (Name)") },
                    placeholder = { Text("ဥပမာ: JLPT N3 Grammar / Vocab") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("ရှင်းလင်းချက် (Description)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("မလုပ်တော့ပါ")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, desc)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ဖန်တီးမည်")
                    }
                }
            }
        }
    }
}

@Composable
fun ShuffleStudyConfigDialog(
    decks: List<DeckEntity>,
    cardsByDeck: Map<Long, List<CardEntity>>,
    allCardsCount: Int,
    starredCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (DeckEntity, ShuffleType) -> Unit
) {
    var selectedTargetId by remember { mutableStateOf<Long>(-998L) } // default: All Cards
    var selectedShuffleType by remember { mutableStateOf(ShuffleType.TRUE_RANDOM) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HardOrange.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = HardOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Shuffle ကျပန်းမွှေ လေ့ကျင့်ခန်း",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "မွှေကျက်မည့် ကတ်များနှင့် စနစ်ရွေးချယ်ပါ",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // 1. Target Deck Selection
                Text(
                    text = "၁။ မွှေကျက်မည့် ကတ်များ ရွေးချယ်ပါ",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Option: All Cards
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTargetId == -998L) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        if (selectedTargetId == -998L) 2.dp else 1.dp,
                        if (selectedTargetId == -998L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTargetId = -998L }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📚", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ဝေါဟာရအားလုံး စုစုပေါင်း",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Deck အားလုံးပေါင်း ဝေါဟာရ $allCardsCount လုံးကို မွှေကျက်မည်",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$allCardsCount လုံး",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Option: Starred Cards
                if (starredCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTargetId == -999L) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            if (selectedTargetId == -999L) 2.dp else 1.dp,
                            if (selectedTargetId == -999L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTargetId = -999L }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ကြယ်ပွင့်မှတ်ထားသော ကတ်များ",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Starred ကတ် $starredCount ခုကိုသာ မွှေမည်",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = GoldStar.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$starredCount ခု",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldStar,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Individual Decks
                decks.forEach { deck ->
                    val deckCardsCount = cardsByDeck[deck.id]?.size ?: 0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTargetId == deck.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            if (selectedTargetId == deck.id) 2.dp else 1.dp,
                            if (selectedTargetId == deck.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTargetId = deck.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🗂️", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deck.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ဤ Deck ရှိ ကတ် $deckCardsCount ခုကိုသာ မွှေမည်",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$deckCardsCount ခု",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Shuffle Strategy Selection
                Text(
                    text = "၂။ Shuffle Logic မွှေနည်း ရွေးချယ်ပါ",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                listOf(
                    ShuffleType.TRUE_RANDOM,
                    ShuffleType.SMART_PRIORITY,
                    ShuffleType.UNMASTERED_ONLY
                ).forEach { type ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedShuffleType == type) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            if (selectedShuffleType == type) 2.dp else 1.dp,
                            if (selectedShuffleType == type) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedShuffleType = type }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = type.iconEmoji, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = type.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("မလုပ်တော့ပါ")
                    }

                    Button(
                        onClick = {
                            val chosenDeck = when (selectedTargetId) {
                                -998L -> DeckEntity(
                                    id = -998L,
                                    name = "🔀 ဝေါဟာရအားလုံး ($allCardsCount လုံး)",
                                    description = "စုစုပေါင်း ဝေါဟာရများ အားလုံးကျပန်းမွှေ"
                                )
                                -999L -> DeckEntity(
                                    id = -999L,
                                    name = "★ Starred Cards ($starredCount ခု)",
                                    description = "ခက်ခဲသော ဝေါဟာရများ မွှေကျက်ခြင်း"
                                )
                                else -> decks.find { it.id == selectedTargetId } ?: decks.firstOrNull() ?: DeckEntity(1L, "Deck")
                            }
                            onConfirm(chosenDeck, selectedShuffleType)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("စတင်မွှေကျက်မည်", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

