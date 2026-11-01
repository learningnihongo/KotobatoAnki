package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.ui.theme.AgainRed
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoldStar
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 3D Animated Flip Card with interactive Gestures:
 * 1. Tap or Vertical Swipe to Flip between Front & Back.
 * 2. Horizontal Swipe Right -> Good / Memorized (မှတ်မိသည်) with green indicator.
 * 3. Horizontal Swipe Left -> Again / Needs Review (မမှတ်မိပါ) with red indicator.
 * 4. Realistic 3D tilt physics and spring return.
 */
@Composable
fun FlipCard(
    card: CardEntity,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null
) {
    var showKanaByDefault by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val swipeThresholdPx = with(density) { 110.dp.toPx() }
    val verticalFlipThresholdPx = with(density) { 60.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    // Reset card drag offset when card changes
    LaunchedEffect(card.id) {
        offsetX.snapTo(0f)
    }

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "card_flip_rotation"
    )

    // Dynamic tilt angle as user drags horizontally
    val rotationZ = (offsetX.value / 600f) * 14f

    Box(
        modifier = modifier
            .testTag("flip_card_container_${card.id}")
            .pointerInput(card.id) {
                detectDragGestures(
                    onDragEnd = {
                        val currentX = offsetX.value
                        val currentY = totalDragY
                        totalDragY = 0f

                        when {
                            // Check vertical swipe for flip
                            abs(currentY) > verticalFlipThresholdPx && abs(currentX) < swipeThresholdPx -> {
                                scope.launch {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                }
                                onFlip()
                            }
                            // Swipe Right: Memorized / Good
                            currentX > swipeThresholdPx && onSwipeRight != null -> {
                                scope.launch {
                                    offsetX.animateTo(1000f, tween(200))
                                    onSwipeRight()
                                    offsetX.snapTo(0f)
                                }
                            }
                            // Swipe Left: Needs review / Again
                            currentX < -swipeThresholdPx && onSwipeLeft != null -> {
                                scope.launch {
                                    offsetX.animateTo(-1000f, tween(200))
                                    onSwipeLeft()
                                    offsetX.snapTo(0f)
                                }
                            }
                            // Spring back to center
                            else -> {
                                scope.launch {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium))
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        totalDragY = 0f
                        scope.launch {
                            offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    this.rotationY = rotationY
                    this.rotationZ = rotationZ
                    cameraDistance = 14f * density.density
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onFlip()
                }
                .testTag("flip_card_${card.id}"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (rotationY <= 90f) {
                // FRONT OF CARD (Question / Japanese)
                CardFrontContent(
                    card = card,
                    showKana = showKanaByDefault,
                    onToggleKana = { showKanaByDefault = !showKanaByDefault },
                    onSpeak = onSpeak,
                    onToggleStar = onToggleStar
                )
            } else {
                // BACK OF CARD (Hiragana reading + Burmese Meaning)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.rotationY = 180f }
                ) {
                    CardBackContent(
                        card = card,
                        onSpeak = onSpeak,
                        onToggleStar = onToggleStar
                    )
                }
            }
        }

        // Live Gesture Feedback Badges (Overlay on drag)
        if (offsetX.value > 30f) {
            val alpha = ((offsetX.value - 30f) / swipeThresholdPx).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .graphicsLayer { this.alpha = alpha },
                shape = RoundedCornerShape(16.dp),
                color = EasyGreen.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "မှတ်မိသည် (GOOD)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        } else if (offsetX.value < -30f) {
            val alpha = ((abs(offsetX.value) - 30f) / swipeThresholdPx).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .graphicsLayer { this.alpha = alpha },
                shape = RoundedCornerShape(16.dp),
                color = AgainRed.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "မမှတ်မိပါ (AGAIN)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
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
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                )
            )
            .padding(22.dp)
    ) {
        // Top Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kana Toggle Pill Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleKana() }
                    .testTag("toggle_furigana_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showKana) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showKana) "Kana ပိတ်မည်" else "Kana ပြမည်",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Right icons: TTS Audio & Star
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSpeak(if (card.kanji.isNotBlank()) card.kanji else card.kana) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak Japanese",
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

        // Center Content: Japanese Word / Kanji & Reading
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (card.kanji.isNotBlank()) {
                // Furigana / Kana above Kanji
                if (showKana) {
                    Text(
                        text = card.kana,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Main Kanji
                Text(
                    text = card.kanji,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else {
                // Kana only
                Text(
                    text = card.kana,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 40.sp,
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(22.dp)
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
                    text = "အဖြေ / နောက်ကျောပြင်",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSpeak(if (card.kana.isNotBlank()) card.kana else card.kanji) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak Japanese",
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

        // Center Content: Prominent Hiragana (Reading) + Myanmar Meaning
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Prominent Hiragana Reading Section
            if (card.kana.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "အသံထွက် (HIRAGANA)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.kana,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Prominent Myanmar Meaning Section
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "မြန်မာအဓိပ္ပာယ် (MYANMAR MEANING)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.burmeseMeaning,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Optional: Kanji & Notes box
            if (card.kanji.isNotBlank() && card.kanji != card.kana) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Kanji: ${card.kanji}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (card.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = card.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
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
