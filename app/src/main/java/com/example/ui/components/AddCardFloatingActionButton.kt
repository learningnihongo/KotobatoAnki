package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Material 3 Speed Dial Floating Action Button for "ကတ်အသစ်ထည့်မည် (Add New Card)".
 * Supports:
 * 1. Quick Add Dialog popup (အမြန်ထည့်မည်)
 * 2. Full Flashcard Form screen (ကတ်အသေးစိတ်ထည့်မည်)
 * 3. New Deck Creation (Deck အသစ်လုပ်မည်)
 */
@Composable
fun AddCardSpeedDialFab(
    onNavigateToAddCard: () -> Unit,
    onQuickAddCard: () -> Unit,
    onCreateNewDeck: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isExpandedDefault: Boolean = false
) {
    var expanded by remember { mutableStateOf(isExpandedDefault) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "fab_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Semi-transparent backdrop when speed dial is open
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = false
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Speed Dial Item 1: Create New Deck (Optional)
            if (onCreateNewDeck != null) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 } + scaleIn(tween(180)),
                    exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 } + scaleOut(tween(150))
                ) {
                    SpeedDialOption(
                        label = "Deck အသစ်ဖန်တီးမည် (New Deck)",
                        sublabel = "ဝေါဟာရ အုပ်စုအသစ် စတင်ရန်",
                        icon = Icons.Default.CreateNewFolder,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        testTag = "speed_dial_new_deck",
                        onClick = {
                            expanded = false
                            onCreateNewDeck()
                        }
                    )
                }
            }

            // Speed Dial Item 2: Quick Add Card Dialog
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 } + scaleIn(tween(220)),
                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 } + scaleOut(tween(150))
            ) {
                SpeedDialOption(
                    label = "ကတ်အမြန်ထည့်မည် (Quick Add)",
                    sublabel = "Popup Dialog ဖြင့် ချက်ချင်းကတ်သွင်းရန်",
                    icon = Icons.Default.FlashOn,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    testTag = "speed_dial_quick_add",
                    onClick = {
                        expanded = false
                        onQuickAddCard()
                    }
                )
            }

            // Speed Dial Item 3: Full Add Card Screen
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 2 } + scaleIn(tween(260)),
                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 } + scaleOut(tween(150))
            ) {
                SpeedDialOption(
                    label = "ကတ်အသေးစိတ် ထည့်မည် (Full Form)",
                    sublabel = "ရှေ့/နောက်/မှတ်စု အစုံအလင်ဖြည့်သွင်းရန်",
                    icon = Icons.Default.NoteAdd,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = "speed_dial_full_form",
                    onClick = {
                        expanded = false
                        onNavigateToAddCard()
                    }
                )
            }

            // Main Trigger Floating Action Button
            FloatingActionButton(
                onClick = {
                    expanded = !expanded
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                ),
                modifier = Modifier
                    .testTag("fab_add_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ကတ်အသစ်ထည့်မည် (Add Card)",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (expanded) "ပိတ်မည်" else "ကတ်အသစ်ထည့်မည်",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialOption(
    label: String,
    sublabel: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 3.dp,
        modifier = Modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
