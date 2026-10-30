package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.ui.theme.GoldStar

@Composable
fun FlipCard(
    card: CardEntity,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showKanaByDefault by remember { mutableStateOf(true) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip_rotation"
    )

    Card(
        modifier = modifier
            .testTag("flip_card_${card.id}")
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onFlip()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        if (rotation <= 90f) {
            // FRONT OF CARD (Question / Japanese)
            CardFrontContent(
                card = card,
                showKana = showKanaByDefault,
                onToggleKana = { showKanaByDefault = !showKanaByDefault },
                onSpeak = onSpeak,
                onToggleStar = onToggleStar
            )
        } else {
            // BACK OF CARD (Answer / Burmese Meaning)
            // Mirror back to read normally
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBackContent(
                    card = card,
                    onSpeak = onSpeak,
                    onToggleStar = onToggleStar
                )
            }
        }
    }
}

@Composable
private fun CardFrontContent(
    card: CardEntity,
    showKana: Boolean,
    onToggleKana: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleStar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        // Top Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "No. ${card.orderIndex.takeIf { it > 0 } ?: card.id}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Toggle Kana Hint button
                IconButton(
                    onClick = onToggleKana,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (showKana) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Kana reading",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Audio speech
                IconButton(
                    onClick = { onSpeak(if (card.kanji.isNotBlank()) card.kanji else card.kana) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Speak Japanese",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Star bookmark
                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (card.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star card",
                        tint = if (card.isStarred) GoldStar else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Center Content: Kanji and Kana
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val hasDistinctKanji = card.kanji.isNotBlank() && card.kanji != card.kana

            if (hasDistinctKanji) {
                // Furigana / Kana above Kanji (or toggleable)
                if (showKana) {
                    Text(
                        text = card.kana,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = card.kanji,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = card.kana,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Tap Hint
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "နှိပ်ပြီး အဓိပ္ပာယ်ကြည့်ပါ (Tap to flip)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardBackContent(
    card: CardEntity,
    onSpeak: (String) -> Unit,
    onToggleStar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(20.dp)
    ) {
        // Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "အဓိပ္ပာယ် (Meaning)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSpeak(if (card.kanji.isNotBlank()) card.kanji else card.kana) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (card.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star card",
                        tint = if (card.isStarred) GoldStar else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Center Content: Burmese Meaning and Word recap
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Burmese Meaning
            Text(
                text = card.burmeseMeaning,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Japanese Word Reference Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (card.kanji.isNotBlank() && card.kanji != card.kana) {
                            "${card.kanji} (${card.kana})"
                        } else {
                            card.kana
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Bottom Tap Hint
        Text(
            text = "ရှေ့မျက်နှာပြင်သို့ ပြန်လှန်ရန် နှိပ်ပါ (Tap to flip back)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
