package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.DeckEntity
import com.example.ui.components.FlipCard
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
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

    var studyList by remember(cards) { mutableStateOf(cards) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var studiedCount by remember { mutableIntStateOf(0) }

    val currentCard = studyList.getOrNull(currentIndex)

    if (isCompleted || currentCard == null) {
        // Study Completed Summary View
        SessionCompleteView(
            totalStudied = studiedCount,
            deckName = deck.name,
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
        // Top Bar: Navigation + Progress
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
                Text(
                    text = "${currentIndex + 1} / ${studyList.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    studyList = studyList.shuffled()
                    currentIndex = 0
                    isFlipped = false
                },
                modifier = Modifier.testTag("study_shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle cards",
                    tint = MaterialTheme.colorScheme.primary
                )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Main 3D Flip Card Container
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
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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

        // Prev / Next quick navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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
