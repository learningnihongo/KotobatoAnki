package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
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
import com.example.ui.SoundHelper
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import kotlinx.coroutines.delay

data class QuizQuestion(
    val card: CardEntity,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun QuizGameScreen(
    cards: List<CardEntity>,
    soundHelper: SoundHelper,
    onBack: () -> Unit
) {
    if (cards.size < 4) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Quiz ကစားရန် အနည်းဆုံး ကတ် ၄ ခု လိုအပ်ပါသည်",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    var questions by remember(cards) {
        mutableStateOf(generateQuizQuestions(cards, count = 10.coerceAtMost(cards.size)))
    }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var isAnswerLocked by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var isGameOver by remember { mutableStateOf(false) }

    val currentQ = questions.getOrNull(currentQIndex)

    // Timer countdown
    LaunchedEffect(currentQIndex, isAnswerLocked, isGameOver) {
        if (!isAnswerLocked && !isGameOver) {
            timeLeft = 15
            while (timeLeft > 0 && !isAnswerLocked) {
                delay(1000L)
                timeLeft--
            }
            if (timeLeft == 0 && !isAnswerLocked) {
                // Time up! Count as wrong
                isAnswerLocked = true
                soundHelper.playWrongSound()
                streak = 0
                delay(1400L)
                if (currentQIndex + 1 < questions.size) {
                    currentQIndex++
                    selectedOptionIndex = null
                    isAnswerLocked = false
                } else {
                    isGameOver = true
                }
            }
        }
    }

    if (isGameOver || currentQ == null) {
        QuizResultView(
            score = score,
            total = questions.size,
            maxStreak = maxStreak,
            onRestart = {
                questions = generateQuizQuestions(cards, count = 10.coerceAtMost(cards.size))
                currentQIndex = 0
                selectedOptionIndex = null
                score = 0
                streak = 0
                maxStreak = 0
                isAnswerLocked = false
                isGameOver = false
            },
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("quiz_game_screen")
    ) {
        // Header: Back, Question count, Streak & Score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("quiz_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Q ${currentQIndex + 1} / ${questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Streak Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (streak >= 3) Color(0xFFF97316).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = if (streak >= 3) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (streak >= 3) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timer Bar
        val timerFraction = timeLeft / 15f
        LinearProgressIndicator(
            progress = { timerFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (timeLeft <= 4) AgainRed else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Audio button in top-right
                IconButton(
                    onClick = {
                        soundHelper.speakJapanese(
                            if (currentQ.card.kanji.isNotBlank()) currentQ.card.kanji else currentQ.card.kana
                        )
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Prompt
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentQ.card.kanji.isNotBlank() && currentQ.card.kanji != currentQ.card.kana) {
                        Text(
                            text = currentQ.card.kana,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQ.card.kanji,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = currentQ.card.kana,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "မှန်ကန်သော မြန်မာအဓိပ္ပာယ်ကို ရွေးပါ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time countdown in bottom-center
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (timeLeft <= 4) AgainRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timeLeft}s",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (timeLeft <= 4) AgainRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Multiple Choice Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            currentQ.options.forEachIndexed { index, optionText ->
                val isCorrect = index == currentQ.correctIndex
                val isSelected = index == selectedOptionIndex

                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        isAnswerLocked && isCorrect -> EasyGreen.copy(alpha = 0.2f)
                        isAnswerLocked && isSelected && !isCorrect -> AgainRed.copy(alpha = 0.2f)
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    label = "option_bg"
                )

                val borderColor by animateColorAsState(
                    targetValue = when {
                        isAnswerLocked && isCorrect -> EasyGreen
                        isAnswerLocked && isSelected && !isCorrect -> AgainRed
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    },
                    label = "option_border"
                )

                Surface(
                    onClick = {
                        if (!isAnswerLocked) {
                            selectedOptionIndex = index
                            isAnswerLocked = true
                            if (isCorrect) {
                                soundHelper.playCorrectSound()
                                streak++
                                if (streak > maxStreak) maxStreak = streak
                                val bonus = if (streak >= 3) 15 else 10
                                score += bonus
                            } else {
                                soundHelper.playWrongSound()
                                streak = 0
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("quiz_option_$index"),
                    shape = RoundedCornerShape(16.dp),
                    color = backgroundColor,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isAnswerLocked && isCorrect -> EasyGreen
                                isAnswerLocked && isSelected && !isCorrect -> AgainRed
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isAnswerLocked && isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (isAnswerLocked && isSelected && !isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${'A' + index}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Next button appears after answering
        if (isAnswerLocked) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (currentQIndex + 1 < questions.size) {
                        currentQIndex++
                        selectedOptionIndex = null
                        isAnswerLocked = false
                    } else {
                        isGameOver = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("quiz_next_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentQIndex + 1 < questions.size) "နောက်မေးခွန်းသို့ (Next)" else "ရလဒ်ကြည့်မည် (See Results)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private fun generateQuizQuestions(allCards: List<CardEntity>, count: Int): List<QuizQuestion> {
    val shuffled = allCards.shuffled().take(count)
    return shuffled.map { card ->
        val otherMeanings = allCards
            .filter { it.id != card.id && it.burmeseMeaning != card.burmeseMeaning }
            .map { it.burmeseMeaning }
            .distinct()
            .shuffled()
            .take(3)

        val options = (otherMeanings + card.burmeseMeaning).shuffled()
        val correctIndex = options.indexOf(card.burmeseMeaning)
        QuizQuestion(
            card = card,
            options = options,
            correctIndex = correctIndex
        )
    }
}

@Composable
private fun QuizResultView(
    score: Int,
    total: Int,
    maxStreak: Int,
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
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GoldStar.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = GoldStar
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Quiz ရလဒ်များ",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ရမှတ် (Score)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$maxStreak",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "အများဆုံး Streak",
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
                        .testTag("quiz_restart_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ထပ်မံကစားမည် (Play Again)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_finish_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ပင်မစာမျက်နှာသို့")
                }
            }
        }
    }
}
