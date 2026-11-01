package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.data.DeckEntity
import com.example.ui.components.FlipCard
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GoodBlue
import com.example.ui.theme.HardOrange

@Composable
fun StudyFlipScreen(
    deck: DeckEntity,
    cards: List<CardEntity>,
    onSpeak: (String) -> Unit,
    onRateCard: (CardEntity, Int) -> Unit,
    onToggleStar: (CardEntity) -> Unit,
    onBack: () -> Unit
) {
    if (cards.isEmpty()) {
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
                    text = "ဤ Deck (${deck.name}) တွင် Flashcard များ မရှိသေးပါ။",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ကတ်များထည့်သွင်းရန် ပြန်သွားမည်")
                }
            }
        }
        return
    }

    var filterStarredOnly by remember { mutableStateOf(false) }

    val activeList = remember(cards, filterStarredOnly) {
        if (filterStarredOnly) cards.filter { it.isStarred } else cards
    }

    var studyList by remember(activeList) { mutableStateOf(activeList) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var studiedCount by remember { mutableIntStateOf(0) }
    var isAutoAudioEnabled by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }
    var showShuffleToast by remember { mutableStateOf(false) }
    var shuffleRotationAngle by remember { mutableFloatStateOf(0f) }

    val animatedShuffleAngle by animateFloatAsState(
        targetValue = shuffleRotationAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shuffle_angle"
    )

    val doShuffle = {
        studyList = studyList.shuffled()
        currentIndex = 0
        isFlipped = false
        isShuffled = true
        shuffleRotationAngle += 360f
        showShuffleToast = true
    }

    LaunchedEffect(showShuffleToast) {
        if (showShuffleToast) {
            kotlinx.coroutines.delay(2200)
            showShuffleToast = false
        }
    }

    if (studyList.isEmpty()) {
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
                Surface(
                    shape = CircleShape,
                    color = GoldStar.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Text(
                    text = "ဤ Deck တွင် ကြယ်ပွင့် (★) မှတ်ထားသော ကတ်မရှိသေးပါ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ခက်ခဲသော ဝေါဟာရများကို ကတ်ပေါ်ရှိ ကြယ်ပွင့် (★) နှိပ်၍ သီးသန့် လေ့ကျင့်နိုင်ပါသည်",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { filterStarredOnly = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ကတ်များအားလုံး ပြန်လည်လေ့ကျင့်မည်")
                }
            }
        }
        return
    }

    val currentCard = studyList.getOrNull(currentIndex)

    // Automatically speak Japanese term when card loads if Auto-Audio is enabled
    LaunchedEffect(currentCard?.id, isAutoAudioEnabled) {
        if (isAutoAudioEnabled && !isFlipped && currentCard != null) {
            val term = if (currentCard.kana.isNotBlank()) currentCard.kana else currentCard.kanji
            onSpeak(term)
        }
    }

    if (isCompleted || currentCard == null) {
        // Study Completed Summary View
        SessionCompleteView(
            totalStudied = studiedCount,
            deckName = if (filterStarredOnly) "★ ${deck.name} (Starred)" else deck.name,
            onRestart = {
                currentIndex = 0
                isFlipped = false
                isCompleted = false
                studiedCount = 0
            },
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("study_flip_screen")
    ) {
        // Top Bar: Navigation + Progress + Audio Toggle + Shuffle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("study_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${currentIndex + 1} / ${studyList.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isShuffled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EasyGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🔀 Shuffled",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EasyGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Starred / Challenging Only Filter Button
                val starredCount = cards.count { it.isStarred }
                IconButton(
                    onClick = {
                        filterStarredOnly = !filterStarredOnly
                        currentIndex = 0
                        isFlipped = false
                    },
                    modifier = Modifier.testTag("study_filter_starred_toggle")
                ) {
                    Icon(
                        imageVector = if (filterStarredOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Filter Starred Only ($starredCount)",
                        tint = if (filterStarredOnly) GoldStar else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // Auto Audio Toggle Button
                IconButton(
                    onClick = { isAutoAudioEnabled = !isAutoAudioEnabled },
                    modifier = Modifier.testTag("study_auto_audio_toggle")
                ) {
                    Icon(
                        imageVector = if (isAutoAudioEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Auto audio toggle",
                        tint = if (isAutoAudioEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = doShuffle,
                    modifier = Modifier.testTag("study_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle cards",
                        tint = if (isShuffled) EasyGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer { rotationZ = animatedShuffleAngle }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        val progress = (currentIndex + 1).toFloat() / studyList.size.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Gesture Guide Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👈 ဘယ်သို့ဆွဲ (Again)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = AgainRed,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "👆 နှိပ်/ပွတ် (လှန်မည်)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "ညာသို့ဆွဲ (Good) 👉",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = EasyGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Shuffle Notification Banner
        AnimatedVisibility(
            visible = showShuffleToast,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("shuffle_notification_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ကတ် ${studyList.size} ခုကို ကျပန်းမွှေပြီးပါပြီ 🔀",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main 3D Flip Card Container with Swipe Gestures
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            FlipCard(
                card = currentCard,
                isFlipped = isFlipped,
                onFlip = { isFlipped = !isFlipped },
                onSpeak = onSpeak,
                onToggleStar = { onToggleStar(currentCard) },
                onSwipeRight = {
                    onRateCard(currentCard, 3)
                    studiedCount++
                    goToNextCard(studyList.size, currentIndex) { next ->
                        currentIndex = next
                        isFlipped = false
                    }
                },
                onSwipeLeft = {
                    onRateCard(currentCard, 1)
                    studiedCount++
                    goToNextCard(studyList.size, currentIndex) { next ->
                        currentIndex = next
                        isFlipped = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls: When Not Flipped vs When Flipped
        if (!isFlipped) {
            // Show Answer Button
            Button(
                onClick = { isFlipped = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("show_answer_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "အဖြေကြည့်မည် (Show Answer)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            // SRS Rating Buttons
            AnimatedVisibility(
                visible = isFlipped,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "မှတ်မိနိုင်စွမ်းကို ရွေးပါ (Rate recall):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Again (ခက်ခဲ / မမှတ်မိ)
                        SrsRateButton(
                            label = "Again",
                            subLabel = "နောက်တစ်ခါ",
                            color = AgainRed,
                            modifier = Modifier.weight(1f),
                            tag = "rate_again_button"
                        ) {
                            onRateCard(currentCard, 1)
                            studiedCount++
                            goToNextCard(studyList.size, currentIndex) { next ->
                                currentIndex = next
                                isFlipped = false
                            }
                        }

                        // Hard (ခက်တယ်)
                        SrsRateButton(
                            label = "Hard",
                            subLabel = "ခက်တယ်",
                            color = HardOrange,
                            modifier = Modifier.weight(1f),
                            tag = "rate_hard_button"
                        ) {
                            onRateCard(currentCard, 2)
                            studiedCount++
                            goToNextCard(studyList.size, currentIndex) { next ->
                                currentIndex = next
                                isFlipped = false
                            }
                        }

                        // Good (ကောင်းတယ်)
                        SrsRateButton(
                            label = "Good",
                            subLabel = "ကောင်းတယ်",
                            color = GoodBlue,
                            modifier = Modifier.weight(1f),
                            tag = "rate_good_button"
                        ) {
                            onRateCard(currentCard, 3)
                            studiedCount++
                            goToNextCard(studyList.size, currentIndex) { next ->
                                currentIndex = next
                                isFlipped = false
                            }
                        }

                        // Easy (လွယ်တယ်)
                        SrsRateButton(
                            label = "Easy",
                            subLabel = "လွယ်တယ်",
                            color = EasyGreen,
                            modifier = Modifier.weight(1f),
                            tag = "rate_easy_button"
                        ) {
                            onRateCard(currentCard, 4)
                            studiedCount++
                            goToNextCard(studyList.size, currentIndex) { next ->
                                currentIndex = next
                                isFlipped = false
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prev / Next quick navigation + Shuffle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFlipped = false
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier.testTag("nav_prev_card")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }

            // Dedicated Shuffle Action Button
            OutlinedButton(
                onClick = doShuffle,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("shuffle_cards_bottom_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = animatedShuffleAngle },
                    tint = if (isShuffled) EasyGreen else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isShuffled) "ပြန်မွှေမည် (Reshuffle)" else "ကျပန်းမွှေမည် (Shuffle)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            IconButton(
                onClick = {
                    if (currentIndex < studyList.size - 1) {
                        currentIndex++
                        isFlipped = false
                    } else {
                        isCompleted = true
                    }
                },
                modifier = Modifier.testTag("nav_next_card")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

private fun goToNextCard(total: Int, current: Int, onNext: (Int) -> Unit) {
    if (current + 1 < total) {
        onNext(current + 1)
    } else {
        // Complete
        onNext(total)
    }
}

@Composable
private fun SrsRateButton(
    label: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionCompleteView(
    totalStudied: Int,
    deckName: String,
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
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "လေ့ကျင့်မှု ပြီးဆုံးပါပြီ!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$deckName မှ ကတ် ${totalStudied} ခုကို အောင်မြင်စွာ လေ့ကျင့်ပြီးပါပြီ။",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("restart_session_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ပြန်လည်လေ့ကျင့်မည် (Restart)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("return_home_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ပင်မစာမျက်နှာသို့ ပြန်သွားမည်")
                }
            }
        }
    }
}
