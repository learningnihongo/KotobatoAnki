package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Material 3 Extended Floating Action Button for adding cards.
 * Elegant, sleek pill design with Burmese label and high visibility.
 */
@Composable
fun AddCardModernFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Card",
                modifier = Modifier.size(24.dp)
            )
        },
        text = {
            Text(
                text = "ကတ်အသစ်ထည့်မည်",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        modifier = modifier.testTag("fab_add_card")
    )
}

/**
 * Modal Bottom Sheet providing crystal-clear creation and import options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardOptionsBottomSheet(
    onDismiss: () -> Unit,
    onQuickAddCard: () -> Unit,
    onNavigateToAddCard: () -> Unit,
    onImportFile: () -> Unit,
    onShowFormatGuide: () -> Unit,
    onCreateNewDeck: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("add_card_bottom_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ကတ်အသစ် ထည့်သွင်းခြင်း",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "မိမိနှစ်သက်ရာ နည်းလမ်းဖြင့် ထည့်သွင်းပါ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: Quick Add Dialog
            AddCardSheetOptionItem(
                title = "ကတ်အမြန်ထည့်မည် (Quick Add)",
                subtitle = "Popup dialog ဖြင့် ချက်ချင်းဖြည့်ပြီး သိမ်းမည်",
                icon = Icons.Default.FlashOn,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tag = "sheet_option_quick_add",
                onClick = {
                    onDismiss()
                    onQuickAddCard()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Full Detailed Form
            AddCardSheetOptionItem(
                title = "ကတ်အသေးစိတ်ထည့်မည် (Full Form)",
                subtitle = "Kanji, အသံထွက်, မြန်မာအဓိပ္ပာယ်, မှတ်စု အပြည့်အစုံထည့်ရန်",
                icon = Icons.AutoMirrored.Filled.NoteAdd,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                tag = "sheet_option_full_form",
                onClick = {
                    onDismiss()
                    onNavigateToAddCard()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Import from File (TXT, CSV, TSV, APKG)
            AddCardSheetOptionItem(
                title = "TXT / CSV ဖိုင်ဖြင့် သွင်းမည် (Import File)",
                subtitle = "ဖုန်းထဲရှိ Text (.txt) သို့မဟုတ် CSV (.csv) ဖိုင်မှ ကတ်များ တစ်ပြိုင်နက်သွင်းရန်",
                icon = Icons.Default.FileUpload,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                tag = "sheet_option_import_file",
                onClick = {
                    onDismiss()
                    onImportFile()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 4: Format Guide
            AddCardSheetOptionItem(
                title = "TXT / CSV ရေးသားနည်း လမ်းညွှန် (Format Guide)",
                subtitle = "ကတ်များသွင်းရန် ဖိုင် format ဖွဲ့စည်းပုံနှင့် ဥပမာများ ကြည့်မည်",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tag = "sheet_option_format_guide",
                onClick = {
                    onDismiss()
                    onShowFormatGuide()
                }
            )

            // Option 5: Create New Deck (Optional)
            if (onCreateNewDeck != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AddCardSheetOptionItem(
                    title = "Deck အသစ်ဖန်တီးမည် (New Deck)",
                    subtitle = "ဝေါဟာရ အုပ်စုသစ် (ဥပမာ N3 Chapter 1) သတ်မှတ်ရန်",
                    icon = Icons.Default.CreateNewFolder,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tag = "sheet_option_new_deck",
                    onClick = {
                        onDismiss()
                        onCreateNewDeck()
                    }
                )
            }
        }
    }
}

@Composable
private fun AddCardSheetOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
