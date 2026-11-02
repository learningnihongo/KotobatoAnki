package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.data.LocalCacheManager
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GoodBlue
import com.example.ui.theme.HardOrange

@Composable
fun StudyProgressScreen(
    decks: List<DeckEntity>,
    cardsByDeck: Map<Long, List<CardEntity>>,
    reminderConfig: LocalCacheManager.StudyReminderConfig,
    onOpenReminderSettings: () -> Unit,
    onBack: () -> Unit
) {
    val allCards = cardsByDeck.values.flatten()
    val totalCount = allCards.size
    val masteredCount = allCards.count { it.srsLevel == 3 }
    val reviewingCount = allCards.count { it.srsLevel == 2 }
    val learningCount = allCards.count { it.srsLevel == 1 }
    val newCount = allCards.count { it.srsLevel == 0 }
    val starredCount = allCards.count { it.isStarred }

    val masteryPercent = if (totalCount > 0) (masteredCount.toFloat() / totalCount.toFloat()) else 0f
    val animatedMastery by animateFloatAsState(
        targetValue = masteryPercent,
        animationSpec = tween(durationMillis = 800),
        label = "mastery_progress"
    )

    val goalProgress = if (reminderConfig.dailyGoalCards > 0) {
        (reminderConfig.todayStudiedCount.toFloat() / reminderConfig.dailyGoalCards.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedGoal by animateFloatAsState(
        targetValue = goalProgress,
        animationSpec = tween(durationMillis = 800),
        label = "goal_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("study_progress_screen")
    ) {
        // Top Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("progress_back_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "စာကျက်မှု တိုးတက်မှု (Study Progress)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SRS စနစ်နှင့် နေ့စဉ်စာကျက် မှတ်တမ်းအသေးစိတ်",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onOpenReminderSettings,
                    modifier = Modifier.testTag("open_reminder_from_progress")
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Reminder Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Daily Streak & Today's Goal Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_streak_goal_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Goal Ring
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedGoal },
                                modifier = Modifier.size(76.dp),
                                color = HardOrange,
                                strokeWidth = 7.dp,
                                trackColor = HardOrange.copy(alpha = 0.2f)
                            )
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = HardOrange,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${reminderConfig.currentStreakDays} ရက်ဆက်တိုက်",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = HardOrange.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🔥 Streak",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = HardOrange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "ယနေ့ ကျက်ပြီး: ${reminderConfig.todayStudiedCount} / ${reminderConfig.dailyGoalCards} ကတ်",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (reminderConfig.todayStudiedCount >= reminderConfig.dailyGoalCards)
                                    "🎉 ယနေ့ ပန်းတိုင်ပြည့်မီပြီးပါပြီ!"
                                else
                                    "ပန်းတိုင်ပြည့်ရန် ${reminderConfig.dailyGoalCards - reminderConfig.todayStudiedCount} ကတ် လိုအပ်ပါသေးသည်",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reminderConfig.todayStudiedCount >= reminderConfig.dailyGoalCards) EasyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Total Mastery Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mastery_overview_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ဝေါဟာရ အလွတ်ရမှု (Mastery)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${(masteryPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { animatedMastery },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ဝေါဟာရ စုစုပေါင်း $totalCount လုံးအနက် $masteredCount လုံးကို စိတ်ချစွာ အလွတ်ရပြီးဖြစ်ပါသည်",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. 4-Stage SRS Level Breakdown Grid
            item {
                Text(
                    text = "SRS စနစ် အဆင့်အလိုက် အခြေအနေ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SrsStatBox(
                        title = "ကျက်ပြီး (Mastered)",
                        count = masteredCount,
                        sub = "SRS Level 3",
                        icon = Icons.Default.CheckCircle,
                        color = EasyGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SrsStatBox(
                        title = "ပြန်လေ့ကျင့် (Review)",
                        count = reviewingCount,
                        sub = "SRS Level 2",
                        icon = Icons.Default.TrendingUp,
                        color = GoodBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SrsStatBox(
                        title = "ကျက်မှတ်ဆဲ (Learning)",
                        count = learningCount,
                        sub = "SRS Level 1",
                        icon = Icons.Default.School,
                        color = HardOrange,
                        modifier = Modifier.weight(1f)
                    )
                    SrsStatBox(
                        title = "ခက်ခဲသော (Starred)",
                        count = starredCount,
                        sub = "★ မှတ်ထားသော",
                        icon = Icons.Default.Star,
                        color = GoldStar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Deck-by-Deck Breakdown (including Imported Decks!)
            item {
                Text(
                    text = "Deck အလိုက် ကျက်ပြီးစီးမှု (Decks Breakdown)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(decks.size) { idx ->
                val deck = decks[idx]
                val deckCards = cardsByDeck[deck.id] ?: emptyList()
                val dTotal = deckCards.size
                val dMastered = deckCards.count { it.srsLevel == 3 }
                val dStarred = deckCards.count { it.isStarred }
                val dPercent = if (dTotal > 0) (dMastered.toFloat() / dTotal.toFloat()) else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("progress_deck_${deck.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = deck.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$dMastered / $dTotal (${(dPercent * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { dPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EasyGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "စုစုပေါင်း $dTotal ကတ်",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (dStarred > 0) {
                                Text(
                                    text = "★ $dStarred ကတ် မှတ်ထားသည်",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldStar
                                )
                            }
                        }
                    }
                }
            }

            // Quick reminder setup banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "နေ့စဉ် စာကျက်သတိပေးချက် (Reminder)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ည ${String.format("%02d:%02d", reminderConfig.reminderHour, reminderConfig.reminderMinute)} တွင် သတိပေးရန် သတ်မှတ်ထားပါသည်",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenReminderSettings,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ပြင်ဆင်မည်")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SrsStatBox(
    title: String,
    count: Int,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.25f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
